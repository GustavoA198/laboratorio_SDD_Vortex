package com.clinica.repository;

import com.clinica.model.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA Repository for Cita entity.
 * Provides CRUD operations and custom queries for appointment management.
 */
@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    /**
     * Find all citas for a specific patient.
     *
     * @param pacienteId the patient ID
     * @return list of citas for the patient
     */
    List<Cita> findByPacienteId(String pacienteId);

    /**
     * Find all citas for a specific doctor.
     *
     * @param medicoId the doctor ID
     * @return list of citas for the doctor
     */
    List<Cita> findByMedicoId(String medicoId);
}
