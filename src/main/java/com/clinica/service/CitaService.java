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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Service layer for medical appointment (Cita) business logic.
 * Handles CRUD operations and domain validations.
 */
@Service
public class CitaService {

    private static final LocalTime HORA_INICIO = LocalTime.of(8, 0);
    private static final LocalTime HORA_FIN = LocalTime.of(17, 0);
    private static final int SLOT_MINUTOS = 30;

    private final CitaRepository citaRepository;
    private final PacienteRepository pacienteRepository;
    private final MedicoRepository medicoRepository;

    public CitaService(CitaRepository citaRepository,
                       PacienteRepository pacienteRepository,
                       MedicoRepository medicoRepository) {
        this.citaRepository = citaRepository;
        this.pacienteRepository = pacienteRepository;
        this.medicoRepository = medicoRepository;
    }

    /**
     * Creates a new medical appointment with validations.
     *
     * @param request the cita creation request
     * @return the created cita response
     * @throws BusinessValidationException if validation rules are violated
     */
    public CitaResponse createCita(CitaRequest request) {
        validarHorario(request.hora());
        validarSlot(request.hora());
        validarDiaHabil(request.fecha());
        validarExistenciaPaciente(request.pacienteId());
        validarExistenciaMedico(request.medicoId());

        Paciente paciente = pacienteRepository.getReferenceById(request.pacienteId());
        Medico medico = medicoRepository.getReferenceById(request.medicoId());

        Cita cita = Cita.builder()
                .paciente(paciente)
                .medico(medico)
                .fecha(request.fecha())
                .hora(request.hora())
                .estado(EstadoCita.ACTIVA)
                .motivoConsulta(request.motivoConsulta())
                .build();

        Cita saved = citaRepository.save(cita);
        return toResponse(saved);
    }

