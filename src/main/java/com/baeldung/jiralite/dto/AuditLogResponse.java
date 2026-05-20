package com.baeldung.jiralite.dto;

import com.baeldung.jiralite.domain.AuditEventType;
import com.baeldung.jiralite.domain.AuditLog;

import java.time.Instant;

public record AuditLogResponse(
    Long id,
    AuditEventType eventType,
    UserResponse actor,
    String entityType,
    Long entityId,
    Long projectId,
    Instant createdAt
) {
    public static AuditLogResponse from(AuditLog log) {
        return new AuditLogResponse(
            log.getId(),
            log.getEventType(),
            log.getActor() != null ? UserResponse.from(log.getActor()) : null,
            log.getEntityType(),
            log.getEntityId(),
            log.getProject() != null ? log.getProject().getId() : null,
            log.getCreatedAt()
        );
    }
}
