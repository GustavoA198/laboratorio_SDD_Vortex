package com.clinica.repository;

import com.clinica.model.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA Repository for Cita entity.
 * Provides CRUD operations and custom queries for appointment management.
 */
@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {
}