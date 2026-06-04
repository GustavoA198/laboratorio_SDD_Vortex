# Design: CHANGE-4 — Validaciones Avanzadas

## Technical Approach

Implement four business validations: max 3 active citas per patient, anti-duplicate (same medico+fecha+hora), rate limiting (10 req/min per user), and audit logging. Each uses the simplest approach that satisfies the proposal requirements:

- **Max 3 citas**: Repository count query + service validation before save
- **Anti-duplicados**: Repository exists query + service validation before save
- **Rate limiting**: Filter-based ConcurrentHashMap with sliding window (60s window, max 10 requests)
- **Audit logging**: SLF4J structured logger with sync writes (async appender config available)

## Architecture Decisions

### Decision: Rate Limit Storage — In-Memory vs Redis

| Option | Tradeoff | Decision |
|--------|----------|----------|
| In-memory ConcurrentHashMap | Simple, no deps, fast. Loses state on restart. | **Chosen** — MVP scope |
| Redis | Distributed, persistent. Adds external dependency. | Rejected — overkill for single service |

### Decision: Audit Logging — Sync vs Async

| Option | Tradeoff | Decision |
|--------|----------|----------|
| Sync SLF4J | Simple, guaranteed ordering. Potential bottleneck under load. | **Chosen** — simplicity for MVP |
| Async appender | Non-blocking, better throughput. More complex config. | Deferred — can upgrade later |

### Decision: Error Responses for Business Validations

| Scenario | HTTP Status | Rationale |
|----------|-------------|-----------|
| Max 3 citas exceeded | 400 | Business rule violation, bad request |
| Duplicate cita | 409 | Conflict with existing resource |
| Rate limit exceeded | 429 | Too many requests |

## Data Flow

```
Request → RateLimitFilter → Controller → CitaService → CitaRepository → DB
              ↓                                    ↓
          429 if exceeded                   AuditService.log()
                                               ↓
                                          SLF4J Logger
```

**Rate Limit Filter** (new): Intercepts all `/api/v1/citas/**` requests, extracts username from JWT SecurityContext, checks ConcurrentHashMap sliding window. Returns 429 immediately if limit exceeded.

**Audit Service** (new): Provides structured logging with `AUDIT | timestamp={} | user={} | action={} | details={}` format. Called from CitaService after successful operations.

## File Changes

| File | Action | Description |
|------|--------|-------------|
| `src/main/java/com/clinica/repository/CitaRepository.java` | Modify | Add `countByPacienteIdAndEstado(…)`, `existsByMedicoIdAndFechaAndHora(…)` |
| `src/main/java/com/clinica/service/CitaService.java` | Modify | Add validations for max 3 citas and anti-duplicate before save |
| `src/main/java/com/clinica/config/RateLimitFilter.java` | Create | OncePerRequestFilter with ConcurrentHashMap sliding window |
| `src/main/java/com/clinica/service/AuditService.java` | Create | SLF4J structured audit logger |
| `src/main/java/com/clinica/exception/GlobalExceptionHandler.java` | Modify | Add handlers for 429 (TooManyRequests) and 409 (Conflict) |
| `src/main/java/com/clinica/config/SecurityConfig.java` | Modify | Register RateLimitFilter in filter chain |

## Interfaces / Contracts

### CitaRepository (new methods)
```java
long countByPacienteIdAndEstado(String pacienteId, EstadoCita estado);
boolean existsByMedicoIdAndFechaAndHora(String medicoId, LocalDate fecha, LocalTime hora);
```

### AuditService
```java
@Service
public class AuditService {
    private static final Logger log = LoggerFactory.getLogger(AuditService.class);
    
    public void logCreate(String username, Long citaId, String details);
    public void logUpdate(String username, Long citaId, String details);
    public void logCancel(String username, Long citaId, String details);
    public void logGet(String username, Long citaId);
}
```

### RateLimitFilter
```java
@Component
public class RateLimitFilter extends OncePerRequestFilter {
    private final Map<String, List<Long>> requestTimes = new ConcurrentHashMap<>();
    private static final int MAX_REQUESTS = 10;
    private static final long WINDOW_MS = 60_000;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
            HttpServletResponse response, FilterChain chain) { ... }
}
```

### New Exceptions for GlobalExceptionHandler
```java
@ExceptionHandler(RateLimitExceededException.class)
public ResponseEntity<?> handleRateLimit(RateLimitExceededException ex) {
    // Returns 429 TOO_MANY_REQUESTS
}
```

## Testing Strategy

| Layer | What to Test | Approach |
|-------|-------------|----------|
| Unit | CitaService validations (max 3, duplicate) | Mock CitaRepository, verify exception thrown |
| Unit | RateLimitFilter logic | Mock SecurityContext, test sliding window |
| Unit | AuditService log format | Verify log output format |
| Integration | Full flow with real DB | Use @DataJpaTest for repository queries |

## Migration / Rollout

No migration required. This change adds new validation behavior only — no schema or data migration needed.

## Open Questions

- [ ] Should rate limit window be configurable via `application.properties`? (Deferred — hardcode 10/60 for MVP)
- [ ] Do we need separate rate limits for different roles? (Deferred — single limit for all users)