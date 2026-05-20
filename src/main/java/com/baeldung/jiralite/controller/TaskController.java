package com.baeldung.jiralite.controller;

import com.baeldung.jiralite.domain.TaskPriority;
import com.baeldung.jiralite.domain.TaskStatus;
import com.baeldung.jiralite.dto.TaskRequest;
import com.baeldung.jiralite.dto.TaskResponse;
import com.baeldung.jiralite.dto.TaskUpdateRequest;
import com.baeldung.jiralite.dto.TransitionRequest;
import com.baeldung.jiralite.security.UserPrincipal;
import com.baeldung.jiralite.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskRequest req,
                                                   @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(taskService.createTask(req, principal.getUser()));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(@PathVariable Long id,
                                                   @RequestBody TaskUpdateRequest req,
                                                   @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(taskService.updateTask(id, req, principal.getUser()));
    }

    @PostMapping("/{id}/transitions")
    public ResponseEntity<TaskResponse> transitionTask(@PathVariable Long id,
                                                       @Valid @RequestBody TransitionRequest req,
                                                       @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(taskService.transitionTask(id, req, principal.getUser()));
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> listTasks(
        @RequestParam(required = false) Long projectId,
        @RequestParam(required = false) TaskStatus status,
        @RequestParam(required = false) Long assigneeId,
        @RequestParam(required = false) TaskPriority priority,
        @RequestParam(required = false) Long sprintId) {
        return ResponseEntity.ok(taskService.listTasks(projectId, status, assigneeId, priority, sprintId));
    }
}
