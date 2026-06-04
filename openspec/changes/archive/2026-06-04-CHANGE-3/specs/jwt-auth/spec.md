# jwt-auth Specification

## Purpose

JWT-based authentication for the clinic microservice. Provides login endpoint and token validation.

## Requirements

### Requirement: Login Endpoint

The system MUST expose `POST /api/v1/auth/login` that accepts JSON `{username, password}` and returns a JWT token on success.

### Requirement: Token Generation

The system SHALL generate JWT tokens containing `sub` (userId), `rol` (PACIENTE|MEDICO|ADMIN), `iat`, and `exp` claims. Tokens MUST expire after 20 minutes.

### Requirement: Token Validation

The system SHALL validate tokens on every protected request by extracting the `Authorization: Bearer <token>` header, parsing the JWT, and rejecting expired or malformed tokens with 401.

## Scenarios

#### Scenario: Login Success

- GIVEN a user with username "gustavo" and password "1234" exists in the system
- WHEN the client sends `POST /api/v1/auth/login` with `{"username": "gustavo", "password": "1234"}`
- THEN the response status is 200
- AND the response body contains `{"token": "<jwt>", "expiresIn": 1200}`

#### Scenario: Login Failure — Invalid Credentials

- GIVEN no user with username "unknown" exists
- WHEN the client sends `POST /api/v1/auth/login` with `{"username": "unknown", "password": "wrong"}`
- THEN the response status is 401

#### Scenario: Token Valid — Access Granted

- GIVEN a valid JWT token for user "gustavo" with rol "PACIENTE"
- WHEN the client sends a request to a protected endpoint with header `Authorization: Bearer <token>`
- THEN the response status is 200 (or appropriate success for the endpoint)

#### Scenario: Token Invalid/Missing — Access Denied

- GIVEN no token or an invalid token
- WHEN the client sends a request to a protected endpoint without `Authorization` header or with invalid Bearer
- THEN the response status is 401