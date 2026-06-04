# Proposal: CHANGE-1 - CRUD Básico de Citas

## Intent
Implementar el CRUD completo de citas médicas (Cita) con validaciones básicas de dominio para el microservicio de clínica. Este es el primer change que establece la base del sistema.

## Scope

### In Scope
- Modelo Cita con enum EstadoCita (ACTIVA, CANCELADA, COMPLETADA)
- Repositorio JPA para Cita
- Servicio CitaService con validaciones básicas
- Controller CitaController con endpoints REST
- DTOs (CitaRequest, CitaResponse)
- Manejo de excepciones (GlobalExceptionHandler)
- Schema H2 creado desde cero
- Tests unitarios para CitaService

### Out of Scope
- Autenticación JWT (Change 3)
- Modelos Paciente y Medico (Change 2)
- Límite de 3 citas activas (Change 4)
- Anti-duplicados (Change 4)
- Rate limiting y audit logs (Change 4)

## Capabilities

### New Capabilities
- `cita-crud`: Operaciones CRUD completas para citas médicas
- `cita-validation-basic`: Validaciones de horario, duración y días hábiles

## Approach
- Spring Boot 4.0.6 con Spring Data JPA
- Arquitectura por capas (Controller → Service → Repository)
- DTOs manuales (sin MapStruct para reducir dependencias)
- Validaciones de negocio en CitaService
- Excepciones custom para errores de negocio

## Affected Areas

| Area | Impact | Description |
|------|--------|-------------|
| `src/main/java/com/clinica/model/Cita.java` | New | Entidad principal |
| `src/main/java/com/clinica/model/enums/EstadoCita.java` | New | Enum de estados |
| `src/main/java/com/clinica/repository/CitaRepository.java` | New | Repositorio JPA |
| `src/main/java/com/clinica/service/CitaService.java` | New | Lógica de negocio |
| `src/main/java/com/clinica/controller/CitaController.java` | New | Endpoints REST |
| `src/main/java/com/clinica/dto/CitaRequest.java` | New | DTO creación |
| `src/main/java/com/clinica/dto/CitaResponse.java` | New | DTO respuesta |
| `src/main/java/com/clinica/exception/*.java` | New | Manejo de errores |
| `src/main/resources/application.properties` | Modified | Config H2 |
| `src/test/java/com/clinica/service/CitaServiceTest.java` | New | Tests unitarios |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| Greenfield sin patrones existentes | Medium | Usar convenciones Spring Boot estándar |
| Dependencias de futuras features | Low | Scope explícitamente definido y deferido |

## Rollback Plan
Eliminar el módulo `com.clinica` y recrear desde cero con Maven.

## Dependencies
Ninguna - es el primer change del proyecto.

## Success Criteria
- [ ] POST /api/v1/citas crea una cita válida
- [ ] GET /api/v1/citas/{id} retorna cita existente
- [ ] PUT /api/v1/citas/{id} actualiza solo si está ACTIVA
- [ ] DELETE /api/v1/citas/{id} cambia estado a CANCELADA
- [ ] Validación horario 08:00-17:00 funciona
- [ ] Validación días hábiles (L-V) funciona
- [ ] Tests unitarios pasan con coverage >70%