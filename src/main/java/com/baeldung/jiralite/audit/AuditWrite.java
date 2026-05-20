package com.baeldung.jiralite.audit;

public record AuditWrite(
        AuditEventType eventType,
        AuditEntityType entityType,
        Long entityId,
        Long projectId,
        String details) { }
