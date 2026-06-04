# Tasks: CHANGE-4 — Validaciones Avanzadas

## Review Workload Forecast

| Field | Value |
|-------|-------|
| Estimated changed lines | ~450-550 |
| 400-line budget risk | Medium |
| Chained PRs recommended | No |
| Suggested split | Single PR |
| Delivery strategy | ask-on-risk |
| Chain strategy | pending |

Decision needed before apply: No
Chained PRs recommended: No
Chain strategy: single-pr
400-line budget risk: Medium

### Suggested Work Units

| Unit | Goal | Likely PR | Notes |
|------|------|-----------|-------|
| 1 | Full implementation | PR 1 | All phases included, single commit |

## Phase 1: Repository Changes (Foundation)

- [x] 1.1 Add `countByPacienteIdAndEstado(String pacienteId, EstadoCita estado)` to `CitaRepository.java`
- [x] 1.2 Add `existsByMedicoIdAndFechaAndHora(String medicoId, LocalDate fecha, LocalTime hora)` to `CitaRepository.java`

## Phase 2: Service Validations (Core Implementation)

- [x] 2.1 Add max 3 active citas validation in `CitaService.createCita()` — query countByPacienteIdAndEstado and throw BusinessValidationException if >= 3
- [x] 2.2 Add anti-duplicate validation in `CitaService.createCita()` — query existsByMedicoIdAndFechaAndHora and throw BusinessValidationException (409 Conflict) if duplicate exists

## Phase 3: Rate Limiting (New Component)

- [x] 3.1 Create `RateLimitExceededException.java` in `com.clinica.exception`
- [x] 3.2 Create `RateLimitFilter.java` in `com.clinica.config` — OncePerRequestFilter with ConcurrentHashMap sliding window (10 req/min per user)
- [x] 3.3 Add rate limit handler to `GlobalExceptionHandler.java` — returns 429 with JSON `{"error": "RATE_LIMIT_EXCEEDED", "message": "Rate limit exceeded. Maximum 10 requests per minute allowed."}`
- [x] 3.4 Register RateLimitFilter in `SecurityConfig.java` filter chain (after JwtAuthenticationFilter)

## Phase 4: Audit Logging (New Component)

- [x] 4.1 Create `AuditService.java` in `com.clinica.service` — SLF4J structured logger with logCreate, logUpdate, logCancel, logGet methods
- [x] 4.2 Inject AuditService into `CitaService.java` and add audit logging calls after successful create/update/cancel/get operations
- [x] 4.3 Log format: `AUDIT | timestamp={} | user={} | action={} | details={}` with fields: timestamp (ISO-8601), username, operation, method, path, status, duration_ms

## Phase 5: Testing

- [x] 5.1 Create `ValidacionCitasTest.java` in `src/test/java/com/clinica/service/` — test max 3 citas validation (success at 2, reject at 3, ignore cancelled) and anti-duplicate (same medico+fecha+hora rejected, different time/medico allowed)
- [x] 5.2 Create `RateLimitFilterTest.java` in `src/test/java/com/clinica/config/` — test sliding window (allow 10, reject 11th, reset after 60s)

## Implementation Order

1. Repository changes (Phase 1) must be completed first — service validations depend on these query methods
2. Service validations (Phase 2) use Phase 1 methods
3. Rate limiting (Phase 3) is independent but integrates with SecurityConfig
4. Audit logging (Phase 4) is independent but logs service operations
5. Testing (Phase 5) should be done after all implementation phases complete

## Files Summary

| File | Action | Lines (est.) |
|------|--------|--------------|
| `src/main/java/com/clinica/repository/CitaRepository.java` | Modify | +10 |
| `src/main/java/com/clinica/service/CitaService.java` | Modify | +30 |
| `src/main/java/com/clinica/exception/RateLimitExceededException.java` | Create | +15 |
| `src/main/java/com/clinica/config/RateLimitFilter.java` | Create | +80 |
| `src/main/java/com/clinica/exception/GlobalExceptionHandler.java` | Modify | +20 |
| `src/main/java/com/clinica/config/SecurityConfig.java` | Modify | +5 |
| `src/main/java/com/clinica/service/AuditService.java` | Create | +60 |
| `src/test/java/com/clinica/service/ValidacionCitasTest.java` | Create | +150 |
| `src/test/java/com/clinica/config/RateLimitFilterTest.java` | Create | +120 |
| **Total** | | **~490** |
