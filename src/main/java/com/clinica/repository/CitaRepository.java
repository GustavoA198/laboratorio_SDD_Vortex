package com.clinica.repository;

import com.clinica.model.Cita;
import com.clinica.model.enums.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
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

    /**
     * Count active citas for a patient.
     *
     * @param pacienteId the patient ID
     * @param estado the cita state
     * @return count of citas matching the criteria
     */
    long countByPacienteIdAndEstado(String pacienteId, EstadoCita estado);

    /**
     * Check if a cita already exists for a doctor at a specific date and time.
     *
     * @param medicoId the doctor ID
     * @param fecha the appointment date
     * @param hora the appointment time
     * @return true if a cita exists at this time slot
     */
    boolean existsByMedicoIdAndFechaAndHora(String medicoId, LocalDate fecha, LocalTime hora);
}
