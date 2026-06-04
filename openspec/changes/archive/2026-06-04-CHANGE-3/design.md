# Design: CHANGE-3 - Seguridad JWT

## Technical Approach

Implementar autenticación JWT y autorización por roles usando Spring Security 7.x con filter chain custom + JJWT 0.12.x. El flujo es: Request → JwtAuthenticationFilter (OncePerRequestFilter) extrae Bearer token → valida con JwtService → setea SecurityContext → Controller con @Secured.

## Architecture Decisions

### Decision: Spring Security + JJWT vs OAuth2 Resource Server

**Choice**: Spring Security + JJWT (custom filter chain)
**Alternatives considered**: OAuth2 Resource Server with JWT decoder
**Rationale**: Enfoque self-contained sin IdP externo. OAuth2 Resource Server agrega complejidad innecesaria para un demo. JJWT da control total sobre token creation/validation y es más ligero.

### Decision: Filter vs Interceptor

**Choice**: OncePerRequestFilter
**Alternatives considered**: HandlerInterceptor
**Rationale**: Filters ejecutan antes del dispatch a controllers, correctos para authentication/authorization. HandlerInterceptor es post-dispatch, más apropiado para logging. OncePerRequestFilter garantiza una sola ejecución por request.

### Decision: @Secured vs @RolesAllowed

**Choice**: @Secured
**Alternatives considered**: @RolesAllowed (JSR-250)
**Rationale**: @Secured es Spring-specific, más flexible para expresiones SpEL futuras. @RolesAllowed es estándar JSR-250 pero menos configurable. El proyecto usa anotaciones Spring (e.g., @Service, @Component), coherencia con el stack.

### Decision: User Storage (in-memory vs database)

**Choice**: In-memory con UsuarioRepository (JPA)
**Alternatives considered**: Hardcoded Map<String, UserDetails>
**Rationale**: UsuarioRepository permite extensión futura a DB sin cambiar código. Mapa hardcodeado es difícil de mantener y no permite escalabilidad.

## Data Flow

```
┌─────────────┐    ┌──────────────────────┐    ┌────────────────┐
│   Request   │───▶│ JwtAuthenticationFilter│───▶│ SecurityContext│
│ (Bearer)    │    │  1. Extract Bearer    │    │  setea auth    │
└─────────────┘    │  2. validateToken()   │    └───────────────┘
                   │  3. extract claims    │           │
                   │  4. set SecurityContext           │
                   └──────────────────────┘           ▼
                                                  ┌────────┐
                                                  │Controller│
                                                  │ @Secured │
                                                  └────────┘
```

## File Changes

| File | Action | Description |
|------|--------|-------------|
| `pom.xml` | Modify | Add spring-boot-starter-security, jjwt-api, jjwt-impl, jjwt-jackson |
| `src/main/java/com/clinica/model/Usuario.java` | Create | Entity: id (Long), username, password, rol (String) |
| `src/main/java/com/clinica/repository/UsuarioRepository.java` | Create | JPA repository for Usuario (findByUsername) |
| `src/main/java/com/clinica/service/JwtService.java` | Create | Token operations: createToken(userId, rol), validateToken(token), getClaims(token) |
| `src/main/java/com/clinica/security/JwtAuthenticationFilter.java` | Create | OncePerRequestFilter: doFilterInternal extracts Bearer, valida, set SecurityContext |
| `src/main/java/com/clinica/config/SecurityConfig.java` | Create | SecurityFilterChain @Bean, permit /api/v1/auth/login, protect all else |
| `src/main/java/com/clinica/service/AuthService.java` | Create | Authentication logic: validateCredentials, generateToken |
| `src/main/java/com/clinica/controller/AuthController.java` | Create | POST /api/v1/auth/login endpoint |
| `src/main/java/com/clinica/dto/LoginRequest.java` | Create | Record: username, password |
| `src/main/java/com/clinica/dto/AuthResponse.java` | Create | Record: token (String), expiresIn (long) |
| `src/main/java/com/clinica/controller/CitaController.java` | Modify | Add @Secured annotations per endpoint |
| `src/main/java/com/clinica/controller/PacienteController.java` | Modify | Add @Secured annotations |
| `src/main/java/com/clinica/controller/MedicoController.java` | Modify | Add @Secured annotations |
| `src/main/java/com/clinica/service/CitaService.java` | Modify | Add ownership checks: getCita, updateCita, cancelCita |

## JWT Structure

```json
Header: { "alg": "HS256", "typ": "JWT" }
Payload: {
  "sub": "user-id-123",
  "rol": "PACIENTE",
  "iat": 1717536000,
  "exp": 1717537200
}
```

- **Algorithm**: HS256 (HMAC SHA-256)
- **Secret**: from environment variable `JWT_SECRET` (fallback: `clinica-dev-secret-key-2024` for development)
- **Expiration**: 20 minutes (1200 seconds)
- **Claims**: sub (userId), rol (PACIENTE|MEDICO|ADMIN), iat, exp

## Interfaces / Contracts

### AuthService

```java
public interface AuthService {
    AuthResponse login(LoginRequest request);  // throws AuthenticationException
}
```

### JwtService

```java
public interface JwtService {
    String createToken(String userId, String rol);
    boolean validateToken(String token);
    Claims getClaims(String token);  // throws JwtException
}
```

### SecurityConfig Bean

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/v1/auth/login").permitAll()
            .anyRequest().authenticated()
        )
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
}
```

## Testing Strategy

| Layer | What to Test | Approach |
|-------|-------------|----------|
| Unit | JwtService token creation/validation | Mock Secret, assert claims extraction |
| Unit | JwtAuthenticationFilter | Mock HttpServletRequest with/without Bearer header |
| Unit | AuthService login success/failure | Mock UsuarioRepository |
| Integration | /api/v1/auth/login | MockMvc POST, assert 200/401 |
| Integration | Protected endpoints | MockMvc with Bearer token, assert 200/401/403 |
| Unit | CitaService ownership | Mock SecurityContext, assert access granted/denied |

## Migration / Rollout

No migration required. Esta es una adición de seguridad que no modifica datos existentes. El feature es backward-compatible: requests sin token reciben 401, no alteran comportamiento de datos.

## Open Questions

- [ ] ¿Usar BCrypt para password hashing en producción? (demo usa comparación simple)
- [ ] ¿Implementar logout manual (token blacklist)? Out of scope para CHANGE-3 pero sería necesario para producción
- [ ] ¿Rotación de secret key? El JWT_SECRET no tiene mecanismo de rotación; si se comprometa, todos los tokens quedan inválidos