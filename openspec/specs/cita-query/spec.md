# Cita Query Specification

## Purpose

Consultar citas filtradas por paciente o médico.

## Requirements

### Requirement: Mis Citas

El sistema DEBE retornar lista de citas de un paciente específico.

- GIVEN un paciente existe con ID conocido
- WHEN GET /api/v1/citas/mis-citas?pacienteId={id} es llamado
- THEN respuesta 200 OK con lista de citas del paciente

#### Scenario: Con citas

- GIVEN paciente "P001" tiene 2 citas activas
- WHEN GET /api/v1/citas/mis-citas?pacienteId=P001
- THEN 返回 200 con array de 2 citas

#### Scenario: Sin citas

- GIVEN paciente "P002" no tiene citas
- WHEN GET /api/v1/citas/mis-citas?pacienteId=P002
- THEN 返回 200 con array vacío []

---

### Requirement: Citas por Médico

El sistema DEBE retornar lista de citas de un médico específico.

- GIVEN un médico existe con ID conocido
- WHEN GET /api/v1/citas/medico/{medicoId} es llamado
- THEN respuesta 200 OK con lista de citas del médico

#### Scenario: Con citas

- GIVEN médico "M001" tiene 3 citas
- WHEN GET /api/v1/citas/medico/M001
- THEN 返回 200 con array de 3 citas

#### Scenario: Sin citas

- GIVEN médico "M002" no tiene citas asignadas
- WHEN GET /api/v1/citas/medico/M002
- THEN 返回 200 con array vacío []