    /**
     * Retrieves a cita by its ID with ownership check.
     *
     * @param id the cita ID
     * @param username the username of the authenticated user
     * @param rol the role of the authenticated user
     * @return the cita response
     * @throws CitaNotFoundException if cita does not exist
     * @throws AccessDeniedException if user does not have access to this cita
     */
    public CitaResponse getCita(Long id, String username, String rol) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new CitaNotFoundException(id));

        validarOwnership(cita, username, rol);

        return toResponse(cita);
    }

    /**
     * Updates an existing cita only if it is in ACTIVA state, with ownership check.
     *
     * @param id the cita ID
     * @param request the update request
     * @param username the username of the authenticated user
     * @param rol the role of the authenticated user
     * @return the updated cita response
     * @throws CitaNotFoundException if cita does not exist
     * @throws IllegalStateException if cita is not in ACTIVA state
     * @throws BusinessValidationException if validation rules are violated
     * @throws AccessDeniedException if user does not have access to this cita
     */
    public CitaResponse updateCita(Long id, CitaRequest request, String username, String rol) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new CitaNotFoundException(id));

        validarOwnership(cita, username, rol);

        if (cita.getEstado() != EstadoCita.ACTIVA) {
            throw new IllegalStateException(
                "No se puede actualizar una cita que no está en estado ACTIVA. Estado actual: " + cita.getEstado()
            );
        }

        validarHorario(request.hora());
        validarSlot(request.hora());
        validarDiaHabil(request.fecha());
        validarExistenciaPaciente(request.pacienteId());
        validarExistenciaMedico(request.medicoId());

        Paciente paciente = pacienteRepository.getReferenceById(request.pacienteId());
        Medico medico = medicoRepository.getReferenceById(request.medicoId());

        cita.setPaciente(paciente);
        cita.setMedico(medico);
        cita.setFecha(request.fecha());
        cita.setHora(request.hora());
        cita.setMotivoConsulta(request.motivoConsulta());

        Cita updated = citaRepository.save(cita);
        return toResponse(updated);
    }

    /**
     * Cancels a cita by changing its state to CANCELADA with ownership check.
     *
     * @param id the cita ID
     * @param username the username of the authenticated user
     * @param rol the role of the authenticated user
     * @return the cancelled cita response
     * @throws CitaNotFoundException if cita does not exist
     * @throws AccessDeniedException if user does not have access to this cita
     */
    public CitaResponse cancelCita(Long id, String username, String rol) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new CitaNotFoundException(id));

        validarOwnership(cita, username, rol);

        cita.setEstado(EstadoCita.CANCELADA);
        Cita saved = citaRepository.save(cita);
        return toResponse(saved);
    }

    /**
     * Lists all citas (admin only).
     *
     * @return list of all cita responses
     */
    public List<CitaResponse> listCitas() {
        return citaRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Gets citas filtered by ACTIVA state for a specific patient.
     *
     * @param pacienteId the patient ID
     * @return list of active cita responses for the patient
     */
    public List<CitaResponse> getCitasPorPaciente(String pacienteId) {
        return citaRepository.findByPacienteId(pacienteId).stream()
                .filter(c -> c.getEstado() == EstadoCita.ACTIVA)
                .map(this::toResponse)
                .toList();
    }

    /**
     * Gets citas filtered by ACTIVA state for a specific doctor.
     *
     * @param medicoId the doctor ID
     * @return list of active cita responses for the doctor
     */
    public List<CitaResponse> getCitasPorMedico(String medicoId) {
        return citaRepository.findByMedicoId(medicoId).stream()
                .filter(c -> c.getEstado() == EstadoCita.ACTIVA)
                .map(this::toResponse)
                .toList();
    }

    /**
     * Validates that the user has ownership of the cita or is MEDICO/ADMIN.
     */
    private void validarOwnership(Cita cita, String username, String rol) {
        if ("MEDICO".equals(rol) || "ADMIN".equals(rol)) {
            return; // MEDICO and ADMIN can access any cita
        }

        // PACIENTE can only access their own citas
        if (!cita.getPaciente().getUsername().equals(username)) {
            throw new AccessDeniedException("No puedes ver esta cita");
        }
    }

    /**
     * Validates that the hora is within the allowed range (08:00-17:00).
     */
    private void validarHorario(LocalTime hora) {
        if (hora.isBefore(HORA_INICIO) || hora.isAfter(HORA_FIN)) {
            throw new BusinessValidationException(
                "Horario fuera de rango (08:00-17:00)"
            );
        }
    }

    /**
     * Validates that the hora is on a valid slot (:00 or :30).
     */
    private void validarSlot(LocalTime hora) {
        int minutos = hora.getMinute();
        if (minutos != 0 && minutos != 30) {
            throw new BusinessValidationException(
                "Duración debe ser 30 minutos en slots de 30 min"
            );
        }
    }

    /**
     * Validates that the fecha is a weekday (Monday-Friday).
     */
    private void validarDiaHabil(LocalDate fecha) {
        DayOfWeek day = fecha.getDayOfWeek();
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            throw new BusinessValidationException(
                "No se permiten citas en fins de semana"
            );
        }
    }

    /**
     * Validates that a patient exists by ID.
     *
     * @param pacienteId the patient ID
     * @throws BusinessValidationException if patient does not exist
     */
    private void validarExistenciaPaciente(String pacienteId) {
        if (!pacienteRepository.existsById(pacienteId)) {
            throw new BusinessValidationException(
                "Paciente no encontrado: " + pacienteId
            );
        }
    }

    /**
     * Validates that a doctor exists by ID.
     *
     * @param medicoId the doctor ID
     * @throws BusinessValidationException if doctor does not exist
     */
    private void validarExistenciaMedico(String medicoId) {
        if (!medicoRepository.existsById(medicoId)) {
            throw new BusinessValidationException(
                "Médico no encontrado: " + medicoId
            );
        }
    }

    /**
     * Converts a Cita entity to a CitaResponse DTO.
     */
    private CitaResponse toResponse(Cita cita) {
        return new CitaResponse(
            cita.getId(),
            cita.getPaciente().getId(),
            cita.getMedico().getId(),
            cita.getFecha(),
            cita.getHora(),
            cita.getEstado().name(),
            cita.getMotivoConsulta(),
            cita.getFechaCreacion()
        );
    }
}
