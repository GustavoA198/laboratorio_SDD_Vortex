package com.clinica.controller;

import com.clinica.dto.PacienteResponse;
import com.clinica.service.PacienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for patient (Paciente) operations.
 * Provides retrieval endpoints for patient management.
 */
@RestController
@RequestMapping("/api/v1/pacientes")
@Tag(name = "Pacientes", description = "Operaciones de consulta para pacientes")
public class PacienteController {

    private final PacienteService pacienteService;

    public PacienteController(PacienteService pacienteService) {
        this.pacienteService = pacienteService;
    }

    /**
     * Retrieves a patient by ID.
     *
     * @param id the patient ID
     * @return 200 OK with patient response, or 404 if not found
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PACIENTE', 'ADMIN')")
    @Operation(summary = "Consultar paciente", description = "Retorna un paciente por su ID")
    public ResponseEntity<PacienteResponse> getPaciente(@PathVariable String id) {
        return pacienteService.getPaciente(id)
                .map(paciente -> new PacienteResponse(
                        paciente.getId(),
                        paciente.getNombre(),
                        paciente.getEmail(),
                        paciente.getTelefono(),
                        paciente.getFechaRegistro()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
