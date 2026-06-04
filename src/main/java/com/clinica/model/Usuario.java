package com.clinica.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing a user (Usuario) in the clinical system.
 * Used for JWT authentication and role-based access control.
 */
@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    /**
     * Unique identifier for the user (same as username).
     */
    @Id
    private String id;

    /**
     * Unique username for authentication.
     */
    private String username;

    /**
     * Password for authentication (simple comparison for demo purposes).
     */
    private String password;

    /**
     * Role of the user: PACIENTE, MEDICO, or ADMIN.
     */
    private String rol;

    /**
     * Reference to Paciente entity (only for PACIENTE role).
     */
    private String pacienteId;

    /**
     * Reference to Medico entity (only for MEDICO role).
     */
    private String medicoId;
}