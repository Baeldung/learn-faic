package com.baeldung.jiralite.service;

import com.baeldung.jiralite.domain.AuditEventType;
import com.baeldung.jiralite.domain.AuditLog;
import com.baeldung.jiralite.domain.Project;
import com.baeldung.jiralite.domain.User;
import com.baeldung.jiralite.dto.AuditLogResponse;
import com.baeldung.jiralite.exception.ResourceNotFoundException;
import com.baeldung.jiralite.repository.AuditLogRepository;
import com.baeldung.jiralite.repository.ProjectRepository;
import com.baeldung.jiralite.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    public AuditLogService(AuditLogRepository auditLogRepository,
                           ProjectRepository projectRepository,
                           TaskRepository taskRepository) {
        this.auditLogRepository = auditLogRepository;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
    }

    @Transactional
    public void log(AuditEventType eventType, User actor, String entityType, Long entityId, Project project) {
        auditLogRepository.save(new AuditLog(eventType, actor, entityType, entityId, project));
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getByProject(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project not found: " + projectId);
        }
        return auditLogRepository.findByProjectIdOrderByCreatedAtDesc(projectId)
            .stream()
            .map(AuditLogResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getByTask(Long taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new ResourceNotFoundException("Task not found: " + taskId);
        }
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc("TASK", taskId)
            .stream()
            .map(AuditLogResponse::from)
            .toList();
    }
}
