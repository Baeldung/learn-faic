package com.baeldung.jiralite.controller;

import com.baeldung.jiralite.domain.enums.TaskPriority;
import com.baeldung.jiralite.domain.enums.TaskStatus;
import com.baeldung.jiralite.dto.TaskRequest;
import com.baeldung.jiralite.dto.TaskResponse;
import com.baeldung.jiralite.dto.TaskUpdateRequest;
import com.baeldung.jiralite.dto.TransitionRequest;
import com.baeldung.jiralite.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@PathVariable Long projectId,
                                                    @Valid @RequestBody TaskRequest request) {
        return ResponseEntity.ok(taskService.createTask(projectId, request));
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> listTasks(
            @PathVariable Long projectId,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) Long sprintId) {
        return ResponseEntity.ok(taskService.listTasks(projectId, status, assigneeId, priority, sprintId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTask(@PathVariable Long projectId,
                                                 @PathVariable Long id) {
        return ResponseEntity.ok(taskService.getTask(projectId, id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(@PathVariable Long projectId,
                                                    @PathVariable Long id,
                                                    @Valid @RequestBody TaskUpdateRequest request) {
        return ResponseEntity.ok(taskService.updateTask(projectId, id, request));
    }

    @PostMapping("/{id}/transition")
    public ResponseEntity<TaskResponse> transitionTask(@PathVariable Long projectId,
                                                        @PathVariable Long id,
                                                        @Valid @RequestBody TransitionRequest request) {
        return ResponseEntity.ok(taskService.transitionTask(projectId, id, request));
    }
}
