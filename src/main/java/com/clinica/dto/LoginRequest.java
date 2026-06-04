package com.clinica.dto;

/**
 * Request DTO for login.
 */
public record LoginRequest(
        String username,
        String password
) {}