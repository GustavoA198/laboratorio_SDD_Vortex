package com.clinica;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Clinica Microservicio REST application.
 * Handles medical appointment scheduling and state management.
 */
@SpringBootApplication
public class MicroservicioClinicaApplication {

    public static void main(String[] args) {
        SpringApplication.run(MicroservicioClinicaApplication.class, args);
    }
