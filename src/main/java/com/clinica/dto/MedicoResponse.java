package com.clinica.dto;

/**
 * Response DTO for doctor (Medico) data.
 *
 * @param id unique identifier
 * @param nombre doctor full name
 * @param especialidad medical specialty
 * @param email doctor email address
 * @param horarioAtencion office hours description
 */
public record MedicoResponse(
    String id,
    String nombre,
    String especialidad,
    String email,
    String horarioAtencion
) {}
