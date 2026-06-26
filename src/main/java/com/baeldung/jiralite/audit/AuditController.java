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

    private final AuditService auditService;
    private final ProjectService projectService;
    private final TaskService taskService;

    public AuditController(AuditService auditService, ProjectService projectService, TaskService taskService) {
        this.auditService = auditService;
        this.projectService = projectService;
        this.taskService = taskService;
    }

    @GetMapping("/projects/{id}/audit")
    public List<AuditResponse> projectAudit(@PathVariable Long id) {
        projectService.requireVisible(id);
        return auditService.findByProject(id);
    }

    @GetMapping("/tasks/{id}/audit")
    public List<AuditResponse> taskAudit(@PathVariable Long id) {
        Task task = taskService.loadVisibleTask(id);
        return auditService.findByTask(task.getId());
    }
}
