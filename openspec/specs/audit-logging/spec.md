# Audit Logging Specification

## Purpose

Record all operations in the system for accountability and traceability.

## Requirements

### Requirement: Operation Logging

The system MUST log every operation with timestamp, username, and operation details.

- GIVEN any API operation is performed
- WHEN the operation completes (success or failure)
- THEN the system MUST write a log entry containing timestamp, username, operation name, and outcome

#### Scenario: Successful Operation Logged

- GIVEN authenticated user "dr.smith" performs POST /api/v1/citas
- WHEN the operation completes successfully with 201
- THEN audit log entry is written with: timestamp, username="dr.smith", operation="CREATE_CITA", status="SUCCESS"

#### Scenario: Failed Operation Logged

- GIVEN authenticated user "dr.smith" attempts POST /api/v1/citas with invalid data
- WHEN the operation fails with 400
- THEN audit log entry is written with: timestamp, username="dr.smith", operation="CREATE_CITA", status="FAILURE"

#### Scenario: Authenticated User Required

- GIVEN a request is made without authentication
- WHEN any operation is attempted
- THEN no audit log entry is created (unauthenticated requests are handled by auth filter)

---

### Requirement: Log Entry Format

The system MUST write audit logs in a structured format suitable for analysis.

- GIVEN an operation to log
- WHEN the log entry is written
- THEN it MUST contain: ISO-8601 timestamp, username (or "ANONYMOUS"), operation identifier, HTTP method, endpoint path, response status code, duration in milliseconds

#### Scenario: Log Entry Contains All Fields

- GIVEN user "admin" calls DELETE /api/v1/citas/5 which returns 204
- WHEN audit log is written
- THEN entry contains: {"timestamp": "2026-06-04T10:30:00Z", "username": "admin", "operation": "DELETE_CITA", "method": "DELETE", "path": "/api/v1/citas/5", "status": 204, "duration_ms": 45}