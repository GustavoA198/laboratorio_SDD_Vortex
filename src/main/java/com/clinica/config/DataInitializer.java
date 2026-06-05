package com.clinica.config;

import com.clinica.model.Medico;
import com.clinica.model.Paciente;
import com.clinica.model.Usuario;
import com.clinica.repository.MedicoRepository;
import com.clinica.repository.PacienteRepository;
import com.clinica.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Seed data configuration for development/testing.
 * Only loads data when 'default' profile is active.
 */
@Configuration
@Profile("!test")
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(PacienteRepository pacienteRepository,
                                    MedicoRepository medicoRepository,
                                    UsuarioRepository usuarioRepository) {
        return args -> {
            // Only seed if database is empty
            if (pacienteRepository.count() == 0) {
                System.out.println("=== Loading seed data ===");
                
                // Create Pacientes
                Paciente pac1 = new Paciente();
                pac1.setId("PAC-001");
                pac1.setNombre("Juan Perez");
                pac1.setEmail("juan@email.com");
                pac1.setTelefono("1234567890");
                pac1.setFechaRegistro(java.time.LocalDate.now());
                pac1.setUsername("paciente1");
                pacienteRepository.save(pac1);

                Paciente pac2 = new Paciente();
                pac2.setId("PAC-002");
                pac2.setNombre("Maria Lopez");
                pac2.setEmail("maria@email.com");
                pac2.setTelefono("0987654321");
                pac2.setFechaRegistro(java.time.LocalDate.now());
                pac2.setUsername("paciente2");
                pacienteRepository.save(pac2);

                // Create Medicos
                Medico med1 = new Medico();
                med1.setId("MED-001");
                med1.setNombre("Dr. Garcia");
                med1.setEspecialidad("Medicina General");
                med1.setEmail("garcia@clinica.com");
                med1.setHorarioAtencion("08:00-17:00");
                medicoRepository.save(med1);

                Medico med2 = new Medico();
                med2.setId("MED-002");
                med2.setNombre("Dra. Rodriguez");
                med2.setEspecialidad("Pediatria");
                med2.setEmail("rodriguez@clinica.com");
                med2.setHorarioAtencion("08:00-17:00");
                medicoRepository.save(med2);

                // Create Usuarios
                Usuario usr1 = new Usuario();
                usr1.setId("USR-001");
                usr1.setUsername("paciente1");
                usr1.setPassword("password123");
                usr1.setRol("PACIENTE");
                usr1.setPacienteId("PAC-001");
                usuarioRepository.save(usr1);

                Usuario usr2 = new Usuario();
                usr2.setId("USR-002");
                usr2.setUsername("paciente2");
                usr2.setPassword("password123");
                usr2.setRol("PACIENTE");
                usr2.setPacienteId("PAC-002");
                usuarioRepository.save(usr2);

                Usuario usr3 = new Usuario();
                usr3.setId("USR-003");
                usr3.setUsername("admin");
                usr3.setPassword("admin123");
                usr3.setRol("ADMIN");
                usuarioRepository.save(usr3);

                Usuario usr4 = new Usuario();
                usr4.setId("USR-004");
                usr4.setUsername("medico1");
                usr4.setPassword("medico123");
                usr4.setRol("MEDICO");
                usr4.setMedicoId("MED-001");
                usuarioRepository.save(usr4);

                System.out.println("=== Seed data loaded successfully ===");
                System.out.println("Pacientes: " + pacienteRepository.count());
                System.out.println("Medicos: " + medicoRepository.count());
                System.out.println("Usuarios: " + usuarioRepository.count());
            } else {
                System.out.println("=== Database already has data, skipping seed ===");
            }
        };
    }
}