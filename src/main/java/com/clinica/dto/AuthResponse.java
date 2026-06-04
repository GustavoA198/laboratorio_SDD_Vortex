package com.clinica.dto;

/**
 * Response DTO for authentication.
 */
public record AuthResponse(
        String token,
        long expiresIn
) {}