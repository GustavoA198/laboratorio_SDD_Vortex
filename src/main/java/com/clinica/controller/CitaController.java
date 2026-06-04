package com.clinica.controller;

import com.clinica.dto.CitaRequest;
import com.clinica.dto.CitaResponse;
import com.clinica.service.CitaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for medical appointment (Cita) operations.
 * Provides CRUD endpoints for appointment management.
 */
@RestController
@RequestMapping("/api/v1/citas")
@Tag(name = "Citas", description = "Operaciones CRUD para citas médicas")
public class CitaController {

    private final CitaService citaService;

    public CitaController(CitaService citaService) {
        this.citaService = citaService;
    }

    /**
     * Creates a new medical appointment.
     *
     * @param request the cita creation request
     * @return 201 Created with the created cita response
     */
    @PostMapping
    @Operation(summary = "Crear nueva cita", description = "Crea una nueva cita médica en estado ACTIVA")
    public ResponseEntity<CitaResponse> createCita(@Valid @RequestBody CitaRequest request) {
        CitaResponse response = citaService.createCita(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves a cita by its ID.
     *
     * @param id the cita ID
     * @return 200 OK with the cita response, or 404 if not found
     */
    @GetMapping("/{id}")
    @Operation(summary = "Consultar cita", description = "Retorna una cita por su ID")
    public ResponseEntity<CitaResponse> getCita(@PathVariable Long id) {
        CitaResponse response = citaService.getCita(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Lists all citas.
     *
     * @return 200 OK with list of all citas (may be empty)
     */
    @GetMapping
    @Operation(summary = "Listar todas las citas", description = "Retorna una lista de todas las citas")
    public ResponseEntity<List<CitaResponse>> listCitas() {
        List<CitaResponse> responses = citaService.listCitas();
        return ResponseEntity.ok(responses);
    }

    /**
     * Updates an existing cita (only if ACTIVA).
     *
     * @param id the cita ID
     * @param request the update request
     * @return 200 OK with updated cita, 404 if not found, 409 if not ACTIVA
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar cita", description = "Actualiza una cita existente solo si está en estado ACTIVA")
    public ResponseEntity<CitaResponse> updateCita(
            @PathVariable Long id,
            @Valid @RequestBody CitaRequest request) {
        CitaResponse response = citaService.updateCita(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Cancels a cita (changes state to CANCELADA).
     *
     * @param id the cita ID
     * @return 204 No Content on success, 404 if not found
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Cancelar cita", description = "Cambia el estado de una cita a CANCELADA")
    public ResponseEntity<Void> cancelCita(@PathVariable Long id) {
        citaService.cancelCita(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Lists all ACTIVE citas for a specific patient.
     *
     * @param pacienteId the patient ID
     * @return 200 OK with list of active citas for the patient
     */
    @GetMapping("/mis-citas")
    @Operation(summary = "Mis citas", description = "Retorna las citas activas de un paciente")
    public ResponseEntity<List<CitaResponse>> getMisCitas(@RequestParam String pacienteId) {
        List<CitaResponse> responses = citaService.getCitasPorPaciente(pacienteId);
        return ResponseEntity.ok(responses);
    }

    /**
     * Lists all ACTIVE citas for a specific doctor.
     *
     * @param medicoId the doctor ID
     * @return 200 OK with list of active citas for the doctor
     */
    @GetMapping("/medico/{medicoId}")
    @Operation(summary = "Citas por médico", description = "Retorna las citas activas de un médico")
    public ResponseEntity<List<CitaResponse>> getCitasPorMedico(@PathVariable String medicoId) {
        List<CitaResponse> responses = citaService.getCitasPorMedico(medicoId);
        return ResponseEntity.ok(responses);
    }
}
