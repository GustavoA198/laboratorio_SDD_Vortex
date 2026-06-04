# PLAN DE IMPLEMENTACIÓN - Microservicio Citas Médicas

## Stack Tecnológico Confirmado

| Componente | Tecnología | Versión |
|------------|------------|---------|
| Framework | Spring Boot | 4.0.x (4.0.6 - GA) |
| Seguridad | Spring Security + JWT | 7.x |
| Persistencia | Spring Data JPA | 4.0.x |
| Base de Datos | H2 (en memoria) | 2.3.x |
| Utilidades | Lombok | 1.18.x |
| Testing | JUnit 5 + Mockito | 5.x |
| Documentación | OpenAPI/SpringDoc | 2.8.x |
| Build | Maven | - |
| Puerto | 8080 (default) | - |

## Proyecto Maven

- **groupId**: `com.clinica`
- **artifactId**: `microservicio-clinica`
- **Estructura**: Por capa (`com.clinica.model`, `controller`, `service`, `repository`, `security`, `config`, `exception`)
- **Logging**: Controllers + Services + componentes clave
- **Swagger UI**: Habilitado (`/swagger-ui.html`)
- **Base de datos**: Schema creado desde cero

---

## ROADMAP DE LOS 4 CHANGES (Features)

> **NOTA**: Cada feature usa las 9 fases SDD completas (explore → propose → spec → design → tasks → tdd → implement → verify → archive)

---

### CHANGE 1: CRUD Básico de Citas

| Fase | Artefactos a Generar | Descripción |
|------|---------------------|-------------|
| **explore** | `CHANGE-1-explore.md` | Investigar dominio, mapear entidades Cita, entender reglas negocio |
| **propose** | `CHANGE-1-propose.md` | Propuesta formal con scope, constraints, enfoque técnico |
| **spec** | `CHANGE-1-spec.md` | BDD scenarios para Cita (crear, consultar, actualizar, cancelar) |
| **design** | `CHANGE-1-design.md` | Diagrama arquitectura, modelo datos, endpoints REST |
| **tasks** | `CHANGE-1-tasks.md` | Breaking down en tareas implementables |
| **tdd** | Tests unitarios | Tests para repository y service (CITAS-001 a CITAS-006) |
| **implement** | Código completo | Repos, Services, Controllers, DTOs, Exception handlers |
| **verify** | Reporte verificación | Tests verdes, coverage, smoke tests |
| **archive** | Delta specs + memoria | Persistir aprendizajes en Engram |

#### Scope Feature 1
- Modelos: `Cita` con enum `EstadoCita` (ACTIVA, CANCELADA, COMPLETADA)
- Endpoints REST:
  - `POST /api/v1/citas` - Crear cita
  - `GET /api/v1/citas/{id}` - Consultar por ID
  - `PUT /api/v1/citas/{id}` - Actualizar (solo si ACTIVA)
  - `DELETE /api/v1/citas/{id}` - Cancelar (cambia a CANCELADA)
- Validaciones básicas de dominio:
  - Horario 08:00-17:00
  - Duración 30 minutos
  - Solo días hábiles (L-V)
- Base de datos H2 con schema creado desde cero
- **Sin JWT** (se agrega en Change 3)

---

### CHANGE 2: Gestión de Usuarios

| Fase | Artefactos a Generar | Descripción |
|------|---------------------|-------------|
| **explore** | `CHANGE-2-explore.md` | Analizar modelos Paciente y Medico, relaciones con Cita |
| **propose** | `CHANGE-2-propose.md` | Propuesta: cómo vincular usuarios a citas |
| **spec** | `CHANGE-2-spec.md` | BDD para gestión de usuarios y endpoints relacionados |
| **design** | `CHANGE-2-design.md` | Diagrama relaciones, modelo de datos ampliado |
| **tasks** | `CHANGE-2-tasks.md` | Tareas para nuevos modelos y endpoints |
| **tdd** | Tests unitarios | Tests para PacienteService, MedicoService |
| **implement** | Código completo | Modelos Paciente/Medico, nuevos endpoints |
| **verify** | Reporte verificación | Tests verdes, smoke tests |
| **archive** | Delta specs + memoria | Persistir aprendizajes en Engram |

#### Scope Feature 2
- Modelos:
  - `Paciente`: id, nombre, email, telefono, fechaRegistro
  - `Medico`: id, nombre, especialidad, email, horarioAtencion
- Endpoints REST:
  - `GET /api/v1/pacientes/{id}` - Consultar paciente
  - `GET /api/v1/medicos/{id}` - Consultar médico
  - `GET /api/v1/citas/mis-citas` - Listar citas del paciente autenticado
  - `GET /api/v1/citas/medico/{medicoId}` - Listar citas de un médico
