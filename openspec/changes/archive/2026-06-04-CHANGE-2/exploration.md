# Exploration: CHANGE-2 — Gestión de Usuarios

## Current State

### Existing from CHANGE-1 (archived)

**Cita model** already has `pacienteId` and `medicoId` as plain `String` columns (no FK constraints):

```java
@Column(name = "paciente_id", nullable = false)
private String pacienteId;

@Column(name = "medico_id", nullable = false)
private String medicoId;
```

**Existing endpoints:**
- `POST /api/v1/citas` — Crear cita (requires pacienteId, medicoId in body)
- `GET /api/v1/citas/{id}` — Consultar cita
- `GET /api/v1/citas` — Listar todas las citas
- `PUT /api/v1/citas/{id}` — Actualizar cita
- `DELETE /api/v1/citas/{id}` — Cancelar cita

**Pattern in use:**
- Records for DTOs (CitaRequest, CitaResponse)
- Lombok @Builder on entity
- Service layer with domain validations
- JpaRepository for persistence
- Business validation exceptions

### From sdd-init context (planned entities)

The init phase anticipated these entities:
- **Paciente**: id (String), nombre (String), email (String), telefono (String), fechaRegistro (LocalDate)
- **Medico**: id (String), nombre (String), especialidad (String), email (String), horarioAtencion (String)

Business rules that need these entities:
1. "Paciente max 3 citas activas simultáneas"
2. "Solo rol PACIENTE puede crear citas"
3. "Paciente solo ve/cancela sus propias citas"

## Affected Areas

| File | Why Affected |
|------|-------------|
| `src/main/java/com/clinica/model/Paciente.java` | **NEW** — Paciente entity |
| `src/main/java/com/clinica/model/Medico.java` | **NEW** — Medico entity |
| `src/main/java/com/clinica/repository/PacienteRepository.java` | **NEW** — Paciente JPA repo |
| `src/main/java/com/clinica/repository/MedicoRepository.java` | **NEW** — Medico JPA repo |
| `src/main/java/com/clinica/controller/PacienteController.java` | **NEW** — GET /pacientes/{id} |
| `src/main/java/com/clinica/controller/MedicoController.java` | **NEW** — GET /medicos/{id} |
| `src/main/java/com/clinica/service/CitaService.java` | **MODIFY** — add findByPacienteId, findByMedicoId |
| `src/main/java/com/clinica/repository/CitaRepository.java` | **MODIFY** — add custom queries |
| `src/main/java/com/clinica/dto/PacienteResponse.java` | **NEW** — Paciente DTO |
| `src/main/java/com/clinica/dto/MedicoResponse.java` | **NEW** — Medico DTO |

## Approaches

### Approach A: Loose Coupling (String IDs only — like today)

Keep `pacienteId` and `medicoId` as `String` in Cita. Create Paciente/Medico models purely for response enrichment (GET /pacientes/{id}, GET /medicos/{id}), but **no FK constraints**, **no referential integrity**.

| Pros | Cons |
|------|------|
| Minimal changes to Cita model | No referential integrity (can create cita with non-existent pacienteId) |
| Simple migration path | Harder to enforce "3 citas max" rule without querying all citas |
| Fast to implement | Paciente/Medico become "stub" entities with no real use |
| No breaking changes | defeats the purpose of user management |

**Effort**: Low | **Recommendation**: ❌ No — defeats the purpose of CHANGE-2

---

### Approach B: Full JPA Entities with Hard FK (RECOMMENDED)

Create proper `Paciente` and `Medico` JPA entities. Add `@ManyToOne` from `Cita` to both. Add FK constraints at DB level.

| Pros | Cons |
|------|------|
| Referential integrity enforced | Requires migration strategy for existing data |
| Enables true "3 citas max" enforcement | More complex than Approach A |
| Natural join queries for mis-citas | FK constraints add coupling |
| Matches what sdd-init originally envisioned | Requires more test coverage |

**Effort**: Medium | **Recommendation**: ✅ Yes — aligns with domain model intent

---

### Approach C: JPA Entities with Soft References (Middle Ground)

Create Paciente/Medico entities but use `String` IDs in Cita (no `@ManyToOne`). Validate existence via service layer when creating/updating Cita.

| Pros | Cons |
|------|------|
| Referential integrity via application logic | No DB-level FK constraint |
| Easier to query "mis-citas" via service | Still can't use JPA relationships |
| Simpler migration | Same as Approach A for queries |

**Effort**: Medium | **Recommendation**: ❌ No — same limitations, more code

## Recommendation

**Approach B — Full JPA Entities with Hard FK**

Rationale:
1. The sdd-init context explicitly planned Paciente and Medico as entities (not just IDs)
2. Business rule "Paciente max 3 citas activas" is impossible to enforce cleanly without entity relationship
3. CHANGE-3 (Seguridad JWT) will need real user entities for role-based access
4. The existing Cita model already has `pacienteId`/`medicoId` columns — we just need to migrate them to FK

### Architecture for new models:

```
@Entity
@Table(name = "pacientes")
class Paciente {
    @Id String id;        // matches String pacienteId in Cita
    String nombre;
    String email;
    String telefono;
    LocalDate fechaRegistro;
}

@Entity
@Table(name = "medicos")
class Medico {
    @Id String id;        // matches String medicoId in Cita
    String nombre;
    String especialidad;
    String email;
    String horarioAtencion;
}

@Entity
class Cita {
    @ManyToOne @JoinColumn(name = "paciente_id") Paciente paciente;
    @ManyToOne @JoinColumn(name = "medico_id") Medico medico;
    ...
}
```

### Controller layer for /mis-citas:

For now (no JWT yet), use `pacienteId` as request parameter:
```
GET /api/v1/citas/mis-citas?pacienteId={id}
```

Future (CHANGE-3): replace with JWT extraction from Authorization header.

## Risks

1. **Data migration**: Existing Cita rows have `paciente_id`/`medico_id` strings — need FK constraint migration
2. **Cascade delete**: If a Medico is deleted, what happens to their Citas? (Needs policy decision)
3. **No authentication yet**: `/mis-citas?pacienteId=X` is insecure — document as known limitation until CHANGE-3
4. **Circular FK potential**: If we later add "last appointment" field to Paciente → avoid it

## Ready for Proposal

**Yes** — structured analysis complete.

The change should proceed with:
1. Create Paciente and Medico JPA entities
2. Add `@ManyToOne` relationships from Cita to Paciente/Medico
3. Add custom queries to CitaRepository (`findByPacienteId`, `findByMedicoId`)
4. Create PacienteController and MedicoController with GET endpoints
5. Add `/mis-citas` endpoint to CitaController with `pacienteId` query param
6. Document limitation: no auth until CHANGE-3