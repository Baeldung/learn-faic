package com.baeldung.jiralite.audit;

import java.time.Instant;

public record AuditEntryResponse(
        Long id,
        AuditEventType eventType,
        Long actorId,
        Long projectId,
        Long taskId,
        Instant timestamp) {
}
