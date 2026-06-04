package com.clinica.repository;

import com.clinica.model.Medico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA Repository for Medico entity.
 * Provides CRUD operations for doctor management.
 */
@Repository
public interface MedicoRepository extends JpaRepository<Medico, String> {
}
