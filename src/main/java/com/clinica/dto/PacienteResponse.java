package com.clinica.dto;

import java.time.LocalDate;

/**
 * Response DTO for patient (Paciente) data.
 *
 * @param id unique identifier
 * @param nombre patient full name
 * @param email patient email address
 * @param telefono patient phone number
 * @param fechaRegistro registration date
 */
public record PacienteResponse(
    String id,
    String nombre,
    String email,
    String telefono,
    LocalDate fechaRegistro
) {}
