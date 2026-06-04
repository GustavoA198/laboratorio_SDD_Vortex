package com.clinica.service;

import com.clinica.model.Medico;
import com.clinica.repository.MedicoRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service layer for doctor (Medico) business logic.
 * Handles doctor retrieval operations.
 */
@Service
public class MedicoService {

    private final MedicoRepository medicoRepository;

    public MedicoService(MedicoRepository medicoRepository) {
        this.medicoRepository = medicoRepository;
    }

    /**
     * Retrieves a doctor by ID.
     *
     * @param id the doctor ID
     * @return Optional containing the doctor if found
     */
    public Optional<Medico> getMedico(String id) {
        return medicoRepository.findById(id);
    }

    /**
     * Checks if a doctor exists by ID.
     *
     * @param id the doctor ID
     * @return true if doctor exists
     */
    public boolean existsById(String id) {
        return medicoRepository.existsById(id);
    }
}
