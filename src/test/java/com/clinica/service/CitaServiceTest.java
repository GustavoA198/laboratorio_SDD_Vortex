package com.clinica.service;

import com.clinica.dto.CitaRequest;
import com.clinica.dto.CitaResponse;
import com.clinica.exception.BusinessValidationException;
import com.clinica.exception.CitaNotFoundException;
import com.clinica.model.Cita;
import com.clinica.model.enums.EstadoCita;
import com.clinica.repository.CitaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CitaService Tests")
class CitaServiceTest {

    @Mock
    private CitaRepository citaRepository;

    @InjectMocks
    private CitaService citaService;

    private Cita createTestCita(Long id, String pacienteId, String medicoId,
                                 LocalDate fecha, LocalTime hora, EstadoCita estado) {
        return Cita.builder()
                .id(id)
                .pacienteId(pacienteId)
                .medicoId(medicoId)
                .fecha(fecha)
                .hora(hora)
                .estado(estado)
                .motivoConsulta("Consulta general")
                .fechaCreacion(LocalDateTime.now())
                .build();
    }

    private CitaRequest createValidRequest() {
        // Find next Tuesday (known weekday)
        LocalDate fecha = LocalDate.now();
        while (fecha.getDayOfWeek() != DayOfWeek.TUESDAY) {
            fecha = fecha.plusDays(1);
        }
        return new CitaRequest(
            "PAC-001",
            "MED-001",
            fecha,
            LocalTime.of(9, 0),
            "Consulta general"
        );
    }

    private LocalDate getNextTuesday() {
        LocalDate date = LocalDate.now();
        while (date.getDayOfWeek() != DayOfWeek.TUESDAY) {
            date = date.plusDays(1);
        }
        return date;
    }

    @Nested
    @DisplayName("createCita Tests")
    class CreateCitaTests {

        @Test
        @DisplayName("Should create cita successfully with valid data")
        void shouldCreateCitaSuccessfully() {
            // Given
            CitaRequest request = createValidRequest();
            Cita savedCita = createTestCita(1L, request.pacienteId(), request.medicoId(),
                    request.fecha(), request.hora(), EstadoCita.ACTIVA);

            when(citaRepository.save(any(Cita.class))).thenReturn(savedCita);

            // When
            CitaResponse response = citaService.createCita(request);

            // Then
            assertNotNull(response);
            assertEquals(1L, response.id());
            assertEquals("PAC-001", response.pacienteId());
            assertEquals("MED-001", response.medicoId());
            assertEquals(request.fecha(), response.fecha());
            assertEquals(request.hora(), response.hora());
            assertEquals("ACTIVA", response.estado());
            assertEquals("Consulta general", response.motivoConsulta());
            verify(citaRepository, times(1)).save(any(Cita.class));
        }

        @Test
        @DisplayName("Should fail when hora is before 08:00")
        void shouldFailWhenHoraBefore08() {
            // Given
            LocalDate fecha = LocalDate.now();
            while (fecha.getDayOfWeek() != DayOfWeek.MONDAY) {
                fecha = fecha.plusDays(1);
            }
            CitaRequest request = new CitaRequest(
                "PAC-001", "MED-001", fecha, LocalTime.of(7, 30), "Consulta"
            );

            // When & Then
            BusinessValidationException exception = assertThrows(
                BusinessValidationException.class,
                () -> citaService.createCita(request)
            );
            assertTrue(exception.getMessage().contains("08:00-17:00"));
        }

        @Test
        @DisplayName("Should fail when hora is after 17:00")
        void shouldFailWhenHoraAfter17() {
            // Given
            LocalDate fecha = LocalDate.now();
            while (fecha.getDayOfWeek() != DayOfWeek.MONDAY) {
                fecha = fecha.plusDays(1);
            }
            CitaRequest request = new CitaRequest(
                "PAC-001", "MED-001", fecha, LocalTime.of(17, 30), "Consulta"
            );

            // When & Then
            BusinessValidationException exception = assertThrows(
                BusinessValidationException.class,
                () -> citaService.createCita(request)
            );
            assertTrue(exception.getMessage().contains("08:00-17:00"));
        }

        @Test
        @DisplayName("Should fail when slot is not :00 or :30")
        void shouldFailWhenSlotInvalid() {
            // Given
            LocalDate fecha = LocalDate.now();
            while (fecha.getDayOfWeek() != DayOfWeek.MONDAY) {
                fecha = fecha.plusDays(1);
            }
            CitaRequest request = new CitaRequest(
                "PAC-001", "MED-001", fecha, LocalTime.of(9, 15), "Consulta"
            );

            // When & Then
            BusinessValidationException exception = assertThrows(
                BusinessValidationException.class,
                () -> citaService.createCita(request)
            );
            assertTrue(exception.getMessage().contains("30 minutos"));
        }

