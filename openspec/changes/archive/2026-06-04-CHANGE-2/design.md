# Design: CHANGE-2 — Gestión de Usuarios

## Technical Approach

Agregar entidades JPA `Paciente` y `Medico` con repositorios, services y controllers REST. Modificar `Cita` para usar relaciones `@ManyToOne` hacia estas entidades en lugar de campos `String`. Crear endpoints de consulta de citas por paciente y por médico. La estrategia de conversión ID→Entidad se maneja en el service layer para mantener los DTOs de API sin cambios.

## Architecture Decisions

### Decision: Relaciones JPA vs Strings (pacienteId/medicoId)

**Choice**: `@ManyToOne` en Cita referenciando entidades Paciente y Medico
**Alternatives considered**: Mantener `String pacienteId`/`medicoId` como columnas simples
**Rationale**: Permite constraints FK en base de datos, carga perezosa de relaciones, y validación de existencia en capa de persistencia. El service layer convierte ID→Entidad al crear/actualizar citas.

### Decision: Repository Pattern (JpaRepository)

**Choice**: JpaRepository para Paciente y Medico
**Alternatives considered**:DAO manual con JDBC, EntityManager directo
**Rationale**: Consistencia con el patrón existente en CitaRepository, query methods automáticos para findById, y reducción de boilerplate.

### Decision: Service Layer (necesario vs directo)

**Choice**: Crear PacienteService y MedicoService
**Alternatives considered**: Acceso directo desde controller a repository
**Rationale**: Permite validación de negocio, conversión de entidad→DTO, y mantiene consistencia con CitaService existente. Facilita testing unitario.

## Data Flow

```
POST /api/v1/citas
       │
       ▼
 CitaController.createCita(CitaRequest)
       │
       ▼
 CitaService.createCita(request)
       │  ├─→ pacienteRepository.findById(request.pacienteId()) → Paciente
       │  ├─→ medicoRepository.findById(request.medicoId()) → Medico
       │  └─→ citaRepository.save(citaConRelaciones)
       │
       ▼
  CitaResponse(con pacienteId, medicoId como Strings)
```

```
GET /api/v1/citas/mis-citas?pacienteId=PAC-001
       │
       ▼
 CitaController.getCitasPorPaciente(pacienteId)
       │
       ▼
 CitaRepository.findByPacienteId(pacienteId) → List<Cita>
       │
       ▼
  CitaResponse List
```

## File Changes

| File | Action | Description |
|------|--------|-------------|
| `src/main/java/com/clinica/model/Paciente.java` | Create | Entidad JPA con id, nombre, email, telefono, fechaRegistro |
| `src/main/java/com/clinica/model/Medico.java` | Create | Entidad JPA con id, nombre, especialidad, email, horarioAtencion |
| `src/main/java/com/clinica/model/Cita.java` | Modify | Remover String pacienteId/medicoId; agregar @ManyToOne Paciente paciente, Medico medico |
| `src/main/java/com/clinica/repository/PacienteRepository.java` | Create | JpaRepository<Paciente, String> |
| `src/main/java/com/clinica/repository/MedicoRepository.java` | Create | JpaRepository<Medico, String> |
| `src/main/java/com/clinica/repository/CitaRepository.java` | Modify | Agregar findByPacienteId, findByMedicoId |
| `src/main/java/com/clinica/service/PacienteService.java` | Create | getPaciente, existsById |
| `src/main/java/com/clinica/service/MedicoService.java` | Create | getMedico, existsById |
| `src/main/java/com/clinica/controller/PacienteController.java` | Create | GET /api/v1/pacientes/{id} |
| `src/main/java/com/clinica/controller/MedicoController.java` | Create | GET /api/v1/medicos/{id} |
| `src/main/java/com/clinica/dto/PacienteResponse.java` | Create | DTO respuesta paciente |
| `src/main/java/com/clinica/dto/MedicoResponse.java` | Create | DTO respuesta medico |
| `src/main/java/com/clinica/dto/CitaRequest.java` | Modify | Sin cambios en estructura (String ids) |
| `src/main/java/com/clinica/dto/CitaResponse.java` | Modify | Sin cambios en estructura (String ids) |
| `src/test/java/com/clinica/service/PacienteServiceTest.java` | Create | Unit tests |
| `src/test/java/com/clinica/service/MedicoServiceTest.java` | Create | Unit tests |

## Interfaces / Contracts

### PacienteResponse.java
```java
public record PacienteResponse(
    String id,
    String nombre,
    String email,
    String telefono,
    LocalDate fechaRegistro
) {}
```

### MedicoResponse.java
```java
public record MedicoResponse(
    String id,
    String nombre,
    String especialidad,
    String email,
    String horarioAtencion
) {}
```

### Repository Query Methods
```java
// CitaRepository
List<Cita> findByPacienteId(String pacienteId);
List<Cita> findByMedicoId(String medicoId);

// PacienteRepository
boolean existsById(String id);

// MedicoRepository
boolean existsById(String id);
```

### Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/pacientes/{id}` | Obtener paciente por ID |
| GET | `/api/v1/medicos/{id}` | Obtener médico por ID |
| GET | `/api/v1/citas/mis-citas?pacienteId={id}` | Listar citas por paciente |
| GET | `/api/v1/citas/medico/{medicoId}` | Listar citas por médico |

## Testing Strategy

| Layer | What to Test | Approach |
|-------|-------------|----------|
| Unit | PacienteService, MedicoService | Mockito: mock repository, verify business logic, null handling |
| Unit | CitaService update (FK validation) | Verify existsById called before save |
| Integration | GET /api/v1/pacientes/{id} | Spring Boot Test: @WebMvcTest, mock service |
| Integration | GET /api/v1/citas/mis-citas | Test query method integration |

## Migration / Rollback

**No migration required** — cambio forward-only. Datos existentes en H2 (in-memory) se recrean en cada inicio. Para entorno production, se requiere migración de datos existentes con String pacienteId/medicoId a nuevas tablas.

**Rollback Plan**:
1. Eliminar entidades Paciente/Medico y sus archivos asociados
2. Restaurar Cita.java a String pacienteId/medicoId
3. Revertir CitaRepository a estado anterior

## Open Questions

- [ ] ¿Se requiere política cascade delete para Cita→Paciente/Medico? (Proposal sugiere RESTRICT)
- [ ] ¿Los endpoints de consulta de citas filtran por estado ACTIVA o retornan todos?