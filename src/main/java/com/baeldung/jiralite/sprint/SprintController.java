package com.baeldung.jiralite.sprint;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class SprintController {

    private final SprintService sprintService;

    public SprintController(SprintService sprintService) {
        this.sprintService = sprintService;
    }

    @PostMapping("/sprints")
    @ResponseStatus(HttpStatus.CREATED)
    public SprintResponse createSprint(@Valid @RequestBody CreateSprintRequest request) {
        return sprintService.createSprint(request);
    }

    @GetMapping("/projects/{projectId}/sprints")
    public List<SprintResponse> listSprints(@PathVariable Long projectId) {
        return sprintService.listSprints(projectId);
    }

    @PostMapping("/sprints/{id}/start")
    public SprintResponse startSprint(@PathVariable Long id) {
        return sprintService.startSprint(id);
    }

    @PostMapping("/sprints/{id}/complete")
    public SprintResponse completeSprint(@PathVariable Long id) {
        return sprintService.completeSprint(id);
    }
}
