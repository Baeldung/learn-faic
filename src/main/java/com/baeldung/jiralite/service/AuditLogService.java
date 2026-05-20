package com.baeldung.jiralite.service;

import com.baeldung.jiralite.domain.AuditLog;
import com.baeldung.jiralite.dto.AuditLogResponse;
import com.baeldung.jiralite.repository.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String action, Long actorId, Long projectId, Long taskId, String details) {
        AuditLog entry = AuditLog.builder()
                .action(action)
                .actorId(actorId)
                .projectId(projectId)
                .taskId(taskId)
                .details(details)
                .build();
        auditLogRepository.save(entry);
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getByProject(Long projectId, Pageable pageable) {
        return auditLogRepository.findByProjectIdOrderByCreatedAtDesc(projectId, pageable)
                .map(AuditLogResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getByTask(Long taskId, Pageable pageable) {
        return auditLogRepository.findByTaskIdOrderByCreatedAtDesc(taskId, pageable)
                .map(AuditLogResponse::from);
    }
}
