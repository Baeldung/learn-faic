package com.baeldung.jiralite.controller;

import com.baeldung.jiralite.dto.AuditLogResponse;
import com.baeldung.jiralite.service.AuditLogService;
import com.baeldung.jiralite.service.ProjectService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects/{projectId}")
public class AuditLogController {

    private final AuditLogService auditLogService;
    private final ProjectService projectService;

    public AuditLogController(AuditLogService auditLogService, ProjectService projectService) {
        this.auditLogService = auditLogService;
        this.projectService = projectService;
    }

    @GetMapping("/audit")
    public ResponseEntity<Page<AuditLogResponse>> getProjectAudit(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        projectService.findProjectWithAccess(projectId);
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(auditLogService.getByProject(projectId, pageable));
    }

    @GetMapping("/tasks/{taskId}/audit")
    public ResponseEntity<Page<AuditLogResponse>> getTaskAudit(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        projectService.findProjectWithAccess(projectId);
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(auditLogService.getByTask(taskId, pageable));
    }
}
