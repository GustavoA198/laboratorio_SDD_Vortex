# Validacion Citas Specification

## Purpose

Advanced validations for medical appointments: max active citas per patient and duplicate prevention.

## Requirements

### Requirement: Max 3 Active Citas

The system MUST reject creation of a new cita when the patient already has 3 or more active citas.

- GIVEN a patient with 3 existing citas in ACTIVA state
- WHEN a new cita creation is attempted for the same patient
- THEN the system MUST return 400 Bad Request with message "Patient has exceeded maximum active citas limit (3)"

#### Scenario: Under Limit - Success

- GIVEN patient has 2 active citas
- WHEN a new cita creation is attempted
- THEN cita is created successfully with 201 response

#### Scenario: At Limit - Rejected

- GIVEN patient has 3 active citas
- WHEN a new cita creation is attempted
- THEN returns 400 with message "Patient has exceeded maximum active citas limit (3)"

#### Scenario: At Limit - With Cancelled

- GIVEN patient has 2 active citas and 1 cancelled cita
- WHEN a new cita creation is attempted
- THEN cita is created successfully (only ACTIVA count matters)

---

### Requirement: Anti-Duplicate Cita

The system MUST reject creation of a cita when an identical one exists (same medico + fecha + hora).

- GIVEN a cita exists with same medicoId, fecha, and hora
- WHEN a new cita creation is attempted with identical parameters
- THEN the system MUST return 409 Conflict with message "Duplicate cita: same medico, date, and time already exist"

#### Scenario: No Duplicate - Success

- GIVEN no existing cita with same medicoId + fecha + hora
- WHEN a new cita creation is attempted
- THEN cita is created successfully with 201 response

#### Scenario: Duplicate - Same Medico Date Time

- GIVEN an existing cita with medicoId=5, fecha="2026-06-10", hora="09:00"
- WHEN a new cita creation is attempted with medicoId=5, fecha="2026-06-10", hora="09:00"
- THEN returns 409 Conflict with message "Duplicate cita: same medico, date, and time already exist"

#### Scenario: Different Time - Allowed

- GIVEN an existing cita with medicoId=5, fecha="2026-06-10", hora="09:00"
- WHEN a new cita creation is attempted with medicoId=5, fecha="2026-06-10", hora="10:00"
- THEN cita is created successfully

#### Scenario: Different Medico - Allowed

- GIVEN an existing cita with medicoId=5, fecha="2026-06-10", hora="09:00"
- WHEN a new cita creation is attempted with medicoId=6, fecha="2026-06-10", hora="09:00"
- THEN cita is created successfully