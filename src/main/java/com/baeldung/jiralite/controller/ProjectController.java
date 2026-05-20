package com.baeldung.jiralite.controller;

import com.baeldung.jiralite.dto.AddMemberRequest;
import com.baeldung.jiralite.dto.ProjectRequest;
import com.baeldung.jiralite.dto.ProjectResponse;
import com.baeldung.jiralite.security.UserPrincipal;
import com.baeldung.jiralite.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody ProjectRequest req,
                                                         @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(projectService.createProject(req, principal.getUser()));
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> listProjects() {
        return ResponseEntity.ok(projectService.listProjects());
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<ProjectResponse> addMember(@PathVariable Long id,
                                                     @Valid @RequestBody AddMemberRequest req,
                                                     @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(projectService.addMember(id, req, principal.getUser()));
    }

    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<ProjectResponse> removeMember(@PathVariable Long id,
                                                        @PathVariable Long userId,
                                                        @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(projectService.removeMember(id, userId, principal.getUser()));
    }
}
