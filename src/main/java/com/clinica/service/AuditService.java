package com.clinica.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Service for audit logging of operations.
 * Provides structured logging for cita CRUD operations.
 */
@Service
public class AuditService {

    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");

    /**
     * Log a create operation.
     *
     * @param username the user performing the operation
     * @param citaId the ID of the created cita
     * @param details additional details about the operation
     */
    public void logCreate(String username, Long citaId, String details) {
        auditLogger.info("timestamp={} user={} operation={} details={}",
            LocalDateTime.now(), username, "CREATE", formatDetails(citaId, details));
    }

    /**
     * Log an update operation.
     *
     * @param username the user performing the operation
     * @param citaId the ID of the updated cita
     * @param details additional details about the operation
     */
    public void logUpdate(String username, Long citaId, String details) {
        auditLogger.info("timestamp={} user={} operation={} details={}",
            LocalDateTime.now(), username, "UPDATE", formatDetails(citaId, details));
    }

    /**
     * Log a cancel operation.
     *
     * @param username the user performing the operation
     * @param citaId the ID of the cancelled cita
     * @param details additional details about the operation
     */
    public void logCancel(String username, Long citaId, String details) {
        auditLogger.info("timestamp={} user={} operation={} details={}",
            LocalDateTime.now(), username, "CANCEL", formatDetails(citaId, details));
    }

    /**
     * Log a get/read operation.
     *
     * @param username the user performing the operation
     * @param citaId the ID of the cita being read
     */
    public void logGet(String username, Long citaId) {
        auditLogger.info("timestamp={} user={} operation={} details={}",
            LocalDateTime.now(), username, "GET", "citaId=" + citaId);
    }

    private String formatDetails(Long citaId, String details) {
        return "citaId=" + citaId + (details != null ? " " + details : "");
    }
}