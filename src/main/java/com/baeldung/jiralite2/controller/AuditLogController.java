package com.baeldung.jiralite2.controller;

import com.baeldung.jiralite2.dto.response.AuditLogResponse;
import com.baeldung.jiralite2.service.AuditLogService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/audit")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping("/projects/{projectId}")
    public List<AuditLogResponse> byProject(@PathVariable Long projectId) {
        return auditLogService.getByProject(projectId);
    }

    @GetMapping("/tasks/{taskId}")
    public List<AuditLogResponse> byTask(@PathVariable Long taskId) {
        return auditLogService.getByTask(taskId);
    }
}
