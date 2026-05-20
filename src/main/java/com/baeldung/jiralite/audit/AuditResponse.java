package com.baeldung.jiralite.audit;

import java.time.Instant;

public record AuditResponse(
        Long id,
        AuditEventType eventType,
        Long actorId,
        AuditEntityType entityType,
        Long entityId,
        Long projectId,
        Instant timestamp,
        String details) { }
