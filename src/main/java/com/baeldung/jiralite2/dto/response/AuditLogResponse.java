package com.baeldung.jiralite2.dto.response;

import com.baeldung.jiralite2.domain.AuditLog;
import java.time.Instant;

public record AuditLogResponse(
    Long id,
    String eventType,
    String actor,
    Long projectId,
    Long taskId,
    String detail,
    Instant occurredAt
) {
    public static AuditLogResponse from(AuditLog a) {
        return new AuditLogResponse(
            a.getId(),
            a.getEventType().name(),
            a.getActor().getUsername(),
            a.getProject() != null ? a.getProject().getId() : null,
            a.getTask() != null ? a.getTask().getId() : null,
            a.getDetail(),
            a.getOccurredAt()
        );
    }
}
