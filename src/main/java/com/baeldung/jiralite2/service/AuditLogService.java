package com.baeldung.jiralite2.service;

import com.baeldung.jiralite2.domain.*;
import com.baeldung.jiralite2.domain.enums.AuditEventType;
import com.baeldung.jiralite2.dto.response.AuditLogResponse;
import com.baeldung.jiralite2.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void record(AuditEventType type, User actor, Project project, Task task, String detail) {
        AuditLog log = new AuditLog();
        log.setEventType(type);
        log.setActor(actor);
        log.setProject(project);
        log.setTask(task);
        log.setDetail(detail);
        auditLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getByProject(Long projectId) {
        return auditLogRepository.findAllByProjectIdOrderByOccurredAtDesc(projectId)
            .stream().map(AuditLogResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getByTask(Long taskId) {
        return auditLogRepository.findAllByTaskIdOrderByOccurredAtDesc(taskId)
            .stream().map(AuditLogResponse::from).toList();
    }
}
