# Rate Limiting Specification

## Purpose

Protect the API from excessive usage by limiting requests per user to a defined threshold.

## Requirements

### Requirement: Request Limit

The system MUST allow a maximum of 10 requests per minute per authenticated user.

- GIVEN an authenticated user
- WHEN requests are made within a 60-second window
- THEN the system MUST allow up to 10 requests
- AND MUST reject request #11 with 429 status

#### Scenario: Within Limit - Success

- GIVEN authenticated user with 5 requests in the last minute
- WHEN a new request is made
- THEN request is processed successfully with normal response

#### Scenario: At Limit - Rejected

- GIVEN authenticated user with 10 requests already made in the current minute
- WHEN an additional request is made
- THEN returns 429 Too Many Requests with message "Rate limit exceeded. Maximum 10 requests per minute allowed."

#### Scenario: Window Reset

- GIVEN authenticated user has made 10 requests
- AND 60 seconds have passed since the first request
- WHEN a new request is made
- THEN request is processed successfully (counter reset)

---

### Requirement: Rate Limit Response Format

The system MUST return a proper rate limit error response.

- GIVEN a request that exceeds the rate limit
- WHEN the system responds with 429
- THEN the response body MUST contain JSON with "error" field and "message" field

#### Scenario: 429 Response Structure

- GIVEN user has exceeded rate limit
- WHEN a request is made
- THEN returns 429 with JSON body: {"error": "RATE_LIMIT_EXCEEDED", "message": "Rate limit exceeded. Maximum 10 requests per minute allowed."}