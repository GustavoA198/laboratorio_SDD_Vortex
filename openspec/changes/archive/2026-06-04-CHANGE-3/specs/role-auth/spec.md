# role-auth Specification

## Purpose

Role-based authorization for the clinic microservice. Protects endpoints by requiring specific roles via `@Secured` annotations and validates ownership for resource access.

## Requirements

### Requirement: Role-Based Endpoint Protection

The system MUST enforce `@Secured({"ROL"})` annotations on all controllers. Requests without the required role SHALL receive 403 Forbidden.

### Requirement: PACIENTE Resource Ownership

The system SHALL restrict PACIENTE users to viewing and managing only their own resources. A PACIENTE attempting to access another user's data MUST receive 403.

### Requirement: MEDICO Resource Access

The system SHALL allow MEDICO users to view all citas assigned to them, but not citas of other medicos.

### Requirement: ADMIN Resource Access

The system SHALL allow ADMIN users unrestricted access to all resources without ownership checks.

## Scenarios

#### Scenario: PACIENTE Can Create Cita

- GIVEN a PACIENTE is authenticated with valid JWT
- WHEN the PACIENTE sends `POST /api/v1/citas` with their own data
- THEN the response status is 201

#### Scenario: PACIENTE Can View Own Cita

- GIVEN a PACIENTE owns cita with id 5
- WHEN the PACIENTE sends `GET /api/v1/citas/5`
- THEN the response status is 200

#### Scenario: PACIENTE Cannot View Another Patient's Cita

- GIVEN a PACIENTE does not own cita with id 99
- WHEN the PACIENTE sends `GET /api/v1/citas/99`
- THEN the response status is 403

#### Scenario: PACIENTE Can Cancel Own Cita

- GIVEN a PACIENTE owns cita with id 5
- WHEN the PACIENTE sends `DELETE /api/v1/citas/5`
- THEN the response status is 204

#### Scenario: MEDICO Can View All Citas by Medico

- GIVEN a MEDICO is authenticated with valid JWT
- WHEN the MEDICO sends `GET /api/v1/citas?medicoId=<their-id>`
- THEN the response contains all citas for that MEDICO

#### Scenario: ADMIN Can View Any Cita

- GIVEN an ADMIN is authenticated with valid JWT
- WHEN the ADMIN sends `GET /api/v1/citas/99` (any cita)
- THEN the response status is 200

#### Scenario: Unauthorized Role — 403 Forbidden

- GIVEN a PACIENTE attempts to access a MEDICO-only endpoint
- WHEN the PACIENTE sends a request to that endpoint
- THEN the response status is 403