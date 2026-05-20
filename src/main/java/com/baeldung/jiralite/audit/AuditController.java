package com.baeldung.jiralite.audit;

import com.baeldung.jiralite.project.ProjectService;
import com.baeldung.jiralite.task.Task;
import com.baeldung.jiralite.task.TaskService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuditController {

    private final AuditRepository auditRepository;
    private final AuditMapper auditMapper;
    private final ProjectService projectService;
    private final TaskService taskService;

    public AuditController(AuditRepository auditRepository, AuditMapper auditMapper,
                           ProjectService projectService, TaskService taskService) {
        this.auditRepository = auditRepository;
        this.auditMapper = auditMapper;
        this.projectService = projectService;
        this.taskService = taskService;
    }

    @GetMapping("/projects/{id}/audit")
    public List<AuditResponse> projectAudit(@PathVariable Long id) {
        projectService.requireVisible(id);
        return auditRepository.findByProject(id).stream().map(auditMapper::toResponse).toList();
    }

    @GetMapping("/tasks/{id}/audit")
    public List<AuditResponse> taskAudit(@PathVariable Long id) {
        Task task = taskService.loadVisibleTask(id);
        return auditRepository.findByTask(task.getId()).stream().map(auditMapper::toResponse).toList();
    }
}