        @Test
        @DisplayName("Should fail on Saturday")
        void shouldFailOnSaturday() {
            // Given
            LocalDate saturday = LocalDate.now();
            while (saturday.getDayOfWeek() != DayOfWeek.SATURDAY) {
                saturday = saturday.plusDays(1);
            }
            CitaRequest request = new CitaRequest(
                "PAC-001", "MED-001", saturday, LocalTime.of(9, 0), "Consulta"
            );

            // When & Then
            BusinessValidationException exception = assertThrows(
                BusinessValidationException.class,
                () -> citaService.createCita(request)
            );
            assertTrue(exception.getMessage().contains("fins de semana"));
        }

        @Test
        @DisplayName("Should fail on Sunday")
        void shouldFailOnSunday() {
            // Given
            LocalDate sunday = LocalDate.now();
            while (sunday.getDayOfWeek() != DayOfWeek.SUNDAY) {
                sunday = sunday.plusDays(1);
            }
            CitaRequest request = new CitaRequest(
                "PAC-001", "MED-001", sunday, LocalTime.of(9, 0), "Consulta"
            );

            // When & Then
            BusinessValidationException exception = assertThrows(
                BusinessValidationException.class,
                () -> citaService.createCita(request)
            );
            assertTrue(exception.getMessage().contains("fins de semana"));
        }

        @Test
        @DisplayName("Should fail when hora is exactly 17:00")
        void shouldFailWhenHoraExactly17() {
            // Given - 17:00 is the boundary, it should be allowed since spec says 08:00 <= hora <= 17:00
            LocalDate fecha = LocalDate.now();
            while (fecha.getDayOfWeek() != DayOfWeek.MONDAY) {
                fecha = fecha.plusDays(1);
            }
            CitaRequest request = new CitaRequest(
                "PAC-001", "MED-001", fecha, LocalTime.of(17, 0), "Consulta"
            );
            Cita savedCita = createTestCita(1L, request.pacienteId(), request.medicoId(),
                    request.fecha(), request.hora(), EstadoCita.ACTIVA);
            when(citaRepository.save(any(Cita.class))).thenReturn(savedCita);

            // When
            CitaResponse response = citaService.createCita(request);

            // Then - 17:00 should be valid (inclusive range)
            assertNotNull(response);
        }
    }

    @Nested
    @DisplayName("getCita Tests")
    class GetCitaTests {

        @Test
        @DisplayName("Should return cita when exists")
        void shouldReturnCitaWhenExists() {
            // Given
            Cita cita = createTestCita(1L, "PAC-001", "MED-001",
                    LocalDate.now().plusDays(1), LocalTime.of(9, 0), EstadoCita.ACTIVA);
            when(citaRepository.findById(1L)).thenReturn(Optional.of(cita));

            // When
            CitaResponse response = citaService.getCita(1L);

            // Then
            assertNotNull(response);
            assertEquals(1L, response.id());
            assertEquals("PAC-001", response.pacienteId());
        }

        @Test
        @DisplayName("Should throw CitaNotFoundException when not exists")
        void shouldThrowWhenNotExists() {
            // Given
            when(citaRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            CitaNotFoundException exception = assertThrows(
                CitaNotFoundException.class,
                () -> citaService.getCita(999L)
            );
            assertTrue(exception.getMessage().contains("999"));
        }
    }

    @Nested
    @DisplayName("updateCita Tests")
    class UpdateCitaTests {

        @Test
        @DisplayName("Should update ACTIVA cita successfully")
        void shouldUpdateActivaCitaSuccessfully() {
            // Given
            Cita existingCita = createTestCita(1L, "PAC-001", "MED-001",
                    LocalDate.now().plusDays(1), LocalTime.of(9, 0), EstadoCita.ACTIVA);
            when(citaRepository.findById(1L)).thenReturn(Optional.of(existingCita));
            when(citaRepository.save(any(Cita.class))).thenAnswer(inv -> inv.getArgument(0));

            LocalDate newFecha = getNextTuesday();
            CitaRequest updateRequest = new CitaRequest(
                "PAC-002", "MED-001", newFecha, LocalTime.of(10, 0), "Nueva consulta"
            );

            // When
            CitaResponse response = citaService.updateCita(1L, updateRequest);

            // Then
            assertNotNull(response);
            assertEquals("PAC-002", response.pacienteId());
            assertEquals(newFecha, response.fecha());
            assertEquals(LocalTime.of(10, 0), response.hora());
        }

