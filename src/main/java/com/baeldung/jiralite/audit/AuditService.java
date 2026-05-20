package com.baeldung.jiralite.audit;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baeldung.jiralite.user.Role;
import com.baeldung.jiralite.user.User;
import com.baeldung.jiralite.web.ForbiddenException;

@Service
@Transactional
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    private final AuditLogMapper auditLogMapper;

    public AuditService(AuditLogRepository auditLogRepository, AuditLogMapper auditLogMapper) {
        this.auditLogRepository = auditLogRepository;
        this.auditLogMapper = auditLogMapper;
    }

    public void record(AuditEventType eventType, User actor, Long projectId, Long taskId) {
        AuditLog log = new AuditLog(eventType, actor, projectId, taskId, Instant.now());
        auditLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public List<AuditEntryResponse> listForProject(Long projectId, User actor, boolean isMember) {
        if (!isMember && actor.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Not a member of this project");
        }
        return auditLogRepository.findByProjectIdOrderByTimestampDesc(projectId).stream()
                .map(auditLogMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AuditEntryResponse> listForTask(Long taskId, User actor, boolean isMember) {
        if (!isMember && actor.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Not a member of this project");
        }
        return auditLogRepository.findByTaskIdOrderByTimestampDesc(taskId).stream()
                .map(auditLogMapper::toResponse)
                .toList();
    }
}
