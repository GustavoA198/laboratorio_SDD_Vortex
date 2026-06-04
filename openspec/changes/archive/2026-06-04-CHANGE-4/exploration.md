# Exploration: CHANGE-4 — Validaciones Avanzadas

## Current State

### Existing Architecture
- **Entities**: Cita (with estado: ACTIVA, CANCELADA, COMPLETADA), Paciente, Medico, Usuario
- **CitaRepository**: Has `findByPacienteId`, `findByMedicoId` — NO active cita count, NO duplicate check
- **CitaService**: Has `validarHorario` (08:00-17:00), `validarSlot` (:00 or :30), `validarDiaHabil` (Mon-Fri)
- **JWT auth**: JwtAuthenticationFilter extracts username/rol from token, SecurityContext set
- **Security config**: All `/api/v1/citas` endpoints require authenticated user (any role)

### Existing Validations (CHANGE-1)
- Horario: 08:00-17:00
- Slot: 30-minute intervals (:00 or :30)
- Día hábil: Monday-Friday only
- Paciente and Médico existence checks

### What's Missing (for CHANGE-4)
1. **Max 3 active citas per paciente** — No check currently
2. **Anti-duplicados** — No check for same medico+fecha+hora
3. **Rate limiting** — No rate limiting implemented
4. **Audit logs** — No structured logging of operations

---

## Affected Areas

| File | Why Affected |
|------|-------------|
| `src/main/java/com/clinica/repository/CitaRepository.java` | **MODIFY** — Add `countByPacienteIdAndEstado`, `existsByMedicoIdAndFechaAndHora` |
| `src/main/java/com/clinica/service/CitaService.java` | **MODIFY** — Add max 3 citas validation, anti-duplicado check in `createCita` |
| `src/main/java/com/clinica/config/RateLimitFilter.java` | **NEW** — Rate limiting filter (10 req/min per user) |
| `src/main/java/com/clinica/service/AuditService.java` | **NEW** — Audit logging service |
| `src/main/java/com/clinica/controller/CitaController.java` | **MODIFY** — Add audit logging to endpoints |
| `src/main/java/com/clinica/exception/GlobalExceptionHandler.java` | **MODIFY** — Handle 429 Too Many Requests, 409 Conflict |
| `pom.xml` | **NO CHANGE** — Existing deps sufficient (Spring Security already has ConcurrentHashMap support) |

---

## Approaches

### 1. Max 3 Citas Activas

**Option A: Query-level count in CitaService (RECOMMENDED)**
```java
// CitaRepository
long countByPacienteIdAndEstado(String pacienteId, EstadoCita estado);

// CitaService.createCita — before save
long activeCount = citaRepository.countByPacienteIdAndEstado(
    request.pacienteId(), EstadoCita.ACTIVA);
if (activeCount >= 3) {
    throw new BusinessValidationException("Máximo 3 citas activas por paciente");
}
```
- Pros: Simple, explicit, easy to test
- Cons: Additional DB query before each create

**Option B: In-memory counter per session**
- Track active citas count in Paciente entity or external cache
- Cons: Complexity, eventual consistency issues — NOT recommended

### 2. Anti-Duplicados

**Option A: Database unique constraint (RECOMMENDED)**
```java
// CitaRepository
boolean existsByMedicoIdAndFechaAndHora(String medicoId, LocalDate fecha, LocalTime hora);

// CitaService.createCita — before save
if (citaRepository.existsByMedicoIdAndFechaAndHora(medicoId, fecha, hora)) {
    throw new BusinessValidationException("Ya existe una cita con este médico en ese horario");
}
```
- Pros: DB-level guarantee, simple query
- Cons: Exception handling for constraint violation

**Option B: Transaction-level check with lock**
- Use `SELECT FOR UPDATE` on medico+fecha+hora
- Cons: Over-engineering for this use case — NOT recommended

### 3. Rate Limiting

**Option A: Filter-based with ConcurrentHashMap (RECOMMENDED)**
```java
// RateLimitFilter extends OncePerRequestFilter
private final Map<String, List<Long>> requestTimes = new ConcurrentHashMap<>();
// sliding window: keep timestamps in last 60 seconds, count <= 10
```
- Pros: Simple, in-memory, no external deps, per-user tracking via JWT username
- Cons: Memory grows if not cleaned (use schedule cleanup or size-bounded list)

**Option B: Bucket4j with Redis**
- External dependency for distributed rate limiting
- Cons: Overkill for single microservice — NOT recommended

**Option C: Spring Security Core RateLimiter**
- Spring's built-in rate limiting
- Cons: Requires additional config, less transparent — NOT recommended

### 4. Audit Logs

**Option A: Service-layer logging with SLF4J (RECOMMENDED)**
```java
// AuditService
public void log(String action, String username, String details) {
    log.info("AUDIT | timestamp={} | user={} | action={} | details={}",
        LocalDateTime.now(), username, action, details);
}

// CitaService methods call auditService.log() after successful operations
```
- Pros: Standard SLF4J, structured, easy to search/filter
- Cons: Async needed for performance (can configure async appender)

**Option B: Database audit table**
- Insert into `audit_log` table
- Cons: Additional table, transaction overhead — NOT recommended for this scope

---

## Recommendation

**All Option A for each rule** — simplest, most direct implementation.

### Implementation Plan

1. **CitaRepository** — Add two new methods:
   - `countByPacienteIdAndEstado(String pacienteId, EstadoCita estado)`
   - `existsByMedicoIdAndFechaAndHora(String medicoId, LocalDate fecha, LocalTime hora)`

2. **CitaService.createCita** — Add validations BEFORE save:
   - Count active citas for paciente, throw if >= 3
   - Check duplicate (medico+fecha+hora), throw if exists

3. **RateLimitFilter** — NEW file:
   - Extract username from SecurityContext
   - Track requests in ConcurrentHashMap<String, List<Long>>
   - If > 10 in last 60 seconds → return 429

4. **AuditService** — NEW file:
   - Simple SLF4J logger with structured format
   - Methods: `logCreate`, `logUpdate`, `logCancel`, `logGet`

5. **GlobalExceptionHandler** — Add handlers for:
   - `429 TOO_MANY_REQUESTS` for rate limit exceeded
   - `409 CONFLICT` for duplicate cita

---

## Risks

1. **Rate limit memory leak**: ConcurrentHashMap with unbounded Lists — needs periodic cleanup or use `LinkedList` with removeIf older than 60s
2. **Race condition on max 3 citas**: Two concurrent creates could both pass the count check — acceptable for MVP (can add pessimistic lock if needed)
3. **Audit performance**: Synchronous logging on every request — configure async appender for production
4. **Time zone for audit logs**: Use UTC consistently

---

## Ready for Proposal

**Yes** — structured analysis complete.

All four business rules are implementable with clear approaches. No major architectural decisions needed — additions to existing patterns.