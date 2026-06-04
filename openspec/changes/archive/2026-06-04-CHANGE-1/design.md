# Design: CHANGE-1 — CRUD Básico de Citas

## Technical Approach

REST API con arquitectura por capas (Controller → Service → Repository). Business validations live in `CitaService`, custom exceptions handled by `GlobalExceptionHandler`. Manual DTOs avoid external dependencies. H2 in-memory database for persistence. Greenfield project — no existing patterns to follow, so Spring Boot conventions are used.

## Architecture Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| DTOs | Manual (`CitaRequest`/`CitaResponse`) | Avoids MapStruct dependency for simple transformations; straightforward mapping in service layer |
| Exception handling | `GlobalExceptionHandler` with `@ControllerAdvice` | Centralized error responses, consistent JSON structure across all endpoints |
| Cita state machine | ACTIVA → CANCELADA/COMPLETADA (no backwards transitions) | Stateless transitions prevent invalid state flows; DELETE sets CANCELADA, not removed |
| Validation location | Service layer (before persistence) | Keeps controller thin; enables reuse if multiple controllers access the service |
| Repository | Spring Data JPA `JpaRepository<Cita, Long>` | Standard pattern, provides CRUD + pagination out of the box |

## Data Flow

```
POST /api/v1/citas
       │
       ▼
┌──────────────────┐
│ CitaController   │  ← @PostMapping
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│ CitaService      │  ← validateBasic() + create()
│ (validaciones)   │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│ CitaRepository   │  ← JPA save()
│ (Spring Data)    │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│ H2 Database      │  ← persisted Cita
└──────────────────┘
```

## File Changes

| File | Action | Description |
|------|--------|-------------|
| `pom.xml` | Create | Spring Boot starter, H2, Lombok, test deps |
| `src/main/java/com/clinica/MicroservicioClinicaApplication.java` | Create | Spring Boot entry point with `@SpringBootApplication` |
| `src/main/java/com/clinica/model/Cita.java` | Create | JPA entity with all fields |
| `src/main/java/com/clinica/model/enums/EstadoCita.java` | Create | Enum: ACTIVA, CANCELADA, COMPLETADA |
| `src/main/java/com/clinica/repository/CitaRepository.java` | Create | `JpaRepository<Cita, Long>` |
| `src/main/java/com/clinica/service/CitaService.java` | Create | CRUD + validations (horario, slots, días) |
| `src/main/java/com/clinica/controller/CitaController.java` | Create | REST endpoints: POST, GET, GETALL, PUT, DELETE |
| `src/main/java/com/clinica/dto/CitaRequest.java` | Create | Fields: pacienteId, medicoId, fecha, hora, motivoConsulta |
| `src/main/java/com/clinica/dto/CitaResponse.java` | Create | Fields: id, pacienteId, medicoId, fecha, hora, estado, motivoConsulta, fechaCreacion |
| `src/main/java/com/clinica/exception/GlobalExceptionHandler.java` | Create | `@ControllerAdvice`, handles MethodArgumentNotValidException, CitaNotFoundException, BusinessValidationException |
| `src/main/java/com/clinica/exception/CitaNotFoundException.java` | Create | RuntimeException for 404 |
| `src/main/java/com/clinica/exception/BusinessValidationException.java` | Create | RuntimeException for validation errors |
| `src/main/resources/application.properties` | Create | H2 config, JPA settings |
| `src/test/java/com/clinica/service/CitaServiceTest.java` | Create | Unit tests with Mockito, 70%+ coverage target |

**Total: 13 new files, 0 modified, 0 deleted**

## Interfaces / Contracts

### CitaRequest DTO
```java
public record CitaRequest(
    String pacienteId,
    String medicoId,
    LocalDate fecha,
    LocalTime hora,
    String motivoConsulta
) {}
```

### CitaResponse DTO
```java
public record CitaResponse(
    Long id,
    String pacienteId,
    String medicoId,
    LocalDate fecha,
    LocalTime hora,
    EstadoCita estado,
    String motivoConsulta,
    LocalDateTime fechaCreacion
) {}
```

### Controller Endpoints
| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/citas` | Create cita → 201 Created |
| `GET` | `/api/v1/citas/{id}` | Get cita by id → 200 OK or 404 |
| `GET` | `/api/v1/citas` | List all citas → 200 OK |
| `PUT` | `/api/v1/citas/{id}` | Update cita → 200 OK, 409 if not ACTIVA, 404 if not found |
| `DELETE` | `/api/v1/citas/{id}` | Cancel cita (state → CANCELADA) → 204 No Content |

### Exception Responses (GlobalExceptionHandler)
```json
{
  "timestamp": "2026-06-04T...",
  "status": 400,
  "error": "Bad Request",
  "message": "Horario fuera de rango (08:00-17:00)",
  "path": "/api/v1/citas"
}
```

## Testing Strategy

| Layer | What | Approach |
|-------|------|----------|
| Unit | `CitaService` business logic | `@ExtendWith(MockitoExtension.class)` — mock `CitaRepository`, verify interactions and validation calls |
| Unit | Validation rules | Parametrized tests for horario (08:00-17:00), slots (:00/:30), días (L-V) |
| Integration | Full stack | `@SpringBootTest` with `TestRestTemplate` — POST/GET/PUT/DELETE flow against embedded H2 |

**Coverage target**: >70% on `CitaService`

## Migration / Rollback

No migration required. Greenfield project with H2 in-memory database. Rollback: `git clean` and recreate module.

## Open Questions

None — all technical decisions clarified in proposal and specs.