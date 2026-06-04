# Proposal: CHANGE-2 — Gestión de Usuarios

## Intent

Agregar los modelos Paciente y Medico con sus repositories, services y endpoints REST correspondientes, estableciendo relaciones JPA @ManyToOne con Cita existente.

## Scope

### In Scope
- Modelo Paciente (id, nombre, email, telefono, fechaRegistro)
- Modelo Medico (id, nombre, especialidad, email, horarioAtencion)
- Repositorios JPA para Paciente y Medico
- Services para Paciente y Medico
- Controllers para Paciente y Medico
- Endpoints GET /mis-citas?pacienteId={id} y GET /medico/{id}/citas
- Relaciones JPA: Cita → Paciente, Cita → Medico (@ManyToOne)

### Out of Scope
- Autenticación JWT (Change 3)
- Límite 3 citas activas por paciente (Change 4)
- Rate limiting y audit logs (Change 4)

## Capabilities

### New Capabilities
- `usuario-management`: Gestión de pacientes y médicos — permite consultar datos de pacientes y médicos por ID
- `cita-query`: Consulta de citas por paciente o médico — permite listar citas filtradas por paciente o médico

### Modified Capabilities
- `cita-crud`: La creación de citas requerirá que pacienteId/medicoId existan como entidades válidas (FK constraint)

## Approach

Crear entidades JPA Paciente y Medico con IDs tipo String. Modificar Cita para usar @ManyToOne en lugar de String pacienteId/medicoId. Crear JpaRepositories, Services y Controllers REST. Endpoint /mis-citas?pacienteId={id} lista citas de un paciente; /medico/{medicoId}/citas lista citas de un médico.

## Affected Areas

| Area | Impact | Description |
|------|--------|-------------|
| `src/main/java/com/clinica/model/Paciente.java` | New | Entidad Paciente |
| `src/main/java/com/clinica/model/Medico.java` | New | Entidad Medico |
| `src/main/java/com/clinica/model/Cita.java` | Modified | Agregar @ManyToOne |
| `src/main/java/com/clinica/repository/PacienteRepository.java` | New | JpaRepository |
| `src/main/java/com/clinica/repository/MedicoRepository.java` | New | JpaRepository |
| `src/main/java/com/clinica/service/PacienteService.java` | New | Lógica de negocio |
| `src/main/java/com/clinica/service/MedicoService.java` | New | Lógica de negocio |
| `src/main/java/com/clinica/controller/PacienteController.java` | New | REST endpoints |
| `src/main/java/com/clinica/controller/MedicoController.java` | New | REST endpoints |
| `src/main/java/com/clinica/repository/CitaRepository.java` | Modified | Agregar findByPacienteId, findByMedicoId |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| Migración datos existentes de Cita | Medium | Solo afecta datos nuevos; FK constraint se aplica a partir de migrate |
| Ausencia de auth en /mis-citas | Medium | Documentar como limitación hasta Change 3; parámetro pacienteId requerido |
| Cascade delete policy | Low | Establecer política RESTRICT en FK |

## Rollback Plan

1. Revertir cambios en Cita.java (remover @ManyToOne, volver a String pacienteId/medicoId)
2. Eliminar Paciente.java, Medico.java y sus archivos asociados
3. Eliminar PacienteRepository, MedicoRepository, PacienteService, MedicoService, PacienteController, MedicoController
4. Revertir CitaRepository a estado anterior
5. Descartar cualquier DTO nuevo

## Dependencies
- Change 1 (Cita CRUD) debe estar verificado y archived antes de iniciar

## Success Criteria
- [ ] GET /api/v1/pacientes/{id} retorna 200 con datos del paciente
- [ ] GET /api/v1/medicos/{id} retorna 200 con datos del médico
- [ ] GET /api/v1/citas/mis-citas?pacienteId={id} retorna lista de citas del paciente
- [ ] GET /api/v1/citas/medico/{medicoId} retorna lista de citas del médico
- [ ] POST /api/v1/citas falla si pacienteId o medicoId no existen (FK constraint)