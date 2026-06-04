# Tasks: CHANGE-2 — Gestión de Usuarios

## Review Workload Forecast

| Field | Value |
|-------|-------|
| Estimated changed lines | ~700-800 |
| 400-line budget risk | Medium |
| Chained PRs recommended | No |
| Suggested split | Single PR |
| Delivery strategy | single-pr |

Decision needed before apply: Yes (size:exception)
Chained PRs recommended: No
Chain strategy: pending
400-line budget risk: Medium

## Phase 1: Model/Entity Changes

- [x] 1.1 Create `src/main/java/com/clinica/model/Paciente.java` — JPA entity with id, nombre, email, telefono, fechaRegistro
- [x] 1.2 Create `src/main/java/com/clinica/model/Medico.java` — JPA entity with id, nombre, especialidad, email, horarioAtencion
- [x] 1.3 Modify `src/main/java/com/clinica/model/Cita.java` — replace String pacienteId/medicoId with @ManyToOne relationships to Paciente and Medico

## Phase 2: Repositories

- [x] 2.1 Create `src/main/java/com/clinica/repository/PacienteRepository.java` — JpaRepository<Paciente, String> with existsById method
- [x] 2.2 Create `src/main/java/com/clinica/repository/MedicoRepository.java` — JpaRepository<Medico, String> with existsById method
- [x] 2.3 Modify `src/main/java/com/clinica/repository/CitaRepository.java` — add findByPacienteId and findByMedicoId query methods

## Phase 3: Services

- [x] 3.1 Create `src/main/java/com/clinica/service/PacienteService.java` — getPaciente, existsById methods
- [x] 3.2 Create `src/main/java/com/clinica/service/MedicoService.java` — getMedico, existsById methods
- [x] 3.3 Modify `src/main/java/com/clinica/service/CitaService.java` — update create/update to validate FK existence (pacienteRepository.existsById, medicoRepository.existsById), inject new repositories
- [x] 3.4 Add getCitasPorPaciente and getCitasPorMedico methods to CitaService using new repository query methods

## Phase 4: Controllers + DTOs

- [x] 4.1 Create `src/main/java/com/clinica/dto/PacienteResponse.java` — record with id, nombre, email, telefono, fechaRegistro
- [x] 4.2 Create `src/main/java/com/clinica/dto/MedicoResponse.java` — record with id, nombre, especialidad, email, horarioAtencion
- [x] 4.3 Create `src/main/java/com/clinica/controller/PacienteController.java` — GET /api/v1/pacientes/{id} endpoint
- [x] 4.4 Create `src/main/java/com/clinica/controller/MedicoController.java` — GET /api/v1/medicos/{id} endpoint
- [x] 4.5 Modify `src/main/java/com/clinica/controller/CitaController.java` — add GET /api/v1/citas/mis-citas?pacienteId={id} and GET /api/v1/citas/medico/{medicoId} endpoints

## Phase 5: Testing

- [x] 5.1 Create `src/test/java/com/clinica/service/PacienteServiceTest.java` — unit tests with Mockito for getPaciente, existsById, null handling
- [x] 5.2 Create `src/test/java/com/clinica/service/MedicoServiceTest.java` — unit tests with Mockito for getMedico, existsById, null handling

## Implementation Order

1. **Phase 1** (entities) — no dependencies, foundation for everything
2. **Phase 2** (repositories) — depend on entities from Phase 1
3. **Phase 3** (services) — depend on repositories from Phase 2
4. **Phase 4** (controllers + DTOs) — depend on services from Phase 3
5. **Phase 5** (tests) — depend on services from Phase 3

## Notes

- CitaRequest/CitaResponse DTOs remain unchanged (still use String ids) — conversion happens in service layer
- FK validation in CitaService.createCita/updateCita throws BusinessValidationException if pacienteId or medicoId don't exist
- Cascade delete policy: RESTRICT (no cascade)
- Endpoints filter by ACTIVA state for citas queries
