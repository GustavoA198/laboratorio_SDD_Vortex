package com.clinica.service;

import com.clinica.dto.CitaRequest;
import com.clinica.dto.CitaResponse;
import com.clinica.exception.BusinessValidationException;
import com.clinica.exception.CitaNotFoundException;
import com.clinica.model.Cita;
import com.clinica.model.enums.EstadoCita;
import com.clinica.repository.CitaRepository;
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

    public CitaService(CitaRepository citaRepository) {
        this.citaRepository = citaRepository;
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

        Cita cita = Cita.builder()
                .pacienteId(request.pacienteId())
                .medicoId(request.medicoId())
                .fecha(request.fecha())
                .hora(request.hora())
                .estado(EstadoCita.ACTIVA)
                .motivoConsulta(request.motivoConsulta())
                .build();

        Cita saved = citaRepository.save(cita);
        return toResponse(saved);
    }

    /**
     * Retrieves a cita by its ID.
     *
     * @param id the cita ID
     * @return the cita response
     * @throws CitaNotFoundException if cita does not exist
     */
    public CitaResponse getCita(Long id) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new CitaNotFoundException(id));
        return toResponse(cita);
    }

    /**
     * Updates an existing cita only if it is in ACTIVA state.
     *
     * @param id the cita ID
     * @param request the update request
     * @return the updated cita response
     * @throws CitaNotFoundException if cita does not exist
     * @throws IllegalStateException if cita is not in ACTIVA state
     * @throws BusinessValidationException if validation rules are violated
     */
    public CitaResponse updateCita(Long id, CitaRequest request) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new CitaNotFoundException(id));

        if (cita.getEstado() != EstadoCita.ACTIVA) {
            throw new IllegalStateException(
                "No se puede actualizar una cita que no está en estado ACTIVA. Estado actual: " + cita.getEstado()
            );
        }

        validarHorario(request.hora());
        validarSlot(request.hora());
        validarDiaHabil(request.fecha());

        cita.setPacienteId(request.pacienteId());
        cita.setMedicoId(request.medicoId());
        cita.setFecha(request.fecha());
        cita.setHora(request.hora());
        cita.setMotivoConsulta(request.motivoConsulta());

        Cita updated = citaRepository.save(cita);
        return toResponse(updated);
    }

    /**
     * Cancels a cita by changing its state to CANCELADA.
     *
     * @param id the cita ID
     * @return the cancelled cita response
     * @throws CitaNotFoundException if cita does not exist
     */
    public CitaResponse cancelCita(Long id) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new CitaNotFoundException(id));

        cita.setEstado(EstadoCita.CANCELADA);
        Cita saved = citaRepository.save(cita);
        return toResponse(saved);
    }

    /**
     * Lists all citas.
     *
     * @return list of all cita responses
     */
    public List<CitaResponse> listCitas() {
        return citaRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
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
     * Converts a Cita entity to a CitaResponse DTO.
     */
    private CitaResponse toResponse(Cita cita) {
        return new CitaResponse(
            cita.getId(),
            cita.getPacienteId(),
            cita.getMedicoId(),
            cita.getFecha(),
            cita.getHora(),
            cita.getEstado().name(),
            cita.getMotivoConsulta(),
            cita.getFechaCreacion()
        );
    }
}