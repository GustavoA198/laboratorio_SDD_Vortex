package com.clinica.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Response DTO for medical appointment (Cita) data.
 *
 * @param id unique identifier
 * @param pacienteId patient identifier
 * @param medicoId doctor identifier
 * @param fecha appointment date
 * @param hora appointment time
 * @param estado current state (ACTIVA, CANCELADA, COMPLETADA)
 * @param motivoConsulta reason for consultation
 * @param fechaCreacion creation timestamp
 */
public record CitaResponse(
    Long id,
    String pacienteId,
    String medicoId,
    LocalDate fecha,
    LocalTime hora,
    String estado,
    String motivoConsulta,
    LocalDateTime fechaCreacion
) {}