-- ============================================
-- SEED DATA - Datos de prueba para testing
-- ============================================

-- Insertar pacientes
INSERT INTO pacientes (id, nombre, email, telefono, fecha_registro, username) 
VALUES ('PAC-001', 'Juan Perez', 'juan@email.com', '1234567890', '2026-01-01', 'paciente1');

INSERT INTO pacientes (id, nombre, email, telefono, fecha_registro, username) 
VALUES ('PAC-002', 'Maria Lopez', 'maria@email.com', '0987654321', '2026-01-15', 'paciente2');

-- Insertar medicos
INSERT INTO medicos (id, nombre, especialidad, email, horario_atencion) 
VALUES ('MED-001', 'Dr. Garcia', 'Medicina General', 'garcia@clinica.com', '08:00-17:00');

INSERT INTO medicos (id, nombre, especialidad, email, horario_atencion) 
VALUES ('MED-002', 'Dra. Rodriguez', 'Pediatria', 'rodriguez@clinica.com', '08:00-17:00');

-- Insertar usuarios (para login)
INSERT INTO usuarios (id, username, password, rol, paciente_id, medico_id) 
VALUES ('USR-001', 'paciente1', 'password123', 'PACIENTE', 'PAC-001', NULL);

INSERT INTO usuarios (id, username, password, rol, paciente_id, medico_id) 
VALUES ('USR-002', 'paciente2', 'password123', 'PACIENTE', 'PAC-002', NULL);

INSERT INTO usuarios (id, username, password, rol, paciente_id, medico_id) 
VALUES ('USR-003', 'admin', 'admin123', 'ADMIN', NULL, NULL);

INSERT INTO usuarios (id, username, password, rol, paciente_id, medico_id) 
VALUES ('USR-004', 'medico1', 'medico123', 'MEDICO', NULL, 'MED-001');