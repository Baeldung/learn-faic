package com.baeldung.jiralite.audit;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private final AuditRepository auditRepository;
    private final AuditMapper auditMapper;

    public AuditService(AuditRepository auditRepository, AuditMapper auditMapper) {
        this.auditRepository = auditRepository;
        this.auditMapper = auditMapper;
    }

    @Transactional(readOnly = true)
    public List<AuditResponse> findByProject(Long projectId) {
        return auditRepository.findByProject(projectId).stream().map(auditMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AuditResponse> findByTask(Long taskId) {
        return auditRepository.findByTask(taskId).stream().map(auditMapper::toResponse).toList();
    }
}
