# Proposal: CHANGE-4 - Validaciones Avanzadas

## Intent

Implementar las reglas de negocio faltantes: límite de 3 citas activas, anti-duplicados, rate limiting y audit logs.

## Scope

### In Scope
- Límite 3 citas activas por paciente (solo ACTIVA, no CANCELADA/COMPLETADA)
- Anti-duplicados (mismo médico + misma fecha + misma hora)
- Rate limiting (10 req/min por usuario)
- Audit logs (timestamp + usuario + operación)

### Out of Scope
- Token refresh
- Token revocation

## Capabilities

### New Capabilities
- `validacion-citas`: Validaciones avanzadas de citas (max 3 activas, anti-duplicados)
- `rate-limiting`: Rate limiting por usuario (10 req/min)
- `audit-logging`: Registro de auditoría (timestamp + usuario + operación)

### Modified Capabilities
- None

## Approach

Repository query para contar citas activas y verificar duplicados. Filtro simple ConcurrentHashMap para rate limiting. SLF4J Logger para audit logs.

## Affected Areas

| Area | Impact | Description |
|------|--------|-------------|
| `CitaRepository.java` | Modified | Add count/sist methods |
| `CitaService.java` | Modified | Add validations |
| `RateLimitFilter.java` | New | Rate limiting |
| `AuditLogger.java` | New | Audit logging |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| Memory leak in rate limit | Low | TTL cleanup |
| Race condition | Low | Acceptable for MVP |

## Rollback Plan

Revertir cambios en CitaRepository, CitaService. Eliminar RateLimitFilter y AuditLogger. Eliminar handlers de GlobalExceptionHandler para 429 y 409.

## Dependencies

- None

## Success Criteria

- [ ] Max 3 citas activas enforced
- [ ] No duplicate citas (409 Conflict)
- [ ] Rate limiting returns 429 when exceeded
- [ ] All operations logged
