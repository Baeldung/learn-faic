package com.baeldung.jiralite.audit;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baeldung.jiralite.project.ProjectService;
import com.baeldung.jiralite.security.CurrentUser;
import com.baeldung.jiralite.task.TaskResponse;
import com.baeldung.jiralite.task.TaskService;
import com.baeldung.jiralite.user.User;

@RestController
@RequestMapping("/api")
public class AuditController {

    private final AuditService auditService;

    private final ProjectService projectService;

    private final TaskService taskService;

    private final CurrentUser currentUser;

    public AuditController(AuditService auditService, ProjectService projectService,
            TaskService taskService, CurrentUser currentUser) {
        this.auditService = auditService;
        this.projectService = projectService;
        this.taskService = taskService;
        this.currentUser = currentUser;
    }

    @GetMapping("/projects/{projectId}/audit")
    public List<AuditEntryResponse> listForProject(@PathVariable Long projectId) {
        User actor = currentUser.get();
        boolean isMember = projectService.isMember(projectId, actor.getId());
        return auditService.listForProject(projectId, actor, isMember);
    }

    @GetMapping("/tasks/{taskId}/audit")
    public List<AuditEntryResponse> listForTask(@PathVariable Long taskId) {
        User actor = currentUser.get();
        TaskResponse task = taskService.getTask(taskId);
        boolean isMember = projectService.isMember(task.projectId(), actor.getId());
        return auditService.listForTask(taskId, actor, isMember);
    }
}
