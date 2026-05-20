package com.baeldung.jiralite2.controller;

import com.baeldung.jiralite2.dto.request.CreateSprintRequest;
import com.baeldung.jiralite2.dto.response.SprintResponse;
import com.baeldung.jiralite2.service.SprintService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
    @ResponseStatus(HttpStatus.CREATED)
    public SprintResponse create(@PathVariable Long projectId,
                                  @Valid @RequestBody CreateSprintRequest req,
                                  @AuthenticationPrincipal UserDetails principal) {
        return sprintService.create(projectId, req, principal.getUsername());
    }

    @GetMapping
    public List<SprintResponse> list(@PathVariable Long projectId) {
        return sprintService.listByProject(projectId);
    }

    @PostMapping("/{sprintId}/start")
    public SprintResponse start(@PathVariable Long projectId,
                                 @PathVariable Long sprintId,
                                 @AuthenticationPrincipal UserDetails principal) {
        return sprintService.start(sprintId, principal.getUsername());
    }

    @PostMapping("/{sprintId}/complete")
    public SprintResponse complete(@PathVariable Long projectId,
                                    @PathVariable Long sprintId,
                                    @AuthenticationPrincipal UserDetails principal) {
        return sprintService.complete(sprintId, principal.getUsername());
    }
}
