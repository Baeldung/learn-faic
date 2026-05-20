package com.baeldung.jiralite.controller;

import com.baeldung.jiralite.dto.AuditLogResponse;
import com.baeldung.jiralite.service.AuditLogService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class AuditLogController {

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping("/projects/{id}/audit")
    public ResponseEntity<List<AuditLogResponse>> getProjectAudit(@PathVariable Long id) {
        return ResponseEntity.ok(auditLogService.getByProject(id));
    }

    @GetMapping("/tasks/{id}/audit")
    public ResponseEntity<List<AuditLogResponse>> getTaskAudit(@PathVariable Long id) {
        return ResponseEntity.ok(auditLogService.getByTask(id));
    }
}
