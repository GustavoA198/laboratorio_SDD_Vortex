package com.clinica.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Paciente entity.
 * Tests the entity fields and builder pattern.
 */
@DisplayName("Paciente Entity Tests")
class PacienteTest {

    @Test
    @DisplayName("Should create Paciente with all fields via builder")
    void shouldCreatePacienteWithAllFields() {
        // Given
        LocalDate fechaRegistro = LocalDate.of(2025, 1, 15);

        // When
        Paciente paciente = Paciente.builder()
                .id("PAC-001")
                .nombre("Juan Pérez")
                .email("juan.perez@email.com")
                .telefono("+54-11-5555-1234")
                .fechaRegistro(fechaRegistro)
                .build();

        // Then
        assertEquals("PAC-001", paciente.getId());
        assertEquals("Juan Pérez", paciente.getNombre());
        assertEquals("juan.perez@email.com", paciente.getEmail());
        assertEquals("+54-11-5555-1234", paciente.getTelefono());
        assertEquals(fechaRegistro, paciente.getFechaRegistro());
    }

    @Test
    @DisplayName("Should allow no-args construction")
    void shouldAllowNoArgsConstruction() {
        // When
        Paciente paciente = new Paciente();

        // Then
        assertNotNull(paciente);
    }

    @Test
    @DisplayName("Should allow setters for all fields")
    void shouldAllowSetters() {
        // Given
        Paciente paciente = new Paciente();
        LocalDate fechaRegistro = LocalDate.of(2025, 2, 20);

        // When
        paciente.setId("PAC-002");
        paciente.setNombre("María García");
        paciente.setEmail("maria.garcia@email.com");
        paciente.setTelefono("+54-11-6666-5678");
        paciente.setFechaRegistro(fechaRegistro);

        // Then
        assertEquals("PAC-002", paciente.getId());
        assertEquals("María García", paciente.getNombre());
        assertEquals("maria.garcia@email.com", paciente.getEmail());
        assertEquals("+54-11-6666-5678", paciente.getTelefono());
        assertEquals(fechaRegistro, paciente.getFechaRegistro());
    }
}