- Vincular `Cita.pacienteId` y `Cita.medicoId` a los nuevos modelos

---

### CHANGE 3: Seguridad JWT

| Fase | Artefactos a Generar | Descripción |
|------|---------------------|-------------|
| **explore** | `CHANGE-3-explore.md` | Analizar vulnerabilidades sin auth, mapear vectores de ataque |
| **propose** | `CHANGE-3-propose.md` | Propuesta: JWT con roles PACIENTE/MEDICO/ADMIN |
| **spec** | `CHANGE-3-spec.md` | BDD: autenticación, autorización por rol, validación token |
| **design** | `CHANGE-3-design.md` | Filtro JWT, configuración Spring Security, header Auth |
| **tasks** | `CHANGE-3-tasks.md` | Desglose: filtro, utilería JWT, config security, tests |
| **tdd** | Tests unitarios | Tests: token válido/inválido, rol autorizado/denegado |
| **implement** | Código completo | Security config, JWT filter, @Secured annotations |
| **verify** | Reporte verificación | Verificar todos los endpoints requieren auth |
| **archive** | Delta specs + memoria | Persistir aprendizajes en Engram |

#### Scope Feature 3
- Endpoint autenticación: `POST /api/v1/auth/login` (retorna token JWT)
- Filtro JWT que intercepta todas las requests
- Roles: `PACIENTE`, `MEDICO`, `ADMIN`
- Validaciones de seguridad:
  - Solo `PACIENTE` puede crear citas (POST)
  - Solo `MEDICO` puede ver todas las citas de un médico
  - Paciente solo ve/cancela sus propias citas
  - `ADMIN` puede ver cualquier cita
- Header: `Authorization: Bearer <token>`
- Registro de usuario en logs (audit trail)

---

### CHANGE 4: Validaciones Avanzadas

| Fase | Artefactos a Generar | Descripción |
|------|---------------------|-------------|
| **explore** | `CHANGE-4-explore.md` | Mapear gaps: límite 3 citas, anti-duplicados, rate limit, audit |
| **propose** | `CHANGE-4-propose.md` | Propuesta: validaciones que faltan |
| **spec** | `CHANGE-4-spec.md` | BDD: 3 citas max, no duplicados, rate limit, audit logs |
| **design** | `CHANGE-4-design.md` | Interceptores, filtros, arquitectura de validaciones |
| **tasks** | `CHANGE-4-tasks.md` | Desglose de tareas de implementación |
| **tdd** | Tests unitarios | Tests: límite 3, anti-duplicados, rate limit |
| **implement** | Código completo | Validators, RateLimitFilter, AuditLogger |
| **verify** | Reporte verificación | Tests de cada validación, smoke tests |
| **archive** | Delta specs + memoria | Persistir aprendizajes en Engram |

#### Scope Feature 4
- **Límite 3 citas activas** por paciente (no cuentan CANCELADA/COMPLETADA)
- **Anti-duplicados**: mismo `medicoId` + mismo `fecha` + mismo `hora` → error 409 Conflict
- **Rate limiting**: máximo 10 requests/minuto por usuario (implementación simple con ConcurrentHashMap)
- **Audit logs**: timestamp + usuario + operación en controllers y services
- **Validaciones de horario**:
  - Horario atención: 08:00-17:00
  - Solo lunes a viernes
  - Duración exacta de 30 minutos por cita

---

## ESTRUCTURA DEL PROYECTO (resultado final)

```
microservicio-clinica/
├── pom.xml
├── src/main/java/com/clinica/
│   ├── MicroservicioClinicaApplication.java
│   ├── model/
│   │   ├── Cita.java
│   │   ├── Paciente.java
│   │   ├── Medico.java
│   │   ├──Usuario.java
│   │   └── enums/EstadoCita.java
│   ├── repository/
│   │   ├── CitaRepository.java
│   │   ├── PacienteRepository.java
│   │   ├── MedicoRepository.java
│   │   └── UsuarioRepository.java
│   ├── service/
│   │   ├── CitaService.java
│   │   ├── PacienteService.java
│   │   ├── MedicoService.java
│   │   └── AuthService.java
│   ├── controller/
│   │   ├── CitaController.java
│   │   ├── PacienteController.java
│   │   ├── MedicoController.java
│   │   └── AuthController.java
│   ├── security/
│   │   ├── JwtTokenProvider.java
│   │   ├── JwtAuthenticationFilter.java
│   │   └── SecurityConfig.java
│   ├── config/
│   │   ├── OpenApiConfig.java
│   │   └── WebConfig.java
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java
│   │   ├── CitaNotFoundException.java
│   │   ├── BusinessValidationException.java
│   │   └── DuplicateCitaException.java
│   ├── dto/
│   │   ├── CitaRequest.java
│   │   ├── CitaResponse.java
│   │   ├── AuthRequest.java
│   │   └── AuthResponse.java
│   ├── validation/
│   │   └── RateLimitFilter.java
│   ├── audit/
│   │   └── AuditLogger.java
│   └── filter/
│       └── RateLimitFilter.java
├── src/main/resources/
│   ├── application.properties
│   └── data.sql (seed data para testing)
└── src/test/java/com/clinica/
    ├── service/
    │   └── CitaServiceTest.java
    ├── controller/
    │   └── CitaControllerTest.java
    └── security/
        ├── JwtTokenProviderTest.java
        └── JwtAuthenticationFilterTest.java
```

