# Proposal: CHANGE-3 - Seguridad JWT

## Intent

Implementar autenticación JWT y autorización por roles para el microservicio. Todos los endpoints excepto `/auth/login` requerirán token JWT válido. Este cambio introduce seguridad completa sin IdP externo, usando Spring Security + JJWT con un enfoque self-contained.

## Scope

### In Scope
- Dependencias: `spring-boot-starter-security` + `jjwt-api/jjwt-impl/jjwt-jackson`
- Entidad `Usuario` (id, username, password, rol)
- `JwtService`: `createToken`, `validateToken`, `getClaims`
- `JwtAuthenticationFilter`: `OncePerRequestFilter` para extraer Bearer token
- `SecurityConfig`: filter chain, `permitAll("/api/v1/auth/login")`, proteger todo lo demás
- `AuthService` + `AuthController`: `POST /api/v1/auth/login`
- DTOs: `LoginRequest` (username, password), `AuthResponse` (token, expiresIn)
- `@Secured` en todos los controllers (`PACIENTE`, `MEDICO`, `ADMIN`)
- Validación de propiedad en service layer (`CitaService`)

### Out of Scope
- Token refresh / token revocation
- BCrypt password encoding (comparación simple en memoria para demo)
- Roles adicionales fuera de PACIENTE, MEDICO, ADMIN
- CORS (documentar pero no implementar en CHANGE-3)

## Capabilities

### New Capabilities
- `jwt-auth`: Autenticación JWT con login y validación de tokens. Login: `POST /api/v1/auth/login` con `{username, password}` → `{token, expiresIn}`
- `role-auth`: Autorización basada en roles con `@Secured` en controllers

### Modified Capabilities
- Ninguna — los specs existentes (`cita-crud`, `usuario-management`) no cambian sus requisitos, solo se añade control de acceso

## Approach

Spring Security con filter chain custom + JJWT (enfoque self-contained, sin IdP externo):

1. **Login**: `POST /api/v1/auth/login` → `AuthService.validateCredentials()` → `JwtService.createToken(userId, rol)` → `AuthResponse` con token
2. **Filtro**: `JwtAuthenticationFilter.doFilterInternal()` — extrae `Authorization: Bearer <token>`, valida con `JwtService.validateToken()`, extrae claims (sub, rol), setea `SecurityContext`
3. **Controlador**: Cada endpoint protegido con `@Secured({"ROL"})` para verificación de rol
4. **Propiedad**: `CitaService` recibe `userId` y `rol` del contexto de seguridad para verificar propiedad en consultas, actualizaciones y eliminaciones

**JWT Claims:**
```json
{ "sub": "user-id", "rol": "PACIENTE|MEDICO|ADMIN", "iat": ..., "exp": ... }
```

**Expiración**: 20 minutos

## Affected Areas

| Area | Impact | Description |
|------|--------|-------------|
| `pom.xml` | Modified | Add spring-boot-starter-security, jjwt-api, jjwt-impl, jjwt-jackson |
| `src/main/java/com/clinica/model/Usuario.java` | New | User entity (id, username, password, rol) |
| `src/main/java/com/clinica/security/JwtService.java` | New | Token operations: createToken, validateToken, getClaims |
| `src/main/java/com/clinica/security/JwtAuthenticationFilter.java` | New | OncePerRequestFilter: extract Bearer, validate, set context |
| `src/main/java/com/clinica/config/SecurityConfig.java` | New | SecurityFilterChain @Bean, permit /auth/login, protect all |
| `src/main/java/com/clinica/service/AuthService.java` | New | Authentication logic |
| `src/main/java/com/clinica/controller/AuthController.java` | New | POST /api/v1/auth/login |
| `src/main/java/com/clinica/dto/LoginRequest.java` | New | (username, password) |
| `src/main/java/com/clinica/dto/AuthResponse.java` | New | (token, expiresIn) |
| `src/main/java/com/clinica/controller/CitaController.java` | Modified | @Secured annotations |
| `src/main/java/com/clinica/controller/PacienteController.java` | Modified | @Secured annotations |
| `src/main/java/com/clinica/controller/MedicoController.java` | Modified | @Secured annotations |
| `src/main/java/com/clinica/service/CitaService.java` | Modified | Ownership checks on get/update/delete |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| Secret key en código | Medium | Usar environment variable `JWT_SECRET` con fallback para dev |
| Sin token revocation | Low | Aceptado para demo; documentar como limitación |
| Ownership check bypass | Medium | Tests unitarios en CitaService con usuarios no-owner |

## Rollback Plan

1. Eliminar archivos nuevos: `Usuario.java`, `JwtService.java`, `JwtAuthenticationFilter.java`, `SecurityConfig.java`, `AuthService.java`, `AuthController.java`, `LoginRequest.java`, `AuthResponse.java`
2. Remover dependencias de `pom.xml`: spring-security, jjwt-*
3. Restaurar `@Secured` annotations eliminadas de controllers
4. Eliminar ownership checks de `CitaService`
5. Si hay冲突 con cambios archivados, regenerar del archive de CHANGE-2

## Dependencies

- Spring Boot 4.0.6 ecosystem (Spring Security 7.x compatible)
- JJWT 0.12.x

## Success Criteria

- [ ] Login con credenciales válidas retorna JWT token
- [ ] Requests sin token reciben 401 Unauthorized
- [ ] Requests con token inválido reciben 401 Unauthorized
- [ ] Endpoints con `@Secured` responden según rol del token
- [ ] Usuario PACIENTE solo puede ver/modificar/eliminarsus propias citas
- [ ] Usuario MEDICO puede ver citas de sus pacientes
- [ ] Usuario ADMIN puede ver todas las citas
- [ ] Todos los tests existentes siguen pasando