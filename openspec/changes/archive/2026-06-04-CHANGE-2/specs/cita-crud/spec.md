# Delta for Cita CRUD

## MODIFIED Requirements

### Requirement: Crear Cita

El sistema DEBE crear una nueva cita cuando se reciben datos válidos Y los IDs de paciente y médico existen como entidades.

(Previously: solo validaba datos de entrada sin verificar existencia de FK)

#### Scenario: Crear - Success

- GIVEN datos válidos de cita Y pacienteId existe Y medicoId existe
- WHEN se envía POST /api/v1/citas
- THEN 返回 201 con cita creada en estado ACTIVA

#### Scenario: Crear - Paciente no existe

- GIVEN pacienteId no existe en base de datos
- WHEN se envía POST /api/v1/citas con ese pacienteId
- THEN 返回 400 Bad Request con mensaje de error

#### Scenario: Crear - Médico no existe

- GIVEN medicoId no existe en base de datos
- WHEN se envía POST /api/v1/citas con ese medicoId
- THEN 返回 400 Bad Request con mensaje de error

#### Scenario: Crear - Datos inválidos

- GIVEN datos incompletos (falta pacienteId)
- WHEN se envía POST /api/v1/citas
- THEN 返回 400 Bad Request

#### Scenario: Crear - Campos requeridos faltantes

- GIVEN request sin fecha
- WHEN se envía POST /api/v1/citas
- THEN 返回 400 con mensaje de validación