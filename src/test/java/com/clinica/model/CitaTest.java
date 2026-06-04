package com.clinica.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Cita entity with Paciente and Medico relationships.
 * Tests the entity fields and relationships.
 */
@DisplayName("Cita Entity with Relationships Tests")
class CitaTest {

    private Paciente createTestPaciente() {
        return Paciente.builder()
                .id("PAC-001")
                .nombre("Juan Pérez")
                .email("juan.perez@email.com")
                .telefono("+54-11-5555-1234")
                .fechaRegistro(LocalDate.of(2025, 1, 15))
                .build();
    }

    private Medico createTestMedico() {
        return Medico.builder()
                .id("MED-001")
                .nombre("Dr. Carlos Rodríguez")
                .especialidad("Cardiología")
                .email("carlos.rodriguez@clinica.com")
                .horarioAtencion("Lunes a Viernes 08:00-17:00")
                .build();
    }

    @Test
    @DisplayName("Should create Cita with @ManyToOne relationships via builder")
    void shouldCreateCitaWithRelationships() {
        // Given
        Paciente paciente = createTestPaciente();
        Medico medico = createTestMedico();

        // When
        Cita cita = Cita.builder()
                .id(1L)
                .paciente(paciente)
                .medico(medico)
                .fecha(LocalDate.of(2025, 3, 20))
                .hora(LocalTime.of(9, 0))
                .estado(com.clinica.model.enums.EstadoCita.ACTIVA)
                .motivoConsulta("Consulta cardiológica")
                .fechaCreacion(LocalDateTime.now())
                .build();

        // Then
        assertEquals(1L, cita.getId());
        assertNotNull(cita.getPaciente());
        assertEquals("PAC-001", cita.getPaciente().getId());
        assertEquals("Juan Pérez", cita.getPaciente().getNombre());
        assertNotNull(cita.getMedico());
        assertEquals("MED-001", cita.getMedico().getId());
        assertEquals("Dr. Carlos Rodríguez", cita.getMedico().getNombre());
        assertEquals(LocalDate.of(2025, 3, 20), cita.getFecha());
        assertEquals(LocalTime.of(9, 0), cita.getHora());
        assertEquals(com.clinica.model.enums.EstadoCita.ACTIVA, cita.getEstado());
        assertEquals("Consulta cardiológica", cita.getMotivoConsulta());
    }

    @Test
    @DisplayName("Should allow no-args construction")
    void shouldAllowNoArgsConstruction() {
        // When
        Cita cita = new Cita();

        // Then
        assertNotNull(cita);
    }

    @Test
    @DisplayName("Should allow setting paciente and medico via setters")
    void shouldAllowSettingRelationsViaSetters() {
        // Given
        Cita cita = new Cita();
        Paciente paciente = createTestPaciente();
        Medico medico = createTestMedico();

        // When
        cita.setPaciente(paciente);
        cita.setMedico(medico);

        // Then
        assertNotNull(cita.getPaciente());
        assertEquals("PAC-001", cita.getPaciente().getId());
        assertNotNull(cita.getMedico());
        assertEquals("MED-001", cita.getMedico().getId());
    }

    @Test
    @DisplayName("Should maintain estado as ACTIVA on @PrePersist when not set")
    void shouldSetDefaultEstadoOnPrePersist() {
        // Given
        Cita cita = Cita.builder()
                .paciente(createTestPaciente())
                .medico(createTestMedico())
                .fecha(LocalDate.of(2025, 3, 20))
                .hora(LocalTime.of(9, 0))
                .build();

        // Simulate @PrePersist
        cita.onCreate();

        // Then
        assertEquals(com.clinica.model.enums.EstadoCita.ACTIVA, cita.getEstado());
    }

    @Test
    @DisplayName("Should set fechaCreacion on @PrePersist when not set")
    void shouldSetFechaCreacionOnPrePersist() {
        // Given
        Cita cita = Cita.builder()
                .paciente(createTestPaciente())
                .medico(createTestMedico())
                .fecha(LocalDate.of(2025, 3, 20))
                .hora(LocalTime.of(9, 0))
                .estado(com.clinica.model.enums.EstadoCita.ACTIVA)
                .build();

        // When
        cita.onCreate();

        // Then
        assertNotNull(cita.getFechaCreacion());
    }
}
