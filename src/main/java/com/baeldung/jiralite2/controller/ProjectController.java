package com.baeldung.jiralite2.controller;

import com.baeldung.jiralite2.dto.request.AddMemberRequest;
import com.baeldung.jiralite2.dto.request.CreateProjectRequest;
import com.baeldung.jiralite2.dto.response.ProjectResponse;
import com.baeldung.jiralite2.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse create(@Valid @RequestBody CreateProjectRequest req,
                                   @AuthenticationPrincipal UserDetails principal) {
        return projectService.create(req, principal.getUsername());
    }

    @GetMapping
    public List<ProjectResponse> list() {
        return projectService.listAll();
    }

    @PostMapping("/{id}/members")
    public ProjectResponse addMember(@PathVariable Long id,
                                     @Valid @RequestBody AddMemberRequest req,
                                     @AuthenticationPrincipal UserDetails principal) {
        return projectService.addMember(id, req, principal.getUsername());
    }

    @DeleteMapping("/{id}/members/{userId}")
    public ProjectResponse removeMember(@PathVariable Long id,
                                        @PathVariable Long userId,
                                        @AuthenticationPrincipal UserDetails principal) {
        return projectService.removeMember(id, userId, principal.getUsername());
    }
}
