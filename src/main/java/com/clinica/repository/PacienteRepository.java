package com.clinica.repository;

import com.clinica.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA Repository for Paciente entity.
 * Provides CRUD operations for patient management.
 */
@Repository
public interface PacienteRepository extends JpaRepository<Paciente, String> {
}
