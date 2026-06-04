package com.clinica.model.enums;

/**
 * Represents the possible states of a medical appointment (Cita).
 * State transitions: ACTIVA → CANCELADA or COMPLETADA (no backwards transitions).
 */
public enum EstadoCita {
    ACTIVA,
    CANCELADA,
    COMPLETADA
}