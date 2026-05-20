package com.baeldung.jiralite.audit;

import org.springframework.stereotype.Component;

@Component
public class AuditMapper {

    public AuditResponse toResponse(AuditEntry entry) {
        return new AuditResponse(
                entry.getId(),
                entry.getEventType(),
                entry.getActor().getId(),
                entry.getEntityType(),
                entry.getEntityId(),
                entry.getProjectId(),
                entry.getTimestamp(),
                entry.getDetails());
    }
}