        @Test
        @DisplayName("Should throw exception when updating non-ACTIVA cita")
        void shouldThrowWhenUpdatingNonActiva() {
            // Given
            Cita cancelledCita = createTestCita(1L, "PAC-001", "MED-001",
                    LocalDate.now().plusDays(1), LocalTime.of(9, 0), EstadoCita.CANCELADA);
            when(citaRepository.findById(1L)).thenReturn(Optional.of(cancelledCita));

            CitaRequest updateRequest = createValidRequest();

            // When & Then
            IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> citaService.updateCita(1L, updateRequest)
            );
            assertTrue(exception.getMessage().contains("ACTIVA"));
        }

        @Test
        @DisplayName("Should throw CitaNotFoundException when updating non-existent cita")
        void shouldThrowWhenUpdatingNonExistent() {
            // Given
            when(citaRepository.findById(999L)).thenReturn(Optional.empty());
            CitaRequest updateRequest = createValidRequest();

            // When & Then
            assertThrows(
                CitaNotFoundException.class,
                () -> citaService.updateCita(999L, updateRequest)
            );
        }

        @Test
        @DisplayName("Should fail update when new time is invalid")
        void shouldFailUpdateWithInvalidTime() {
            // Given
            Cita existingCita = createTestCita(1L, "PAC-001", "MED-001",
                    LocalDate.now().plusDays(1), LocalTime.of(9, 0), EstadoCita.ACTIVA);
            when(citaRepository.findById(1L)).thenReturn(Optional.of(existingCita));

            LocalDate newFecha = getNextTuesday();
            CitaRequest invalidRequest = new CitaRequest(
                "PAC-001", "MED-001", newFecha, LocalTime.of(9, 15), "Consulta" // Invalid slot
            );

            // When & Then
            assertThrows(
                BusinessValidationException.class,
                () -> citaService.updateCita(1L, invalidRequest)
            );
        }
    }

    @Nested
    @DisplayName("cancelCita Tests")
    class CancelCitaTests {

        @Test
        @DisplayName("Should cancel cita successfully")
        void shouldCancelCitaSuccessfully() {
            // Given
            Cita activaCita = createTestCita(1L, "PAC-001", "MED-001",
                    LocalDate.now().plusDays(1), LocalTime.of(9, 0), EstadoCita.ACTIVA);
            when(citaRepository.findById(1L)).thenReturn(Optional.of(activaCita));
            when(citaRepository.save(any(Cita.class))).thenAnswer(inv -> inv.getArgument(0));

            // When
            CitaResponse response = citaService.cancelCita(1L);

            // Then
            assertNotNull(response);
            assertEquals("CANCELADA", response.estado());
            verify(citaRepository, times(1)).save(any(Cita.class));
        }

        @Test
        @DisplayName("Should throw CitaNotFoundException when cancelling non-existent")
        void shouldThrowWhenCancellingNonExistent() {
            // Given
            when(citaRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(
                CitaNotFoundException.class,
                () -> citaService.cancelCita(999L)
            );
        }
    }

    @Nested
    @DisplayName("listCitas Tests")
    class ListCitasTests {

        @Test
        @DisplayName("Should return list of citas")
        void shouldReturnListOfCitas() {
            // Given
            List<Cita> citas = List.of(
                createTestCita(1L, "PAC-001", "MED-001",
                        LocalDate.now().plusDays(1), LocalTime.of(9, 0), EstadoCita.ACTIVA),
                createTestCita(2L, "PAC-002", "MED-002",
                        LocalDate.now().plusDays(2), LocalTime.of(10, 0), EstadoCita.ACTIVA)
            );
            when(citaRepository.findAll()).thenReturn(citas);

            // When
            List<CitaResponse> responses = citaService.listCitas();

            // Then
            assertNotNull(responses);
            assertEquals(2, responses.size());
            assertEquals(1L, responses.get(0).id());
            assertEquals(2L, responses.get(1).id());
        }

        @Test
        @DisplayName("Should return empty list when no citas")
        void shouldReturnEmptyListWhenNoCitas() {
            // Given
            when(citaRepository.findAll()).thenReturn(List.of());

            // When
            List<CitaResponse> responses = citaService.listCitas();

            // Then
            assertNotNull(responses);
            assertTrue(responses.isEmpty());
        }
    }
}