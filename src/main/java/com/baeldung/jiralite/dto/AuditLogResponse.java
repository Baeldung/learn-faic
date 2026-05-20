package com.baeldung.jiralite.dto;

import com.baeldung.jiralite.domain.AuditLog;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuditLogResponse {
    private Long id;
    private Long projectId;
    private Long taskId;
    private String action;
    private Long actorId;
    private String details;
    private LocalDateTime createdAt;

    public static AuditLogResponse from(AuditLog log) {
        AuditLogResponse r = new AuditLogResponse();
        r.setId(log.getId());
        r.setProjectId(log.getProjectId());
        r.setTaskId(log.getTaskId());
        r.setAction(log.getAction());
        r.setActorId(log.getActorId());
        r.setDetails(log.getDetails());
        r.setCreatedAt(log.getCreatedAt());
        return r;
    }
}
