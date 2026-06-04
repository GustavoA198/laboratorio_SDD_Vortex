# Usuario Management Specification

## Purpose

Gestionar las entidades Paciente y Medico, permitiendo consulta de datos por ID.

## Requirements

### Requirement: Consultar Paciente

El sistema DEBE retornar los datos de un paciente existente dado su ID.

- GIVEN un paciente existe con ID conocido
- WHEN GET /api/v1/pacientes/{id} es llamado
- THEN respuesta 200 OK con datos completos del paciente

#### Scenario: Consultar - Existe

- GIVEN paciente existe en base de datos con ID "P001"
- WHEN GET /api/v1/pacientes/P001
- THEN 返回 200 con {id, nombre, email, telefono, fechaRegistro}

#### Scenario: Consultar - No existe

- GIVEN no existe paciente con ID dado
- WHEN GET /api/v1/pacientes/INVALID
- THEN 返回 404 Not Found

---

### Requirement: Consultar Médico

El sistema DEBE retornar los datos de un médico existente dado su ID.

- GIVEN un médico existe con ID conocido
- WHEN GET /api/v1/medicos/{id} es llamado
- THEN respuesta 200 OK con datos completos del médico

#### Scenario: Consultar - Existe

- GIVEN médico existe en base de datos con ID "M001"
- WHEN GET /api/v1/medicos/M001
- THEN 返回 200 con {id, nombre, especialidad, email, horarioAtencion}

#### Scenario: Consultar - No existe

- GIVEN no existe médico con ID dado
- WHEN GET /api/v1/medicos/INVALID
- THEN 返回 404 Not Found