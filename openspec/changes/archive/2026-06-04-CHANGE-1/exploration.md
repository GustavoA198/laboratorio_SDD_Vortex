# Exploration: CHANGE-1 — CRUD Básico de Citas

## Current State

**Greenfield project** — No existing Java/Maven source files. This is the first change for `microservicio-clinica`.

### Domain Model (from requirements)

**Entity: Cita**
| Field | Type | Notes |
|-------|------|-------|
| id | Long | Auto-generated |
| pacienteId | String | From User service (future) |
| medicoId | String | From User service (future) |
| fecha | LocalDate | Agendamiento |
| hora | LocalTime | 08:00-17:00 |
| estado | EstadoCita | Enum: ACTIVA, CANCELADA, COMPLETADA |
| motivoConsulta | String |nullable, max 500 chars |
| fechaCreacion | LocalDateTime | Auto-set |

**EstadoCita enum lifecycle:**
```
[NEW] --> ACTIVA --> CANCELADA
                  --> COMPLETADA
```

### Business Rules (per requirements)

| # | Rule | Change 1 Scope? |
|---|------|----------------|
| 1 | Paciente max 3 citas activas | Later (Change 4) |
| 2 | Horario 08:00-17:00 | **Yes** |
| 3 | Duración 30 minutos | **Yes** (implicit slot) |
| 4 | No fines de semana | **Yes** |
| 5 | No citas duplicadas | Later (Change 4) |

**Decision for Change 1:** Only implement rules 2, 3, 4. Rules 1 and 5 require patient/doctor lookup that doesn't exist yet (Change 2).

---

## Affected Areas

This is the **first change** — everything is new.

| Area | Path | Purpose |
|------|------|---------|
| Entity | `src/main/java/com/clinica/citas/model/Cita.java` | JPA entity |
| Enum | `src/main/java/com/clinica/citas/model/EstadoCita.java` | ACTIVA/CANCELADA/COMPLETADA |
| Repository | `src/main/java/com/clinica/citas/repository/CitaRepository.java` | Spring Data JPA |
| DTOs | `src/main/java/com/clinica/citas/dto/` | Request/Response objects |
| Service | `src/main/java/com/clinica/citas/service/CitaService.java` | Business logic |
| Controller | `src/main/java/com/clinica/citas/controller/CitaController.java` | REST endpoints |
| Exception | `src/main/java/com/clinica/citas/exception/` | Global exception handling |
| Tests | `src/test/java/com/clinica/citas/` | Unit + integration tests |
| Schema | `src/main/resources/schema.sql` | H2 DDL |
| Test data | `src/main/resources/data.sql` | Sample data for testing |

---

## Approaches

### Approach 1: Minimal DTOs with Validation on Service Layer

**Design:**
- Create `CrearCitaRequest` (pacienteId, medicoId, fecha, hora, motivoConsulta)
- Create `ActualizarCitaRequest` (fecha, hora, motivoConsulta)
- Create `CitaResponse` for all reads
- Validations in `CitaService` using `java.time` APIs
- Repository extends `JpaRepository<Cita, Long>`

| Pros | Cons |
|------|------|
| Simple, fast to implement | DTOs may need expansion later |
| Service layer is testable | Validation scattered across methods |
| Standard Spring patterns | |

**Effort:** Low

### Approach 2: Rich DTOs with Bean Validation (JSR-380)

**Design:**
- Use `@NotNull`, `@NotBlank`, `@Size`, `@Future` on DTOs
- Custom `@HorarioValido` and `@DiaHabil` validators as annotations
- Controller receives validated `@Valid` DTOs
- Service focuses on orchestration

| Pros | Cons |
|------|------|
| Declarative validation, self-documenting | Custom validators require extra classes |
| Errors grouped via `MethodArgumentNotValidException` | Overkill for Change 1 scope |
| Reusable across controllers | |

**Effort:** Medium

### Approach 3: Domain-Driven with State Machine

**Design:**
- `Cita` entity has explicit state transitions via `canTransitionTo(EstadoCita)` method
- Service enforces `validarTransicion()` before any state change
- `CitaNotFoundException`, `CitaNoActualizableException` custom exceptions

| Pros | Cons |
|------|------|
| Explicit lifecycle, harder to misuse | More code for simple CRUD |
| Self-validating entity | YAGNI for future states? |
| Clear contract | |

**Effort:** Medium

---

## Recommendation

**Approach 1 (Minimal DTOs + Service Validation)** for Change 1 because:
1. Greenfield — no legacy to preserve
2. Change 1 scope is deliberately constrained (basic CRUD only)
3. Complex validation (rule 1: 3-citas limit, rule 5: duplicates) deferred to Change 4
4. Service-layer validation is easier to test without Spring context
5. TDD enabled — tests drive the simplest correct implementation

**Entity design for future-proofing:**
- Add `canTransitionTo()` private method to Cita entity for state machine
- Add `duracionMinutos()` returning 30 constant (future: configurable)
- Repository query methods for future checks: `findByMedicoIdAndFechaAndHora()`, `countByPacienteIdAndEstado()`

---

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| No existing code patterns to follow | High | Use standard Spring Boot conventions from start |
| Hard to validate "mismo médico, mismo horario" without medicos table | High | Stub repository with empty implementations, full query in Change 4 |
| Business rule scope creep | Medium | Explicitly defer rules 1 & 5 to Change 4 in specs |
| TDD might slow initial delivery | Low | Short sprint, focused scope, red-green-refactor |
| H2 schema evolution across changes | Medium | Use `schema.sql` + `Flyway` consideration for Change 5+ |

---

## Ready for Proposal

**Yes.**

- Domain model clearly bounded
- Scope explicitly scoped to basic CRUD (rules 2, 3, 4 only)
- Business rules 1 & 5 deferred to Change 4
- State lifecycle defined
- Validation boundaries: API-level (HTTP status, basic input) + Service-level (domain rules)
- All affected areas identified

**Next phase: sdd-propose** — define approach, rollback plan, scope confirmation with user.

---

## Appendix: API Surface for Change 1

```
POST   /api/v1/citas          → 201 Created, 400 Bad Request, 422 (validation failed)
GET    /api/v1/citas/{id}     → 200 OK, 404 Not Found
PUT    /api/v1/citas/{id}     → 200 OK, 400 Bad Request, 404, 409 (not ACTIVA)
DELETE /api/v1/citas/{id}     → 200 OK (→ CANCELADA), 404
```

**Validation for Change 1:**
- `hora` must be between 08:00 and 16:30 (last slot starts at 16:30, ends at 17:00)
- `fecha` must be Monday-Friday (no Saturday/Sunday)
- `estado` on PUT: only if current state is ACTIVA
- `estado` on DELETE: ACTIVA → CANCELADA (idempotent)