package com.clinica.controller;

import com.clinica.dto.MedicoResponse;
import com.clinica.service.MedicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for doctor (Medico) operations.
 * Provides retrieval endpoints for doctor management.
 */
@RestController
@RequestMapping("/api/v1/medicos")
@Tag(name = "Médicos", description = "Operaciones de consulta para médicos")
public class MedicoController {

    private final MedicoService medicoService;

    public MedicoController(MedicoService medicoService) {
        this.medicoService = medicoService;
    }

    /**
     * Retrieves a doctor by ID.
     *
     * @param id the doctor ID
     * @return 200 OK with doctor response, or 404 if not found
     */
    @GetMapping("/{id}")
    @Operation(summary = "Consultar médico", description = "Retorna un médico por su ID")
    public ResponseEntity<MedicoResponse> getMedico(@PathVariable String id) {
        return medicoService.getMedico(id)
                .map(medico -> new MedicoResponse(
                        medico.getId(),
                        medico.getNombre(),
                        medico.getEspecialidad(),
                        medico.getEmail(),
                        medico.getHorarioAtencion()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
