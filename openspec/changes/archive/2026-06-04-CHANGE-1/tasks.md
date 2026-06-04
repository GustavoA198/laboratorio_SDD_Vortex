# Tasks: CHANGE-1 — CRUD Básico de Citas

## Review Workload Forecast

| Field | Value |
|-------|-------|
| Estimated changed lines | ~800-1000 (13 files + tests) |
| 400-line budget risk | Medium |
| Chained PRs recommended | Yes |
| Suggested split | PR 1 (Foundation) → PR 2 (Core + Tests) |
| Delivery strategy | ask-on-risk |
| Chain strategy | pending |

Decision needed before apply: Yes
Chained PRs recommended: Yes
Chain strategy: stacked-to-main|feature-branch-chain|size-exception|pending
400-line budget risk: Medium

### Suggested Work Units

| Unit | Goal | Likely PR | Notes |
|------|------|-----------|-------|
| 1 | Foundation — pom.xml, main class, entity, enum, repository, config | PR 1 | Base branch = main; includes DTOs + exceptions (dependent on foundation) |
| 2 | Core — service, controller, integration tests | PR 2 | Base branch = PR 1; service + controller + CitaServiceTest |

## Phase 1: Foundation / Infrastructure

- [x] 1.1 Create `pom.xml` with spring-boot-starter-web, spring-boot-starter-data-jpa, h2, lombok, springdoc-openapi, test dependencies
- [x] 1.2 Create `src/main/java/com/clinica/MicroservicioClinicaApplication.java` with @SpringBootApplication
- [x] 1.3 Create `src/main/java/com/clinica/model/Cita.java` JPA entity (id, pacienteId, medicoId, fecha, hora, estado, motivoConsulta, fechaCreacion)
- [x] 1.4 Create `src/main/java/com/clinica/model/enums/EstadoCita.java` enum (ACTIVA, CANCELADA, COMPLETADA)
- [x] 1.5 Create `src/main/java/com/clinica/repository/CitaRepository.java` extending JpaRepository<Cita, Long>
- [x] 1.6 Create `src/main/resources/application.properties` with H2 config, JPA settings

## Phase 2: Core Implementation — DTOs + Exceptions

- [x] 2.1 Create `src/main/java/com/clinica/dto/CitaRequest.java` record (pacienteId, medicoId, fecha, hora, motivoConsulta)
- [x] 2.2 Create `src/main/java/com/clinica/dto/CitaResponse.java` record (id, pacienteId, medicoId, fecha, hora, estado, motivoConsulta, fechaCreacion)
- [x] 2.3 Create `src/main/java/com/clinica/exception/CitaNotFoundException.java` RuntimeException (404)
- [x] 2.4 Create `src/main/java/com/clinica/exception/BusinessValidationException.java` RuntimeException (400)
- [x] 2.5 Create `src/main/java/com/clinica/exception/GlobalExceptionHandler.java` with @ControllerAdvice (handles MethodArgumentNotValidException, CitaNotFoundException, BusinessValidationException)

## Phase 3: Core Implementation — Service + Controller

- [x] 3.1 Create `src/main/java/com/clinica/service/CitaService.java` with CRUD operations + validations (horario 08:00-17:00, slots :00/:30, días L-V)
- [x] 3.2 Create `src/main/java/com/clinica/controller/CitaController.java` with POST /api/v1/citas, GET /api/v1/citas/{id}, GET /api/v1/citas, PUT /api/v1/citas/{id}, DELETE /api/v1/citas/{id}

## Phase 4: Testing

- [x] 4.1 Create `src/test/java/com/clinica/service/CitaServiceTest.java` with @ExtendWith(MockitoExtension.class) — unit tests for CRUD + validations
- [x] 4.2 Create integration test class with @SpringBootTest + TestRestTemplate (full CRUD flow against embedded H2)

## Implementation Order

1. **PR 1 — Foundation**: Start with pom.xml and infrastructure files (1.1–1.6), then DTOs/exceptions (2.1–2.5) since they have no internal dependencies. This establishes the project skeleton.
2. **PR 2 — Core**: CitaService (3.1) first (business logic, no external deps), then CitaController (3.2), then tests (4.1–4.2). Controller depends on Service; tests verify both.

## Verification Criteria

- [ ] POST /api/v1/citas creates valid cita → 201 Created
- [ ] GET /api/v1/citas/{id} returns existing cita → 200 OK
- [ ] GET /api/v1/citas/{id} for non-existent → 404 (CitaNotFoundException)
- [ ] PUT /api/v1/citas/{id} on ACTIVA cita → 200 OK
- [ ] PUT /api/v1/citas/{id} on non-ACTIVA cita → 409 Conflict
- [ ] DELETE /api/v1/citas/{id} changes estado to CANCELADA → 204 No Content
- [ ] Horario validation: outside 08:00-17:00 → 400 (BusinessValidationException)
- [ ] Slot validation: times not :00 or :30 → 400 (BusinessValidationException)
- [ ] Día validation: Saturday/Sunday → 400 (BusinessValidationException)
- [ ] CitaServiceTest coverage >70%
