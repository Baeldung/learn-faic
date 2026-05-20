package com.baeldung.jiralite.controller;

import com.baeldung.jiralite.dto.SprintRequest;
import com.baeldung.jiralite.dto.SprintResponse;
import com.baeldung.jiralite.security.UserPrincipal;
import com.baeldung.jiralite.service.SprintService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class SprintController {

    @Autowired
    private SprintService sprintService;

    @PostMapping("/projects/{projectId}/sprints")
    public ResponseEntity<SprintResponse> createSprint(@PathVariable Long projectId,
        @RequestBody @Valid SprintRequest request,
        @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
            sprintService.createSprint(projectId, request, principal.getUser())
        );
    }

    @PostMapping("/sprints/{id}/start")
    public ResponseEntity<SprintResponse> startSprint(@PathVariable Long id,
        @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(sprintService.startSprint(id, principal.getUser()));
    }

    @PostMapping("/sprints/{id}/complete")
    public ResponseEntity<SprintResponse> completeSprint(@PathVariable Long id,
        @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(sprintService.completeSprint(id, principal.getUser()));
    }
}
