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
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@RequestBody @Valid TaskRequest request,
        @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
            taskService.createTask(request, principal.getUser())
        );
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(@PathVariable Long id,
        @RequestBody TaskUpdateRequest request,
        @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(taskService.updateTask(id, request, principal.getUser()));
    }

    @PostMapping("/{id}/transitions")
    public ResponseEntity<TaskResponse> transitionTask(@PathVariable Long id,
        @RequestBody @Valid TransitionRequest request,
        @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(taskService.transitionTask(id, request, principal.getUser()));
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> listTasks(
        @RequestParam Long projectId,
        @RequestParam(required = false) TaskStatus status,
        @RequestParam(required = false) Long assigneeId,
        @RequestParam(required = false) TaskPriority priority,
        @RequestParam(required = false) Long sprintId) {
        return ResponseEntity.ok(taskService.listTasks(projectId, status, assigneeId, priority, sprintId));
    }
}
