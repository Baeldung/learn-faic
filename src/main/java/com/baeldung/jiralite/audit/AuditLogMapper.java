package com.baeldung.jiralite.audit;

import org.springframework.stereotype.Component;

@Component
public class AuditLogMapper {

    public AuditEntryResponse toResponse(AuditLog log) {
        return new AuditEntryResponse(
                log.getId(),
                log.getEventType(),
                log.getActor().getId(),
                log.getProjectId(),
                log.getTaskId(),
                log.getTimestamp());
    }
}
