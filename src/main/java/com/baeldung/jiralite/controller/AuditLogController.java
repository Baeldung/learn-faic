package com.baeldung.jiralite.controller;

import com.baeldung.jiralite.dto.AuditLogResponse;
import com.baeldung.jiralite.service.AuditLogService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping("/api/projects/{projectId}/audit")
    public ResponseEntity<List<AuditLogResponse>> auditByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(auditLogService.listByProject(projectId));
    }

    @GetMapping("/api/tasks/{taskId}/audit")
    public ResponseEntity<List<AuditLogResponse>> auditByTask(@PathVariable Long taskId) {
        return ResponseEntity.ok(auditLogService.listByTask(taskId));
    }
}
