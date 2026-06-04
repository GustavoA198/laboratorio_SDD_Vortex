# LABORATORIO SDD - MICROSERVICIO DE CITAS MÉDICAS

Ejercicio Práctico de Spec-Driven Development

Fecha: | Grupo: | Integrantes:

## INSTRUCCIONES PARA EL EQUIPO:

1. Lean TODOS los requisitos de este documento cuidadosamente antes de iniciar 2. Usen Vorkan para ejecutar las 9 fases SDD: explore → propose → spec → design → tasks → tdd → implement → verify → archive 3. Documenten cada fase con sus respectivos artifacts Open Spec 4. Preparen una demostración de 3 minutos al final del laboratorio 5. Importante: Asegúrense de comprender completamente el dominio antes de generar especificaciones

## ESTRATEGIA: DIVISIÓN EN FEATURES INDEPENDIENTES

IMPORTANTE: No implementen todo el sistema en un único change. SDD promueve cambios atómicos y revisables. Dividan el sistema en los siguientes 4 features independientes:

| # | Feature / Change | Contenido | Pipeline SDD |
|---|----------------|-----------|--------------|
| 1 | CRUD Básico de Citas | POST/GET cita, modelo Cita, validaciones básicas de negocio (3) | CRUD de Citas (3) |
| 2 | Gestión de Usuarios | Modelos Paciente/Medico, GET /mis-citas, GET /medico/(id) | Reducido (6 fases) |
| 3 | Seguridad JWT | Autenticación JWT, roles PACIENTE/MEDICO, headers Autorización (3) | Reducido (9 fases) |
| 4 | Validaciones Avanzadas | Límite 3 citas, anti-duplicados, rate limiting, audit logs | Reducido (5 fases) |

Nota: Cada feature debe ser un change independiente en Vorkan. Al final del laboratorio deben tener 4 changes completados en Engram (uno por feature). El Feature 1 debe usar pipeline completo; los demás pueden usar pipelines reducidos si el contexto ya está claro.

## CONTEXTO DEL NEGOCIO

===== Page 2 =====

 ¡¢£¤¥¦§¨©ª«¬­®¯°±²³´µ¶·¸¹º»¼½¾¿ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿ

Eres el líder técnico de un equipo que debe desarrollar un microservicio para una clínica médica. El sistema debe permitir a los pacientes agendar, consultar y cancelar citas con médicos específicos.

La clínica atiende de lunes a viernes en horario de 08:00 a 17:00 horas. Cada cita médica tiene una duración exacta de 30 minutos. El sistema debe garantizar que no existan citas duplicadas (sobrepuestas) para un mismo médico en el mismo horario.

## REGLAS DE NEGOCIO OBLIGATORIAS

| # | Regla de Negocio | Tipo |
|---|------------------|------|
| 1 | Un paciente NO puede tener más de 3 citas activas simultáneas | Validación |
| 2 | Las citas solo se pueden agendar en horario de 08:00 a 17:00 | Validación |
| 3 | Una cita dura exactamente 30 minutos | Dominio |
| 4 | No se puede agendar citas en fines de semana (sábado/domingo) | Validación |
| 5 | Solo usuarios autenticados con rol "PACIENTE" pueden crear citas | Seguridad |
| 6 | Solo usuarios autenticados con rol "MEDICO" pueden ver todas las citas | Seguridad |
| 7 | Un paciente solo puede ver/cancelar sus PROPIAS citas | Seguridad |
| 8 | No se permiten citas duplicadas (mismo médico, mismo horario) | Validación |

## MODELO DE DATOS REQUERIDO

Cita: id (Long), pacienteld (String), medicold (String), fecha (LocalDate), hora (LocalTime), estado (Enum: ACTIVA/CANCELADA/COMPLETADA), motivoConsulta (String), fechaCreacion (LocalDateTime)

Paciente: id (String), nombre (String), email (String), telefono (String), fechaRegistro (LocalDate)

Medico: id (String), nombre (String), especialidad (String), email (String), horarioAtencion (String)

## ENDPOINTS API REST REQUERIDOS

| Método | Endpoint | Descripción | Autenticación |
|--------|----------|-------------|----------------|
| POST | /api/v1/citas | Crear nueva cita | JWT - Rol PACIENTE |
| GET | /api/v1/citas/{id} | Consultar cita por ID | JWT - Dueño o Médico |
| GET | /api/v1/citas/mis-citas | Listar citas del paciente autenticado | JWT - Cualquier rol |
| GET | /api/v1/citas/medico/{medicold} | Listar citas de un médico específico | JWT - Rol MEDICO |
| PUT | /api/v1/citas/{id} | Actualizar datos de cita (solo si está ACTIVA) | JWT - Dueño |
| DELETE | /api/v1/citas/{id} | Cancelar cita (cambia estado a CANCELADA) | JWT - Dueño |

===== Page 3 =====

 ¡¢£¤¥¦§¨©ª«¬­®¯°±²³´µ¶·¸¹º»¼½¾¿ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿ

## VALIDACIONES DE SEGURIDAD OBLIGATORIAS

• Todos los endpoints deben validar JWT en header Authorization

• Validar que el pacienteld en la cita corresponda al usuario autenticado (token JWT)

• Validar que solo el médico asignado o el paciente dueño puedan ver una cita específica

• Registrar en logs todas las operaciones con timestamp y usuario (audit trail)

• Implementar rate limiting: máximo 10 requests por minuto por usuario

• Validar que el usuario autenticado tenga el rol requerido para cada operación

## STACK TECNOLÓGICO

| Componente | Tecnología | Versión |
|------------|------------|---------|
| Framework | Spring Boot | 4.0.x (4.0.6 - GA) |
| Seguridad | Spring Security + JWT | 7.x |
| Persistencia | Spring Data JPA | 4.0.x |
| Base de Datos | H2 (en memoria) | 2.3.x |
| Utilidades | Lombok | 1.18.x |
| Testing | JUnit 5 + Mockito | 5.x |
| Documentación | OpenAPI/SpringDoc | 2.8.x |

Nota: Este documento contiene todos los requisitos necesarios para completar el ejercicio. Asegúrate de revisar cada sección detenidamente antes de comenzar con Vorkan.