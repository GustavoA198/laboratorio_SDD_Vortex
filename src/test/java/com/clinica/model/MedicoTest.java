package com.clinica.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Medico entity.
 * Tests the entity fields and builder pattern.
 */
@DisplayName("Medico Entity Tests")
class MedicoTest {

    @Test
    @DisplayName("Should create Medico with all fields via builder")
    void shouldCreateMedicoWithAllFields() {
        // When
        Medico medico = Medico.builder()
                .id("MED-001")
                .nombre("Dr. Carlos Rodríguez")
                .especialidad("Cardiología")
                .email("carlos.rodriguez@clinica.com")
                .horarioAtencion("Lunes a Viernes 08:00-17:00")
                .build();

        // Then
        assertEquals("MED-001", medico.getId());
        assertEquals("Dr. Carlos Rodríguez", medico.getNombre());
        assertEquals("Cardiología", medico.getEspecialidad());
        assertEquals("carlos.rodriguez@clinica.com", medico.getEmail());
        assertEquals("Lunes a Viernes 08:00-17:00", medico.getHorarioAtencion());
    }

    @Test
    @DisplayName("Should allow no-args construction")
    void shouldAllowNoArgsConstruction() {
        // When
        Medico medico = new Medico();

        // Then
        assertNotNull(medico);
    }

    @Test
    @DisplayName("Should allow setters for all fields")
    void shouldAllowSetters() {
        // Given
        Medico medico = new Medico();

        // When
        medico.setId("MED-002");
        medico.setNombre("Dra. Ana Martínez");
        medico.setEspecialidad("Dermatología");
        medico.setEmail("ana.martinez@clinica.com");
        medico.setHorarioAtencion("Lunes a Jueves 09:00-16:00");

        // Then
        assertEquals("MED-002", medico.getId());
        assertEquals("Dra. Ana Martínez", medico.getNombre());
        assertEquals("Dermatología", medico.getEspecialidad());
        assertEquals("ana.martinez@clinica.com", medico.getEmail());
        assertEquals("Lunes a Jueves 09:00-16:00", medico.getHorarioAtencion());
    }
}
