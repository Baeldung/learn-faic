package com.baeldung.jiralite.controller;

import com.baeldung.jiralite.dto.SprintRequest;
import com.baeldung.jiralite.dto.SprintResponse;
import com.baeldung.jiralite.service.SprintService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/sprints")
public class SprintController {

    private final SprintService sprintService;

    public SprintController(SprintService sprintService) {
        this.sprintService = sprintService;
    }

    @PostMapping
    public ResponseEntity<SprintResponse> createSprint(@PathVariable Long projectId,
                                                        @Valid @RequestBody SprintRequest request) {
        return ResponseEntity.ok(sprintService.createSprint(projectId, request));
    }

    @GetMapping
    public ResponseEntity<List<SprintResponse>> listSprints(@PathVariable Long projectId) {
        return ResponseEntity.ok(sprintService.listSprints(projectId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SprintResponse> getSprint(@PathVariable Long projectId,
                                                     @PathVariable Long id) {
        return ResponseEntity.ok(sprintService.getSprint(projectId, id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SprintResponse> updateSprint(@PathVariable Long projectId,
                                                        @PathVariable Long id,
                                                        @Valid @RequestBody SprintRequest request) {
        return ResponseEntity.ok(sprintService.updateSprint(projectId, id, request));
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<SprintResponse> startSprint(@PathVariable Long projectId,
                                                       @PathVariable Long id) {
        return ResponseEntity.ok(sprintService.startSprint(projectId, id));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<SprintResponse> completeSprint(@PathVariable Long projectId,
                                                          @PathVariable Long id) {
        return ResponseEntity.ok(sprintService.completeSprint(projectId, id));
    }
}
