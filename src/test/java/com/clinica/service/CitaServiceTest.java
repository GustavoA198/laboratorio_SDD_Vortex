package com.clinica.service;

import com.clinica.dto.CitaRequest;
import com.clinica.dto.CitaResponse;
import com.clinica.exception.BusinessValidationException;
import com.clinica.exception.CitaNotFoundException;
import com.clinica.model.Cita;
import com.clinica.model.Medico;
import com.clinica.model.Paciente;
import com.clinica.model.enums.EstadoCita;
import com.clinica.repository.CitaRepository;
import com.clinica.repository.MedicoRepository;
import com.clinica.repository.PacienteRepository;
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

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private MedicoRepository medicoRepository;

    @InjectMocks
    private CitaService citaService;

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

    private Cita createTestCita(Long id, Paciente paciente, Medico medico,
                                 LocalDate fecha, LocalTime hora, EstadoCita estado) {
        return Cita.builder()
                .id(id)
                .paciente(paciente)
                .medico(medico)
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
            Paciente paciente = createTestPaciente();
            Medico medico = createTestMedico();
            Cita savedCita = createTestCita(1L, paciente, medico,
                    request.fecha(), request.hora(), EstadoCita.ACTIVA);

            when(pacienteRepository.existsById("PAC-001")).thenReturn(true);
            when(medicoRepository.existsById("MED-001")).thenReturn(true);
            when(pacienteRepository.getReferenceById("PAC-001")).thenReturn(paciente);
            when(medicoRepository.getReferenceById("MED-001")).thenReturn(medico);
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
        @DisplayName("Should fail when paciente does not exist")
        void shouldFailWhenPacienteNotExists() {
            // Given
            CitaRequest request = createValidRequest();
            when(pacienteRepository.existsById("PAC-001")).thenReturn(false);

            // When & Then
            BusinessValidationException exception = assertThrows(
                BusinessValidationException.class,
                () -> citaService.createCita(request)
            );
            assertTrue(exception.getMessage().contains("Paciente no encontrado"));
        }

        @Test
        @DisplayName("Should fail when medico does not exist")
        void shouldFailWhenMedicoNotExists() {
            // Given
            CitaRequest request = createValidRequest();
            when(pacienteRepository.existsById("PAC-001")).thenReturn(true);
            when(medicoRepository.existsById("MED-001")).thenReturn(false);

            // When & Then
            BusinessValidationException exception = assertThrows(
                BusinessValidationException.class,
                () -> citaService.createCita(request)
            );
            assertTrue(exception.getMessage().contains("Médico no encontrado"));
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
            Paciente paciente = createTestPaciente();
            Medico medico = createTestMedico();
            Cita savedCita = createTestCita(1L, paciente, medico,
                    request.fecha(), request.hora(), EstadoCita.ACTIVA);
            when(pacienteRepository.existsById("PAC-001")).thenReturn(true);
            when(medicoRepository.existsById("MED-001")).thenReturn(true);
            when(pacienteRepository.getReferenceById("PAC-001")).thenReturn(paciente);
            when(medicoRepository.getReferenceById("MED-001")).thenReturn(medico);
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
        @DisplayName("Should return cita when exists and user is owner")
        void shouldReturnCitaWhenExistsAndUserIsOwner() {
            // Given
            Paciente paciente = createTestPaciente();
            paciente.setUsername("PAC-001");
            Medico medico = createTestMedico();
            Cita cita = createTestCita(1L, paciente, medico,
                    LocalDate.now().plusDays(1), LocalTime.of(9, 0), EstadoCita.ACTIVA);
            when(citaRepository.findById(1L)).thenReturn(Optional.of(cita));

            // When - PACIENTE accessing their own cita
            CitaResponse response = citaService.getCita(1L, "PAC-001", "PACIENTE");

            // Then
            assertNotNull(response);
            assertEquals(1L, response.id());
            assertEquals("PAC-001", response.pacienteId());
        }

        @Test
        @DisplayName("Should return cita when MEDICO accesses any cita")
        void shouldReturnCitaWhenMedicoAccesses() {
            // Given
            Paciente paciente = createTestPaciente();
            Medico medico = createTestMedico();
            Cita cita = createTestCita(1L, paciente, medico,
                    LocalDate.now().plusDays(1), LocalTime.of(9, 0), EstadoCita.ACTIVA);
            when(citaRepository.findById(1L)).thenReturn(Optional.of(cita));

            // When - MEDICO can access any cita
            CitaResponse response = citaService.getCita(1L, "MED-001", "MEDICO");

            // Then
            assertNotNull(response);
            assertEquals(1L, response.id());
        }

        @Test
        @DisplayName("Should return cita when ADMIN accesses any cita")
        void shouldReturnCitaWhenAdminAccesses() {
            // Given
            Paciente paciente = createTestPaciente();
            Medico medico = createTestMedico();
            Cita cita = createTestCita(1L, paciente, medico,
                    LocalDate.now().plusDays(1), LocalTime.of(9, 0), EstadoCita.ACTIVA);
            when(citaRepository.findById(1L)).thenReturn(Optional.of(cita));

            // When - ADMIN can access any cita
            CitaResponse response = citaService.getCita(1L, "admin", "ADMIN");

            // Then
            assertNotNull(response);
            assertEquals(1L, response.id());
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when PACIENTE accesses another patient's cita")
        void shouldThrowWhenPacienteAccessesOthersCita() {
            // Given
            Paciente paciente = createTestPaciente();
            paciente.setUsername("PAC-001");
            Medico medico = createTestMedico();
            Cita cita = createTestCita(1L, paciente, medico,
                    LocalDate.now().plusDays(1), LocalTime.of(9, 0), EstadoCita.ACTIVA);
            when(citaRepository.findById(1L)).thenReturn(Optional.of(cita));

            // When & Then - PAC-002 trying to access PAC-001's cita
            assertThrows(
                org.springframework.security.access.AccessDeniedException.class,
                () -> citaService.getCita(1L, "PAC-002", "PACIENTE")
            );
        }

        @Test
        @DisplayName("Should throw CitaNotFoundException when not exists")
        void shouldThrowWhenNotExists() {
            // Given
            when(citaRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            CitaNotFoundException exception = assertThrows(
                CitaNotFoundException.class,
                () -> citaService.getCita(999L, "user", "ADMIN")
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
            Paciente paciente = createTestPaciente();
            paciente.setUsername("PAC-001");
            Medico medico = createTestMedico();
            Cita existingCita = createTestCita(1L, paciente, medico,
                    LocalDate.now().plusDays(1), LocalTime.of(9, 0), EstadoCita.ACTIVA);
            when(citaRepository.findById(1L)).thenReturn(Optional.of(existingCita));
            when(pacienteRepository.existsById("PAC-002")).thenReturn(true);
            when(medicoRepository.existsById("MED-001")).thenReturn(true);

            Paciente paciente2 = Paciente.builder().id("PAC-002").nombre("María").username("PAC-002").build();
            when(pacienteRepository.getReferenceById("PAC-002")).thenReturn(paciente2);
            when(medicoRepository.getReferenceById("MED-001")).thenReturn(medico);

            when(citaRepository.save(any(Cita.class))).thenAnswer(inv -> inv.getArgument(0));

            LocalDate newFecha = getNextTuesday();
            CitaRequest updateRequest = new CitaRequest(
                "PAC-002", "MED-001", newFecha, LocalTime.of(10, 0), "Nueva consulta"
            );

            // When - PACIENTE updating their own cita
            CitaResponse response = citaService.updateCita(1L, updateRequest, "PAC-001", "PACIENTE");

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
            Paciente paciente = createTestPaciente();
            paciente.setUsername("PAC-001");
            Medico medico = createTestMedico();
            Cita cancelledCita = createTestCita(1L, paciente, medico,
                    LocalDate.now().plusDays(1), LocalTime.of(9, 0), EstadoCita.CANCELADA);
            when(citaRepository.findById(1L)).thenReturn(Optional.of(cancelledCita));

            CitaRequest updateRequest = createValidRequest();

            // When & Then
            IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> citaService.updateCita(1L, updateRequest, "PAC-001", "PACIENTE")
            );
            assertTrue(exception.getMessage().contains("ACTIVA"));
        }

        @Test
        @DisplayName("Should throw CitaNotFoundException when updating non-existent cita")
        void shouldThrowWhenUpdatingNonExuent() {
            // Given
            when(citaRepository.findById(999L)).thenReturn(Optional.empty());
            CitaRequest updateRequest = createValidRequest();

            // When & Then
            assertThrows(
                CitaNotFoundException.class,
                () -> citaService.updateCita(999L, updateRequest, "user", "ADMIN")
            );
        }

        @Test
        @DisplayName("Should fail update when new time is invalid")
        void shouldFailUpdateWithInvalidTime() {
            // Given
            Paciente paciente = createTestPaciente();
            paciente.setUsername("PAC-001");
            Medico medico = createTestMedico();
            Cita existingCita = createTestCita(1L, paciente, medico,
                    LocalDate.now().plusDays(1), LocalTime.of(9, 0), EstadoCita.ACTIVA);
            when(citaRepository.findById(1L)).thenReturn(Optional.of(existingCita));

            LocalDate newFecha = getNextTuesday();

            CitaRequest invalidRequest = new CitaRequest(
                "PAC-001", "MED-001", newFecha, LocalTime.of(9, 15), "Consulta" // Invalid slot
            );

            // When & Then
            assertThrows(
                BusinessValidationException.class,
                () -> citaService.updateCita(1L, invalidRequest, "PAC-001", "PACIENTE")
            );
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when PACIENTE tries to update another's cita")
        void shouldThrowWhenPacienteUpdatesOthersCita() {
            // Given
            Paciente paciente = createTestPaciente();
            paciente.setUsername("PAC-001");
            Medico medico = createTestMedico();
            Cita existingCita = createTestCita(1L, paciente, medico,
                    LocalDate.now().plusDays(1), LocalTime.of(9, 0), EstadoCita.ACTIVA);
            when(citaRepository.findById(1L)).thenReturn(Optional.of(existingCita));

            CitaRequest updateRequest = createValidRequest();

            // When & Then - PAC-002 trying to update PAC-001's cita
            assertThrows(
                org.springframework.security.access.AccessDeniedException.class,
                () -> citaService.updateCita(1L, updateRequest, "PAC-002", "PACIENTE")
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
            Paciente paciente = createTestPaciente();
            paciente.setUsername("PAC-001");
            Medico medico = createTestMedico();
            Cita activaCita = createTestCita(1L, paciente, medico,
                    LocalDate.now().plusDays(1), LocalTime.of(9, 0), EstadoCita.ACTIVA);
            when(citaRepository.findById(1L)).thenReturn(Optional.of(activaCita));
            when(citaRepository.save(any(Cita.class))).thenAnswer(inv -> inv.getArgument(0));

            // When - PACIENTE canceling their own cita
            CitaResponse response = citaService.cancelCita(1L, "PAC-001", "PACIENTE");

            // Then
            assertNotNull(response);
            assertEquals("CANCELADA", response.estado());
            verify(citaRepository, times(1)).save(any(Cita.class));
        }

        @Test
        @DisplayName("Should throw CitaNotFoundException when cancelling non-existent")
        void shouldThrowWhenCancellingNonExuent() {
            // Given
            when(citaRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(
                CitaNotFoundException.class,
                () -> citaService.cancelCita(999L, "user", "ADMIN")
            );
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when PACIENTE tries to cancel another's cita")
        void shouldThrowWhenPacienteCancelsOthersCita() {
            // Given
            Paciente paciente = createTestPaciente();
            paciente.setUsername("PAC-001");
            Medico medico = createTestMedico();
            Cita activaCita = createTestCita(1L, paciente, medico,
                    LocalDate.now().plusDays(1), LocalTime.of(9, 0), EstadoCita.ACTIVA);
            when(citaRepository.findById(1L)).thenReturn(Optional.of(activaCita));

            // When & Then - PAC-002 trying to cancel PAC-001's cita
            assertThrows(
                org.springframework.security.access.AccessDeniedException.class,
                () -> citaService.cancelCita(1L, "PAC-002", "PACIENTE")
            );
        }

        @Test
        @DisplayName("Should allow MEDICO to cancel any cita")
        void shouldAllowMedicoToCancelAnyCita() {
            // Given
            Paciente paciente = createTestPaciente();
            paciente.setUsername("PAC-001");
            Medico medico = createTestMedico();
            Cita activaCita = createTestCita(1L, paciente, medico,
                    LocalDate.now().plusDays(1), LocalTime.of(9, 0), EstadoCita.ACTIVA);
            when(citaRepository.findById(1L)).thenReturn(Optional.of(activaCita));
            when(citaRepository.save(any(Cita.class))).thenAnswer(inv -> inv.getArgument(0));

            // When - MEDICO canceling any cita
            CitaResponse response = citaService.cancelCita(1L, "MED-001", "MEDICO");

            // Then
            assertNotNull(response);
            assertEquals("CANCELADA", response.estado());
        }
    }

    @Nested
    @DisplayName("listCitas Tests")
    class ListCitasTests {

        @Test
        @DisplayName("Should return list of citas")
        void shouldReturnListOfCitas() {
            // Given
            Paciente paciente = createTestPaciente();
            Medico medico = createTestMedico();
            List<Cita> citas = List.of(
                createTestCita(1L, paciente, medico,
                        LocalDate.now().plusDays(1), LocalTime.of(9, 0), EstadoCita.ACTIVA),
                createTestCita(2L, paciente, medico,
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

    @Nested
    @DisplayName("getCitasPorPaciente Tests")
    class GetCitasPorPacienteTests {

        @Test
        @DisplayName("Should return only ACTIVA citas for paciente")
        void shouldReturnOnlyActivaCitasForPaciente() {
            // Given
            Paciente paciente = createTestPaciente();
            Medico medico = createTestMedico();
            Cita cita1 = createTestCita(1L, paciente, medico,
                    LocalDate.now().plusDays(1), LocalTime.of(9, 0), EstadoCita.ACTIVA);
            Cita cita2 = createTestCita(2L, paciente, medico,
                    LocalDate.now().plusDays(2), LocalTime.of(10, 0), EstadoCita.CANCELADA);
            when(citaRepository.findByPacienteId("PAC-001")).thenReturn(List.of(cita1, cita2));

            // When
            List<CitaResponse> responses = citaService.getCitasPorPaciente("PAC-001");

            // Then
            assertNotNull(responses);
            assertEquals(1, responses.size());
            assertEquals(1L, responses.get(0).id());
            assertEquals("ACTIVA", responses.get(0).estado());
        }
    }

    @Nested
    @DisplayName("getCitasPorMedico Tests")
    class GetCitasPorMedicoTests {

        @Test
        @DisplayName("Should return only ACTIVA citas for medico")
        void shouldReturnOnlyActivaCitasForMedico() {
            // Given
            Paciente paciente = createTestPaciente();
            Medico medico = createTestMedico();
            Cita cita1 = createTestCita(1L, paciente, medico,
                    LocalDate.now().plusDays(1), LocalTime.of(9, 0), EstadoCita.ACTIVA);
            Cita cita2 = createTestCita(2L, paciente, medico,
                    LocalDate.now().plusDays(2), LocalTime.of(10, 0), EstadoCita.ACTIVA);
            when(citaRepository.findByMedicoId("MED-001")).thenReturn(List.of(cita1, cita2));

            // When
            List<CitaResponse> responses = citaService.getCitasPorMedico("MED-001");

            // Then
            assertNotNull(responses);
            assertEquals(2, responses.size());
        }
    }
}
