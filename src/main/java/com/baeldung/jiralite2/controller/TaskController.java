package com.baeldung.jiralite2.controller;

import com.baeldung.jiralite2.domain.enums.Priority;
import com.baeldung.jiralite2.domain.enums.TaskStatus;
import com.baeldung.jiralite2.dto.request.CreateTaskRequest;
import com.baeldung.jiralite2.dto.request.TransitionRequest;
import com.baeldung.jiralite2.dto.request.UpdateTaskRequest;
import com.baeldung.jiralite2.dto.response.TaskResponse;
import com.baeldung.jiralite2.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse create(@Valid @RequestBody CreateTaskRequest req,
                                @AuthenticationPrincipal UserDetails principal) {
        return taskService.create(req, principal.getUsername());
    }

    @PutMapping("/{id}")
    public TaskResponse update(@PathVariable Long id,
                                @RequestBody UpdateTaskRequest req,
                                @AuthenticationPrincipal UserDetails principal) {
        return taskService.update(id, req, principal.getUsername());
    }

    @PostMapping("/{id}/transition")
    public TaskResponse transition(@PathVariable Long id,
                                    @Valid @RequestBody TransitionRequest req,
                                    @AuthenticationPrincipal UserDetails principal) {
        return taskService.transition(id, req, principal.getUsername());
    }

    @GetMapping
    public List<TaskResponse> list(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) Long sprintId) {
        return taskService.list(projectId, status, assigneeId, priority, sprintId);
    }
}
