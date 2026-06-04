# Cita CRUD Specification

## Purpose

Gestionar citas médicas con operaciones CRUD y control de estados.

## Requirements

### Requirement: Crear Cita

El sistema DEBE crear una nueva cita cuando se reciben datos válidos Y los IDs de paciente y médico existen como entidades.

- GIVEN datos de cita válidos (pacienteId, medicoId, fecha, hora, duracion) Y pacienteId existe Y medicoId existe
- WHEN POST /api/v1/citas es llamado
- THEN respuesta 201 Created con CitaResponse
- AND cita queda en estado ACTIVA

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

---

### Requirement: Consultar Cita

El sistema DEBE retornar una cita existente por su ID.

- GIVEN una cita existe con ID conocido
- WHEN GET /api/v1/citas/{id} es llamado
- THEN respuesta 200 OK con CitaResponse completo

#### Scenario: Consultar - Existe

- GIVEN cita existe en base de datos
- WHEN GET /api/v1/citas/1
- THEN 返回 200 con datos completos

#### Scenario: Consultar - No existe

- GIVEN no existe cita con ID dado
- WHEN GET /api/v1/citas/999
- THEN 返回 404 Not Found

---

### Requirement: Listar Citas

El sistema DEBE retornar lista de citas.

- GIVEN hay cero o más citas en sistema
- WHEN GET /api/v1/citas es llamado
- THEN respuesta 200 OK con lista (vacía o con elementos)

#### Scenario: Listar - Vacía inicialmente

- GIVEN no hay citas creadas
- WHEN GET /api/v1/citas
- THEN 返回 200 con array vacío []

---

### Requirement: Actualizar Cita

El sistema DEBE actualizar una cita solo si está en estado ACTIVA.

- GIVEN una cita existe
- WHEN PUT /api/v1/citas/{id} es llamado
- THEN si estado es ACTIVA: actualización exitosa
- AND si estado NO es ACTIVA: respuesta 409 Conflict

#### Scenario: Actualizar - Success (ACTIVA)

- GIVEN cita existe y está en estado ACTIVA
- WHEN PUT /api/v1/citas/1 con nuevos datos
- THEN 返回 200 con datos actualizados

#### Scenario: Actualizar - Conflicto (no ACTIVA)

- GIVEN cita existe pero estado es CANCELADA
- WHEN PUT /api/v1/citas/1
- THEN 返回 409 Conflict

#### Scenario: Actualizar - No existe

- GIVEN no existe cita con ID dado
- WHEN PUT /api/v1/citas/999
- THEN 返回 404 Not Found

---

### Requirement: Cancelar Cita

El sistema DEBE cambiar estado a CANCELADA sin eliminar el registro.

- GIVEN una cita existe
- WHEN DELETE /api/v1/citas/{id} es llamado
- THEN estado cambia a CANCELADA
- AND retorna 204 No Content

#### Scenario: Cancelar - Success

- GIVEN cita existe
- WHEN DELETE /api/v1/citas/1
- THEN estado cambia a CANCELADA y 返回 204

#### Scenario: Cancelar - No existe

- GIVEN no existe cita con ID
- WHEN DELETE /api/v1/citas/999
- THEN 返回 404 Not Found