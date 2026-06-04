package com.clinica.service;

import com.clinica.model.Paciente;
import com.clinica.repository.PacienteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PacienteService.
 * Tests service methods with Mockito mocking.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PacienteService Tests")
class PacienteServiceTest {

    @Mock
    private PacienteRepository pacienteRepository;

    @InjectMocks
    private PacienteService pacienteService;

    private Paciente createTestPaciente() {
        return Paciente.builder()
                .id("PAC-001")
                .nombre("Juan Pérez")
                .email("juan.perez@email.com")
                .telefono("+54-11-5555-1234")
                .fechaRegistro(LocalDate.of(2025, 1, 15))
                .build();
    }

    @Nested
    @DisplayName("getPaciente Tests")
    class GetPacienteTests {

        @Test
        @DisplayName("Should return Paciente when exists")
        void shouldReturnPacienteWhenExists() {
            // Given
            Paciente expected = createTestPaciente();
            when(pacienteRepository.findById("PAC-001")).thenReturn(Optional.of(expected));

            // When
            Optional<Paciente> result = pacienteService.getPaciente("PAC-001");

            // Then
            assertTrue(result.isPresent());
            assertEquals("PAC-001", result.get().getId());
            assertEquals("Juan Pérez", result.get().getNombre());
            verify(pacienteRepository, times(1)).findById("PAC-001");
        }

        @Test
        @DisplayName("Should return empty Optional when not exists")
        void shouldReturnEmptyWhenNotExists() {
            // Given
            when(pacienteRepository.findById("UNKNOWN")).thenReturn(Optional.empty());

            // When
            Optional<Paciente> result = pacienteService.getPaciente("UNKNOWN");

            // Then
            assertTrue(result.isEmpty());
            verify(pacienteRepository, times(1)).findById("UNKNOWN");
        }
    }

    @Nested
    @DisplayName("existsById Tests")
    class ExistsByIdTests {

        @Test
        @DisplayName("Should return true when Paciente exists")
        void shouldReturnTrueWhenExists() {
            // Given
            when(pacienteRepository.existsById("PAC-001")).thenReturn(true);

            // When
            boolean result = pacienteService.existsById("PAC-001");

            // Then
            assertTrue(result);
            verify(pacienteRepository, times(1)).existsById("PAC-001");
        }

        @Test
        @DisplayName("Should return false when Paciente does not exist")
        void shouldReturnFalseWhenNotExists() {
            // Given
            when(pacienteRepository.existsById("UNKNOWN")).thenReturn(false);

            // When
            boolean result = pacienteService.existsById("UNKNOWN");

            // Then
            assertFalse(result);
            verify(pacienteRepository, times(1)).existsById("UNKNOWN");
        }
    }
}
