package com.clinica.service;

import com.clinica.model.Paciente;
import com.clinica.repository.PacienteRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service layer for patient (Paciente) business logic.
 * Handles patient retrieval operations.
 */
@Service
public class PacienteService {

    private final PacienteRepository pacienteRepository;

    public PacienteService(PacienteRepository pacienteRepository) {
        this.pacienteRepository = pacienteRepository;
    }

    /**
     * Retrieves a patient by ID.
     *
     * @param id the patient ID
     * @return Optional containing the patient if found
     */
    public Optional<Paciente> getPaciente(String id) {
        return pacienteRepository.findById(id);
    }

    /**
     * Checks if a patient exists by ID.
     *
     * @param id the patient ID
     * @return true if patient exists
     */
    public boolean existsById(String id) {
        return pacienteRepository.existsById(id);
    }
}
