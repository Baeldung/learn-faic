package com.baeldung.jiralite.dto;

import com.baeldung.jiralite.domain.Task;
import com.baeldung.jiralite.domain.enums.TaskPriority;
import com.baeldung.jiralite.domain.enums.TaskStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class TaskResponse {
    private Long id;
    private Long projectId;
    private Long sprintId;
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private UserResponse assignee;
    private UserResponse reporter;
    private LocalDate dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TaskResponse from(Task task) {
        TaskResponse r = new TaskResponse();
        r.setId(task.getId());
        r.setProjectId(task.getProject().getId());
        r.setSprintId(task.getSprint() != null ? task.getSprint().getId() : null);
        r.setTitle(task.getTitle());
        r.setDescription(task.getDescription());
        r.setStatus(task.getStatus());
        r.setPriority(task.getPriority());
        r.setAssignee(task.getAssignee() != null ? UserResponse.from(task.getAssignee()) : null);
        r.setReporter(UserResponse.from(task.getReporter()));
        r.setDueDate(task.getDueDate());
        r.setCreatedAt(task.getCreatedAt());
        r.setUpdatedAt(task.getUpdatedAt());
        return r;
    }
}
