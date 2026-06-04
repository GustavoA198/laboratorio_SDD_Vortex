# Cita Validation Basic Specification

## Purpose

Validaciones de dominio para crear y modificar citas médicas.

## Requirements

### Requirement: Horario Válido

Las citas DEBEN ser creadas únicamente entre las 08:00 y las 17:00 horas.

- GIVEN fecha y hora de cita
- WHEN se intenta crear o actualizar
- THEN hora DEBE estar entre 08:00 y 17:00

#### Scenario: Horario - Within range

- GIVEN hora 09:30
- WHEN se valida horario
- THEN pasa validación

#### Scenario: Horario - Outside range (before 8am)

- GIVEN hora 07:30
- WHEN se valida horario
- THEN falla con mensaje "Horario fuera de rango (08:00-17:00)"

#### Scenario: Horario - Outside range (after 5pm)

- GIVEN hora 17:30
- WHEN se valida horario
- THEN falla con mensaje "Horario fuera de rango (08:00-17:00)"

---

### Requirement: Duración Fija

Cada cita DEBE durar exactamente 30 minutos en slots fijos.

- GIVEN hora de cita
- WHEN se intenta crear o actualizar
- THEN hora DEBE ser múltiplo de 30 minutos desde 08:00

#### Scenario: Duración - Slot válido (08:00)

- GIVEN hora 08:00
- WHEN se valida slot
- THEN pasa validación

#### Scenario: Duración - Slot válido (08:30)

- GIVEN hora 08:30
- WHEN se valida slot
- THEN pasa validación

#### Scenario: Duración - Slot válido (09:00)

- GIVEN hora 09:00
- WHEN se valida slot
- THEN pasa validación

#### Scenario: Duración - Slot inválido (08:15)

- GIVEN hora 08:15
- WHEN se valida slot
- THEN falla con mensaje "Duración debe ser 30 minutos en slots de 30 min"

#### Scenario: Duración - Slot inválido (08:45)

- GIVEN hora 08:45
- WHEN se valida slot
- THEN falla con mensaje "Duración debe ser 30 minutos en slots de 30 min"

---

### Requirement: Días Hábiles

Las citas NO DEBEN ser creadas en fines de semana (sábado o domingo).

- GIVEN fecha de cita
- WHEN se intenta crear o actualizar
- THEN el día DEBE ser lunes, martes, miércoles, jueves o viernes

#### Scenario: Días - Monday-Friday success

- GIVEN fecha lunes 02/06/2026
- WHEN se valida día hábil
- THEN pasa validación

#### Scenario: Días - Saturday

- GIVEN fecha sábado 07/06/2026
- WHEN se valida día hábil
- THEN falla con mensaje "No se permiten citas en fins de semana"

#### Scenario: Días - Sunday

- GIVEN fecha domingo 08/06/2026
- WHEN se valida día hábil
- THEN falla con mensaje "No se permiten citas en fins de semana"