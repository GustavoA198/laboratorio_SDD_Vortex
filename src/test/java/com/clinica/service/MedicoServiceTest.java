package com.clinica.service;

import com.clinica.model.Medico;
import com.clinica.repository.MedicoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MedicoService.
 * Tests service methods with Mockito mocking.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MedicoService Tests")
class MedicoServiceTest {

    @Mock
    private MedicoRepository medicoRepository;

    @InjectMocks
    private MedicoService medicoService;

    private Medico createTestMedico() {
        return Medico.builder()
                .id("MED-001")
                .nombre("Dr. Carlos Rodríguez")
                .especialidad("Cardiología")
                .email("carlos.rodriguez@clinica.com")
                .horarioAtencion("Lunes a Viernes 08:00-17:00")
                .build();
    }

    @Nested
    @DisplayName("getMedico Tests")
    class GetMedicoTests {

        @Test
        @DisplayName("Should return Medico when exists")
        void shouldReturnMedicoWhenExists() {
            // Given
            Medico expected = createTestMedico();
            when(medicoRepository.findById("MED-001")).thenReturn(Optional.of(expected));

            // When
            Optional<Medico> result = medicoService.getMedico("MED-001");

            // Then
            assertTrue(result.isPresent());
            assertEquals("MED-001", result.get().getId());
            assertEquals("Dr. Carlos Rodríguez", result.get().getNombre());
            assertEquals("Cardiología", result.get().getEspecialidad());
            verify(medicoRepository, times(1)).findById("MED-001");
        }

        @Test
        @DisplayName("Should return empty Optional when not exists")
        void shouldReturnEmptyWhenNotExists() {
            // Given
            when(medicoRepository.findById("UNKNOWN")).thenReturn(Optional.empty());

            // When
            Optional<Medico> result = medicoService.getMedico("UNKNOWN");

            // Then
            assertTrue(result.isEmpty());
            verify(medicoRepository, times(1)).findById("UNKNOWN");
        }
    }

    @Nested
    @DisplayName("existsById Tests")
    class ExistsByIdTests {

        @Test
        @DisplayName("Should return true when Medico exists")
        void shouldReturnTrueWhenExists() {
            // Given
            when(medicoRepository.existsById("MED-001")).thenReturn(true);

            // When
            boolean result = medicoService.existsById("MED-001");

            // Then
            assertTrue(result);
            verify(medicoRepository, times(1)).existsById("MED-001");
        }

        @Test
        @DisplayName("Should return false when Medico does not exist")
        void shouldReturnFalseWhenNotExists() {
            // Given
            when(medicoRepository.existsById("UNKNOWN")).thenReturn(false);

            // When
            boolean result = medicoService.existsById("UNKNOWN");

            // Then
            assertFalse(result);
            verify(medicoRepository, times(1)).existsById("UNKNOWN");
        }
    }
}
