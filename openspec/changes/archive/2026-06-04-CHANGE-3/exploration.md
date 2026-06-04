# Exploration: CHANGE-3 — Seguridad JWT

## Current State

### Existing from CHANGE-2 (archived)
- **Entities**: Paciente (String id), Medico (String id), Cita with @ManyToOne relationships
- **Endpoints**: All open — POST/GET/PUT/DELETE /api/v1/citas, GET /api/v1/pacientes/{id}, GET /api/v1/medicos/{id}
- **No authentication**: All requests pass through without any security check
- **pom.xml**: Has `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation` — **NO** spring-security dependency yet

### Security Requirements

| Endpoint | Auth Required | Role/Permission |
|----------|---------------|-----------------|
| POST /api/v1/citas | JWT | PACIENTE role |
| GET /api/v1/citas/{id} | JWT | owner (paciente) OR MEDICO OR ADMIN |
| GET /api/v1/citas/mis-citas | JWT | any authenticated |
| GET /api/v1/citas/medico/{medicoId} | JWT | MEDICO or ADMIN role |
| PUT /api/v1/citas/{id} | JWT | owner (paciente) |
| DELETE /api/v1/citas/{id} | JWT | owner (paciente) |
| GET /api/v1/pacientes/{id} | JWT | any authenticated |
| GET /api/v1/medicos/{id} | JWT | any authenticated |

### Roles Defined
- **PACIENTE**: Can create citas, view/cancel own citas
- **MEDICO**: Can view all citas for a specific doctor
- **ADMIN**: Can view any cita

## Affected Areas

| File | Why Affected |
|------|-------------|
| `pom.xml` | **MODIFY** — Add spring-boot-starter-security and JJWT dependency |
| `src/main/java/com/clinica/config/SecurityConfig.java` | **NEW** — Configure security filter chain, permit /auth/login, protect all else |
| `src/main/java/com/clinica/security/JwtAuthenticationFilter.java` | **NEW** — Extract JWT from Authorization header, set SecurityContext |
| `src/main/java/com/clinica/security/JwtService.java` | **NEW** — Generate and validate JWT tokens |
| `src/main/java/com/clinica/service/AuthService.java` | **NEW** — Authenticate user, return JWT |
| `src/main/java/com/clinica/controller/AuthController.java` | **NEW** — POST /api/v1/auth/login |
| `src/main/java/com/clinica/dto/LoginRequest.java` | **NEW** — login credentials DTO |
| `src/main/java/com/clinica/dto/AuthResponse.java` | **NEW** — JWT token response DTO |
| `src/main/java/com/clinica/controller/CitaController.java` | **MODIFY** — Add @Secured annotations |
| `src/main/java/com/clinica/controller/PacienteController.java` | **MODIFY** — Add @Secured annotations |
| `src/main/java/com/clinica/controller/MedicoController.java` | **MODIFY** — Add @Secured annotations |
| `src/main/java/com/clinica/service/CitaService.java` | **MODIFY** — Add ownership checks for update/delete/cita-view |

## Approaches

### Approach 1: Spring Security OAuth2 Resource Server + JJWT (RECOMMENDED)

Use `spring-boot-starter-security` with JWT validation via JJWT library (io.jsonwebtoken).

| Pros | Cons |
|------|------|
| Standard Spring Security integration | Requires explicit filter chain configuration |
| JJWT gives full control over token structure/claims | More code than OAuth2 Resource Server auto-config |
| Easy to add custom claims (sub, rol) | Must manually implement token validation |
| Works well with @Secured annotations | — |
| Can inject JwtService into services for ownership checks | — |

**JWT Claims Structure:**
```json
{
  "sub": "paciente-123",    // userId
  "rol": "PACIENTE",        // role
  "iat": 1234567890,
  "exp": 1234571490         // 20 min expiry
}
```

**Files:**
```
SecurityConfig.java      — @Bean SecurityFilterChain with JwtAuthenticationFilter
JwtAuthenticationFilter  — OncePerRequestFilter: extract Bearer token, validate, set context
JwtService.java           — createToken(userId, rol), validateToken(token), getUserIdFromToken
LoginRequest.java        — record (username, password)
AuthResponse.java        — record (token, expiresIn)
AuthController.java      — @PostMapping("/auth/login") → AuthService
```

