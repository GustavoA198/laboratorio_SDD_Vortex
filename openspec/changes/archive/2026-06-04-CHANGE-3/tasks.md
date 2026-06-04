# Tasks: CHANGE-3 - Seguridad JWT

## Review Workload Forecast

| Field | Value |
|-------|-------|
| Estimated changed lines | ~650 |
| 400-line budget risk | Medium |
| Chained PRs recommended | No |
| Suggested split | Single commit |
| Delivery strategy | ask-on-risk |

Decision needed before apply: No
Chained PRs recommended: No
Chain strategy: pending
400-line budget risk: Medium

## Phase 1: Dependencies + Entity

- [x] 1.1 Modify `pom.xml` — add `spring-boot-starter-security`, `jjwt-api`, `jjwt-impl`, `jjwt-jackson` (0.12.x)
- [x] 1.2 Create `src/main/java/com/clinica/model/Usuario.java` — entity: id (Long), username (String), password (String), rol (String)
- [x] 1.3 Create `src/main/java/com/clinica/repository/UsuarioRepository.java` — JPA repository with `findByUsername(String)`

## Phase 2: JWT Infrastructure

- [x] 2.1 Create `src/main/java/com/clinica/service/JwtService.java` — `createToken(userId, rol)`, `validateToken(token)`, `getClaims(token)`. HS256, 20min expiration, secret from env `JWT_SECRET` (fallback: `clinica-dev-secret-key-2024`)
- [x] 2.2 Create `src/main/java/com/clinica/security/JwtAuthenticationFilter.java` — `OncePerRequestFilter`: extract `Authorization: Bearer <token>`, call `JwtService.validateToken()`, extract `sub` and `rol` claims, set `SecurityContextHolder` with `UsernamePasswordAuthenticationToken`

## Phase 3: Security Config

- [x] 3.1 Create `src/main/java/com/clinica/config/SecurityConfig.java` — `@Bean SecurityFilterChain`: `csrf.disable()`, `requestMatchers("/api/v1/auth/login").permitAll()`, all else `.authenticated()`. Add `JwtAuthenticationFilter` before `UsernamePasswordAuthenticationFilter`

## Phase 4: Auth Service + Controller

- [x] 4.1 Create `src/main/java/com/clinica/dto/LoginRequest.java` — record: username (String), password (String)
- [x] 4.2 Create `src/main/java/com/clinica/dto/AuthResponse.java` — record: token (String), expiresIn (long)
- [x] 4.3 Create `src/main/java/com/clinica/service/AuthService.java` — `login(LoginRequest)`: validate credentials via `UsuarioRepository`, call `JwtService.createToken()`, return `AuthResponse`. Throw `AuthenticationException` on failure
- [x] 4.4 Create `src/main/java/com/clinica/controller/AuthController.java` — `POST /api/v1/auth/login` — accept JSON body, delegate to `AuthService`, return `AuthResponse`

## Phase 5: Controller Authorization

- [x] 5.1 Modify `src/main/java/com/clinica/controller/CitaController.java` — add `@Secured` annotations: `POST /api/v1/citas` → `{"PACIENTE","MEDICO","ADMIN"}`, `GET /api/v1/citas/{id}` → `{"PACIENTE","MEDICO","ADMIN"}`, `PUT`/`DELETE` → role-checked
- [x] 5.2 Modify `src/main/java/com/clinica/controller/PacienteController.java` — add `@Secured({"PACIENTE","ADMIN"})` to all endpoints
- [x] 5.3 Modify `src/main/java/com/clinica/controller/MedicoController.java` — add `@Secured({"MEDICO","ADMIN"})` to all endpoints

## Phase 6: Service Ownership Checks

- [x] 6.1 Modify `src/main/java/com/clinica/service/CitaService.java` — add ownership logic: `getCita(id, userId, rol)` — PACIENTE can only access own citas, MEDICO can access citas by them, ADMIN bypasses check. Apply to `updateCita`, `cancelCita` methods too

## Phase 7: Testing

- [x] 7.1 Create `src/test/java/com/clinica/service/JwtServiceTest.java` — test `createToken` (assert claims), `validateToken` (valid/invalid/expired), `getClaims` ( malformed token throws)
- [x] 7.2 Create `src/test/java/com/clinica/service/AuthServiceTest.java` — test login success (valid creds → token), login failure (invalid creds → throw `AuthenticationException`)