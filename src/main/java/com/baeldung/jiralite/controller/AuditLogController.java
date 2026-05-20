package com.baeldung.jiralite.controller;

import com.baeldung.jiralite.dto.AuditLogResponse;
import com.baeldung.jiralite.service.AuditLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping("/projects/{id}/audit")
    public ResponseEntity<List<AuditLogResponse>> projectAudit(@PathVariable Long id) {
        return ResponseEntity.ok(auditLogService.getByProject(id));
    }

    @GetMapping("/tasks/{id}/audit")
    public ResponseEntity<List<AuditLogResponse>> taskAudit(@PathVariable Long id) {
        return ResponseEntity.ok(auditLogService.getByTask(id));
    }
}