---

### Approach 2: Spring Security OAuth2 Resource Server (JWK Set)

Use `spring-security-oauth2-resource-server` with automatic JWT decoding via JWK Set URI.

| Pros | Cons |
|------|------|
| Minimal code — mostly application.yml config | Requires external JWK Set endpoint (Keycloak, Auth0, etc.) |
| Spring auto-configures JwtAuthenticationProvider | Not suitable for self-contained microservice (no external IdP) |
| Built-in token validation and expiration handling | Less control over token claims structure |

**Not recommended** for this microservice since it should be self-contained without external identity provider dependency.

---

### Approach 3: Custom Filter without Spring Security

Skip Spring Security entirely — use a custom `JwtFilter` that sets a custom principal object directly on request attributes.

| Pros | Cons |
|------|------|
| Maximum control | No integration with Spring's @Secured or @PreAuthorize |
| Lighter weight | Must implement own security convention everywhere |
| No dependency on Spring Security | More code to write |

**Not recommended** — defeats purpose of using Spring Security + JWT standard.

---

### Authorization Strategy: Role + Ownership Checks

**@Secured for role checks** — simplest for roles like PACIENTE, MEDICO, ADMIN.

**Service-layer for ownership checks** — because GET /citas/{id} requires checking if the authenticated user is the owner (paciente) OR has MEDICO/ADMIN role.

Ownership check implementation in CitaService:
```java
@GetMapping("/{id}")
@Secured({"PACIENTE", "MEDICO", "ADMIN"})
public ResponseEntity<CitaResponse> getCita(@PathVariable Long id) {
    Cita cita = citaService.getCitaWithOwnershipCheck(id, getCurrentUserId(), getCurrentUserRole());
    // ...
}
```

## Recommendation

**Approach 1 — Spring Security OAuth2 Resource Server + JJWT** with service-layer ownership checks.

Rationale:
1. Self-contained microservice — no external IdP needed
2. JJWT gives full control over token structure (sub + rol claims)
3. @Secured for simple role checks, service-layer for ownership (clean separation)
4. Matches Spring Boot 4.0.6 ecosystem (Spring Security 7.x)
5. Easy to test — JwtService can be unit tested with mock secret key

### Auth Flow

```
1. POST /api/v1/auth/login { username, password }
   → AuthService.validateCredentials(username, password)
   → JwtService.createToken(userId, rol)
   → Return { token, expiresIn }

2. All subsequent requests: Authorization: Bearer <token>
   → JwtAuthenticationFilter.doFilterInternal
   → JwtService.validateToken(token)
   → JwtService.getUserIdFromToken(token) → extract from claims
   → JwtService.getRolFromToken(token) → extract from claims
   → Set SecurityContext with JwtAuthenticationToken
   → Continue to controller
```

## Risks

1. **Secret key management**: JJWT requires a symmetric secret key — must be stored securely (environment variable, not in code)
2. **Token expiration**: Short-lived tokens (20 min) need refresh strategy — document as out-of-scope for CHANGE-3 (future extension)
3. **Password storage**: For demo, in-memory user store is acceptable; production would need BCrypt hashed passwords in database
4. **CORS**: If frontend calls this API, need to configure CORS policy — should be documented
5. **Logout**: No token blacklist/revocation — token valid until expiration

## Next Steps for Proposal

1. Define Usuario entity (id, username, password, rol) or use existing Paciente/Medico with password field
2. Decide on user store (in-memory for demo, JPA entity for production)
3. Plan SecurityConfig with permit /auth/login, protect all other endpoints
4. Define JwtService interface for token generation/validation
5. Add @Secured annotations to controllers
6. Add ownership checks in CitaService for update/delete/get-by-id

## Ready for Proposal

**Yes** — structured analysis complete.

Key decision needed from user: **Should we create a separate Usuario entity for auth, or add password fields to Paciente/Medico?**

Recommended: Separate `Usuario` entity (cleaner, standard pattern) but keep it simple for demo purposes.