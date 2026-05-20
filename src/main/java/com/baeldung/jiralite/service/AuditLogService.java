package com.baeldung.jiralite.service;

import com.baeldung.jiralite.domain.AuditEventType;
import com.baeldung.jiralite.domain.AuditLog;
import com.baeldung.jiralite.domain.Project;
import com.baeldung.jiralite.domain.User;
import com.baeldung.jiralite.dto.AuditLogResponse;
import com.baeldung.jiralite.repository.AuditLogRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogService {

    private static final String ENTITY_TASK = "TASK";

    @Autowired
    private AuditLogRepository auditLogRepo;

    @Transactional
    public void log(AuditEventType eventType, User actor, String entityType, Long entityId, Project project) {
        AuditLog entry = new AuditLog();
        entry.setEventType(eventType);
        entry.setActor(actor);
        entry.setEntityType(entityType);
        entry.setEntityId(entityId);
        entry.setProject(project);
        auditLogRepo.save(entry);
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getByProject(Long projectId) {
        return auditLogRepo.findByProjectIdOrderByCreatedAtDesc(projectId)
            .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getByTask(Long taskId) {
        return auditLogRepo.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(ENTITY_TASK, taskId)
            .stream().map(this::toResponse).toList();
    }

    private AuditLogResponse toResponse(AuditLog auditLog) {
        AuditLogResponse r = new AuditLogResponse();
        r.setId(auditLog.getId());
        r.setEventType(auditLog.getEventType());
        r.setActorUsername(auditLog.getActor() != null ? auditLog.getActor().getUsername() : null);
        r.setEntityType(auditLog.getEntityType());
        r.setEntityId(auditLog.getEntityId());
        r.setCreatedAt(auditLog.getCreatedAt());
        return r;
    }
}
