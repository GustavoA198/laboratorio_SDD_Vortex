package com.clinica.exception;

/**
 * Exception thrown when a requested Cita (appointment) is not found.
 * Returns HTTP 404 Not Found.
 */
public class CitaNotFoundException extends RuntimeException {

    public CitaNotFoundException(Long id) {
        super("Cita no encontrada con id: " + id);
    }

    public CitaNotFoundException(String message) {
        super(message);
    }
}