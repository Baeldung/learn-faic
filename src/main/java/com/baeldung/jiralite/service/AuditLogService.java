package com.baeldung.jiralite.service;

import com.baeldung.jiralite.domain.AuditEventType;
import com.baeldung.jiralite.domain.AuditLog;
import com.baeldung.jiralite.domain.User;
import com.baeldung.jiralite.dto.AuditLogResponse;
import com.baeldung.jiralite.repository.AuditLogRepository;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void record(AuditEventType eventType, User actor, Long projectId, Long taskId, String details) {
        AuditLog log = new AuditLog();
        log.setEventType(eventType);
        log.setActor(actor);
        log.setProjectId(projectId);
        log.setTaskId(taskId);
        log.setCreatedAt(Instant.now());
        log.setDetails(details);
        auditLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> listByProject(Long projectId) {
        return auditLogRepository.findByProjectIdOrderByCreatedAtDesc(projectId)
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> listByTask(Long taskId) {
        return auditLogRepository.findByTaskIdOrderByCreatedAtDesc(taskId)
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    private AuditLogResponse toResponse(AuditLog log) {
        Long actorId = log.getActor() != null ? log.getActor().getId() : null;
        AuditLogResponse resp = new AuditLogResponse();
        resp.setId(log.getId());
        resp.setEventType(log.getEventType());
        resp.setActorId(actorId);
        resp.setProjectId(log.getProjectId());
        resp.setTaskId(log.getTaskId());
        resp.setCreatedAt(log.getCreatedAt());
        resp.setDetails(log.getDetails());
        return resp;
    }
}