---

## MODELO DE DATOS (final)

### Cita
```
- id: Long
- pacienteId: String
- medicoId: String
- fecha: LocalDate
- hora: LocalTime
- estado: EstadoCita (ACTIVA/CANCELADA/COMPLETADA)
- motivoConsulta: String
- fechaCreacion: LocalDateTime
```

### Paciente
```
- id: String
- nombre: String
- email: String
- telefono: String
- fechaRegistro: LocalDate
```

### Medico
```
- id: String
- nombre: String
- especialidad: String
- email: String
- horarioAtencion: String
```

### Usuario
```
- id: String
- username: String
- password: String (encoded)
- rol: Rol (PACIENTE/MEDICO/ADMIN)
- pacienteId: String (nullable, para PACIENTE)
- medicoId: String (nullable, para MEDICO)
```

---

## ENDPOINTS API REST (final)

| Método | Endpoint | Descripción | Auth Requerida |
|--------|----------|-------------|----------------|
| POST | `/api/v1/auth/login` | Iniciar sesión, obtener JWT | No |
| POST | `/api/v1/citas` | Crear nueva cita | JWT - Rol PACIENTE |
| GET | `/api/v1/citas/{id}` | Consultar cita por ID | JWT - Dueño, Médico o Admin |
| GET | `/api/v1/citas/mis-citas` | Listar mis citas | JWT - Cualquier rol |
| GET | `/api/v1/citas/medico/{medicoId}` | Listar citas de un médico | JWT - Rol MEDICO o ADMIN |
| PUT | `/api/v1/citas/{id}` | Actualizar cita (solo si ACTIVA) | JWT - Dueño |
| DELETE | `/api/v1/citas/{id}` | Cancelar cita | JWT - Dueño |
| GET | `/api/v1/pacientes/{id}` | Consultar paciente | JWT - Cualquier rol |
| GET | `/api/v1/medicos/{id}` | Consultar médico | JWT - Cualquier rol |

---

## REGLAS DE NEGOCIO IMPLEMENTADAS

| # | Regla | Implementada en |
|---|-------|----------------|
| 1 | Paciente no puede tener más de 3 citas activas | Change 4 |
| 2 | Citas solo en horario 08:00-17:00 | Change 1 |
| 3 | Cita dura exactamente 30 minutos | Change 1 |
| 4 | No citas en fines de semana | Change 1 |
| 5 | Solo PACIENTE puede crear citas | Change 3 |
| 6 | Solo MEDICO puede ver todas las citas | Change 3 |
| 7 | Paciente solo ve/cancela sus propias citas | Change 3 |
| 8 | No citas duplicadas (mismo médico, mismo horario) | Change 4 |
| - | Rate limiting: 10 req/min por usuario | Change 4 |
| - | Audit logs con timestamp y usuario | Change 4 |

---

## TIMELINE ESTIMADO

| Change | Fases | Tiempo Est. |
|--------|-------|-------------|
| **CHANGE 1** | 9 fases | ~4-6 horas |
| **CHANGE 2** | 9 fases | ~4-6 horas |
| **CHANGE 3** | 9 fases | ~4-6 horas |
| **CHANGE 4** | 9 fases | ~4-6 horas |
| **TOTAL** | **36 fases** | **~16-24 horas** |

---

## PRÓXIMO PASO

Para iniciar con Change 1, ejecutar en Vorkan:

```bash
vorkan start --type FEATURE --title "CRUD Básico de Citas - Change 1"
```

**Nota**: Los changes se ejecutan secuencialmente. Un change se inicia solo cuando el anterior está completamente archivado (phase = archive, status = SUCCESS).