package com.clinica.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Request DTO for creating or updating a medical appointment (Cita).
 *
 * @param pacienteId unique identifier of the patient
 * @param medicoId unique identifier of the medical doctor
 * @param fecha appointment date (must be Monday-Friday)
 * @param hora appointment time (must be between 08:00-17:00 in :00 or :30 slots)
 * @param motivoConsulta reason for the consultation (optional)
 */
public record CitaRequest(
    @NotBlank(message = "pacienteId es requerido")
    String pacienteId,

    @NotBlank(message = "medicoId es requerido")
    String medicoId,

    @NotNull(message = "fecha es requerida")
    LocalDate fecha,

    @NotNull(message = "hora es requerida")
    LocalTime hora,

    String motivoConsulta
) {}