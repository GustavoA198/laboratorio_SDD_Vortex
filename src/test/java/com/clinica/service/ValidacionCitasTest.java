package com.clinica.service;

import com.clinica.dto.CitaRequest;
import com.clinica.dto.CitaResponse;
import com.clinica.exception.BusinessValidationException;
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
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests for advanced Cita validations:
 * - Max 3 active citas per patient
 * - Anti-duplicate (same medico + fecha + hora)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ValidacionCitas Tests")
class ValidacionCitasTest {

    @Mock
    private CitaRepository citaRepository;

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private MedicoRepository medicoRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private CitaService citaService;

    private Paciente createTestPaciente(String id) {
        return Paciente.builder()
                .id(id)
                .nombre("Test Paciente")
                .email("test@email.com")
                .telefono("+54-11-5555-1234")
                .fechaRegistro(LocalDate.of(2025, 1, 15))
                .build();
    }

    private Medico createTestMedico(String id) {
        return Medico.builder()
                .id(id)
                .nombre("Dr. Test")
                .especialidad("General")
                .email("doctor@clinica.com")
                .horarioAtencion("Lunes a Viernes 08:00-17:00")
                .build();
    }

    private LocalDate getNextWeekday() {
        LocalDate date = LocalDate.now();
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY ||
               date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            date = date.plusDays(1);
        }
        return date;
    }

    private CitaRequest createValidRequest(String pacienteId, String medicoId) {
        return new CitaRequest(
            pacienteId,
            medicoId,
            getNextWeekday(),
            LocalTime.of(9, 0),
            "Consulta general"
        );
    }

    @Nested
    @DisplayName("Max 3 Citas Activas Tests")
    class Max3CitasActivasTests {

        @Test
        @DisplayName("Should create cita when patient has 0 active citas")
        void shouldCreateWhenNoActiveCitas() {
            // Given
            String pacienteId = "PAC-001";
            CitaRequest request = createValidRequest(pacienteId, "MED-001");
            Paciente paciente = createTestPaciente(pacienteId);
            Medico medico = createTestMedico("MED-001");

            when(pacienteRepository.existsById(pacienteId)).thenReturn(true);
            when(medicoRepository.existsById("MED-001")).thenReturn(true);
            when(citaRepository.countByPacienteIdAndEstado(pacienteId, EstadoCita.ACTIVA)).thenReturn(0L);
            when(pacienteRepository.getReferenceById(pacienteId)).thenReturn(paciente);
            when(medicoRepository.getReferenceById("MED-001")).thenReturn(medico);
            when(citaRepository.save(any(Cita.class))).thenAnswer(inv -> {
                Cita c = inv.getArgument(0);
                c.setId(1L);
                return c;
            });

            // When
            CitaResponse response = citaService.createCita(request);

            // Then
            assertNotNull(response);
            verify(citaRepository).countByPacienteIdAndEstado(pacienteId, EstadoCita.ACTIVA);
            verify(citaRepository).save(any(Cita.class));
        }

        @Test
        @DisplayName("Should create cita when patient has 2 active citas")
        void shouldCreateWhenPatientHas2Active() {
            // Given
            String pacienteId = "PAC-001";
            CitaRequest request = createValidRequest(pacienteId, "MED-001");
            Paciente paciente = createTestPaciente(pacienteId);
            Medico medico = createTestMedico("MED-001");

            when(pacienteRepository.existsById(pacienteId)).thenReturn(true);
            when(medicoRepository.existsById("MED-001")).thenReturn(true);
            when(citaRepository.countByPacienteIdAndEstado(pacienteId, EstadoCita.ACTIVA)).thenReturn(2L);
            when(pacienteRepository.getReferenceById(pacienteId)).thenReturn(paciente);
            when(medicoRepository.getReferenceById("MED-001")).thenReturn(medico);
            when(citaRepository.save(any(Cita.class))).thenAnswer(inv -> {
                Cita c = inv.getArgument(0);
                c.setId(1L);
                return c;
            });

            // When
            CitaResponse response = citaService.createCita(request);

            // Then
            assertNotNull(response);
            assertEquals(pacienteId, response.pacienteId());
        }

        @Test
        @DisplayName("Should reject when patient has 3 active citas")
        void shouldRejectWhenPatientHas3Active() {
            // Given
            String pacienteId = "PAC-001";
            CitaRequest request = createValidRequest(pacienteId, "MED-001");

            when(pacienteRepository.existsById(pacienteId)).thenReturn(true);
            when(medicoRepository.existsById("MED-001")).thenReturn(true);
            when(citaRepository.countByPacienteIdAndEstado(pacienteId, EstadoCita.ACTIVA)).thenReturn(3L);

            // When & Then
            BusinessValidationException exception = assertThrows(
                BusinessValidationException.class,
                () -> citaService.createCita(request)
            );
            assertTrue(exception.getMessage().contains("3"));
            assertTrue(exception.getMessage().toLowerCase().contains("activa"));
            verify(citaRepository, never()).save(any(Cita.class));
        }

        @Test
        @DisplayName("Should reject when patient has more than 3 active citas")
        void shouldRejectWhenPatientHasMoreThan3Active() {
            // Given
            String pacienteId = "PAC-001";
            CitaRequest request = createValidRequest(pacienteId, "MED-001");

            when(pacienteRepository.existsById(pacienteId)).thenReturn(true);
            when(medicoRepository.existsById("MED-001")).thenReturn(true);
            when(citaRepository.countByPacienteIdAndEstado(pacienteId, EstadoCita.ACTIVA)).thenReturn(5L);

            // When & Then
            BusinessValidationException exception = assertThrows(
                BusinessValidationException.class,
                () -> citaService.createCita(request)
            );
            assertTrue(exception.getMessage().contains("3"));
            verify(citaRepository, never()).save(any(Cita.class));
        }

        @Test
        @DisplayName("Should ignore cancelled citas for max count")
        void shouldIgnoreCancelledCitasForMaxCount() {
            // Given
            String pacienteId = "PAC-001";
            CitaRequest request = createValidRequest(pacienteId, "MED-001");
            Paciente paciente = createTestPaciente(pacienteId);
            Medico medico = createTestMedico("MED-001");

            when(pacienteRepository.existsById(pacienteId)).thenReturn(true);
            when(medicoRepository.existsById("MED-001")).thenReturn(true);
            // Only ACTIVA count is checked, not CANCELADA
            when(citaRepository.countByPacienteIdAndEstado(pacienteId, EstadoCita.ACTIVA)).thenReturn(2L);
            when(pacienteRepository.getReferenceById(pacienteId)).thenReturn(paciente);
            when(medicoRepository.getReferenceById("MED-001")).thenReturn(medico);
            when(citaRepository.save(any(Cita.class))).thenAnswer(inv -> {
                Cita c = inv.getArgument(0);
                c.setId(1L);
                return c;
            });

            // When
            CitaResponse response = citaService.createCita(request);

            // Then - should succeed because only ACTIVA count is 2
            assertNotNull(response);
        }
    }

    @Nested
    @DisplayName("Anti-Duplicados Tests")
    class AntiDuplicadosTests {

        @Test
        @DisplayName("Should create cita when no duplicate exists")
        void shouldCreateWhenNoDuplicateExists() {
            // Given
            String pacienteId = "PAC-001";
            String medicoId = "MED-001";
            LocalDate fecha = getNextWeekday();
            LocalTime hora = LocalTime.of(9, 0);

            CitaRequest request = new CitaRequest(
                pacienteId, medicoId, fecha, hora, "Consulta general"
            );
            Paciente paciente = createTestPaciente(pacienteId);
            Medico medico = createTestMedico(medicoId);

            when(pacienteRepository.existsById(pacienteId)).thenReturn(true);
            when(medicoRepository.existsById(medicoId)).thenReturn(true);
            when(citaRepository.countByPacienteIdAndEstado(pacienteId, EstadoCita.ACTIVA)).thenReturn(0L);
            when(citaRepository.existsByMedicoIdAndFechaAndHora(medicoId, fecha, hora)).thenReturn(false);
            when(pacienteRepository.getReferenceById(pacienteId)).thenReturn(paciente);
            when(medicoRepository.getReferenceById(medicoId)).thenReturn(medico);
            when(citaRepository.save(any(Cita.class))).thenAnswer(inv -> {
                Cita c = inv.getArgument(0);
                c.setId(1L);
                return c;
            });

            // When
            CitaResponse response = citaService.createCita(request);

            // Then
            assertNotNull(response);
            verify(citaRepository).existsByMedicoIdAndFechaAndHora(medicoId, fecha, hora);
        }

        @Test
        @DisplayName("Should reject duplicate cita with same medico, fecha, and hora")
        void shouldRejectDuplicateWithSameMedicoFechaHora() {
            // Given
            String pacienteId = "PAC-001";
            String medicoId = "MED-001";
            LocalDate fecha = getNextWeekday();
            LocalTime hora = LocalTime.of(9, 0);

            CitaRequest request = new CitaRequest(
                pacienteId, medicoId, fecha, hora, "Consulta general"
            );

            when(pacienteRepository.existsById(pacienteId)).thenReturn(true);
            when(medicoRepository.existsById(medicoId)).thenReturn(true);
            when(citaRepository.countByPacienteIdAndEstado(pacienteId, EstadoCita.ACTIVA)).thenReturn(0L);
            when(citaRepository.existsByMedicoIdAndFechaAndHora(medicoId, fecha, hora)).thenReturn(true);

            // When & Then
            BusinessValidationException exception = assertThrows(
                BusinessValidationException.class,
                () -> citaService.createCita(request)
            );
            assertTrue(exception.getMessage().toLowerCase().contains("médico") ||
                       exception.getMessage().toLowerCase().contains("hora"));
            verify(citaRepository, never()).save(any(Cita.class));
        }

        @Test
        @DisplayName("Should allow different time with same medico and fecha")
        void shouldAllowDifferentTimeWithSameMedicoAndFecha() {
            // Given
            String pacienteId = "PAC-001";
            String medicoId = "MED-001";
            LocalDate fecha = getNextWeekday();

            // Request at 10:00 - 9:00 slot is already taken (but not by this patient)
            CitaRequest request = new CitaRequest(
                pacienteId, medicoId, fecha, LocalTime.of(10, 0), "Consulta 2"
            );

            Paciente paciente = createTestPaciente(pacienteId);
            Medico medico = createTestMedico(medicoId);

            when(pacienteRepository.existsById(pacienteId)).thenReturn(true);
            when(medicoRepository.existsById(medicoId)).thenReturn(true);
            when(citaRepository.countByPacienteIdAndEstado(pacienteId, EstadoCita.ACTIVA)).thenReturn(0L);
            // 10:00 slot is free
            when(citaRepository.existsByMedicoIdAndFechaAndHora(medicoId, fecha, LocalTime.of(10, 0))).thenReturn(false);
            when(pacienteRepository.getReferenceById(pacienteId)).thenReturn(paciente);
            when(medicoRepository.getReferenceById(medicoId)).thenReturn(medico);
            when(citaRepository.save(any(Cita.class))).thenAnswer(inv -> {
                Cita c = inv.getArgument(0);
                c.setId(1L);
                return c;
            });

            // When - request at 10:00 (different from 9:00 which is taken)
            CitaResponse response = citaService.createCita(request);

            // Then
            assertNotNull(response);
            assertEquals(LocalTime.of(10, 0), response.hora());
        }

        @Test
        @DisplayName("Should allow same time with different medico")
        void shouldAllowSameTimeWithDifferentMedico() {
            // Given
            String pacienteId = "PAC-001";
            LocalDate fecha = getNextWeekday();
            LocalTime hora = LocalTime.of(9, 0);

            Paciente paciente = createTestPaciente(pacienteId);
            Medico medico1 = createTestMedico("MED-001");

            // Request with MED-001
            CitaRequest request = new CitaRequest(
                pacienteId, "MED-001", fecha, hora, "Consulta"
            );

            when(pacienteRepository.existsById(pacienteId)).thenReturn(true);
            when(medicoRepository.existsById("MED-001")).thenReturn(true);
            when(citaRepository.countByPacienteIdAndEstado(pacienteId, EstadoCita.ACTIVA)).thenReturn(0L);
            // MED-001 at this time is free
            when(citaRepository.existsByMedicoIdAndFechaAndHora("MED-001", fecha, hora)).thenReturn(false);
            when(pacienteRepository.getReferenceById(pacienteId)).thenReturn(paciente);
            when(medicoRepository.getReferenceById("MED-001")).thenReturn(medico1);
            when(citaRepository.save(any(Cita.class))).thenAnswer(inv -> {
                Cita c = inv.getArgument(0);
                c.setId(1L);
                return c;
            });

            // When
            CitaResponse response = citaService.createCita(request);

            // Then - should succeed because different medico
            assertNotNull(response);
            assertEquals("MED-001", response.medicoId());
        }

        @Test
        @DisplayName("Should allow same time and medico on different date")
        void shouldAllowSameTimeAndMedicoOnDifferentDate() {
            // Given
            String pacienteId = "PAC-001";
            String medicoId = "MED-001";
            LocalTime hora = LocalTime.of(9, 0);

            Paciente paciente = createTestPaciente(pacienteId);
            Medico medico = createTestMedico(medicoId);

            // Find next Monday and Tuesday
            LocalDate monday = LocalDate.now();
            while (monday.getDayOfWeek() != DayOfWeek.MONDAY) {
                monday = monday.plusDays(1);
            }
            LocalDate tuesday = monday.plusDays(1);

            // Request for Monday
            CitaRequest requestMonday = new CitaRequest(
                pacienteId, medicoId, monday, hora, "Consulta lunes"
            );

            when(pacienteRepository.existsById(pacienteId)).thenReturn(true);
            when(medicoRepository.existsById(medicoId)).thenReturn(true);
            when(citaRepository.countByPacienteIdAndEstado(pacienteId, EstadoCita.ACTIVA)).thenReturn(0L);
            when(citaRepository.existsByMedicoIdAndFechaAndHora(medicoId, monday, hora)).thenReturn(false);
            when(pacienteRepository.getReferenceById(pacienteId)).thenReturn(paciente);
            when(medicoRepository.getReferenceById(medicoId)).thenReturn(medico);
            when(citaRepository.save(any(Cita.class))).thenAnswer(inv -> {
                Cita c = inv.getArgument(0);
                c.setId(1L);
                return c;
            });

            // When
            CitaResponse response = citaService.createCita(requestMonday);

            // Then - should succeed because different date
            assertNotNull(response);
            assertEquals(monday, response.fecha());
        }
    }
}