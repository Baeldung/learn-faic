package com.baeldung.jiralite.service;

import com.baeldung.jiralite.domain.Task;
import com.baeldung.jiralite.dto.TaskResponse;

final class TaskMapper {

    private TaskMapper() {
    }

    static TaskResponse toResponse(Task task) {
        TaskResponse resp = new TaskResponse();
        resp.setId(task.getId());
        resp.setProjectId(task.getProject().getId());
        resp.setTitle(task.getTitle());
        resp.setDescription(task.getDescription());
        resp.setStatus(task.getStatus());
        resp.setPriority(task.getPriority());
        resp.setAssigneeId(task.getAssignee() != null ? task.getAssignee().getId() : null);
        resp.setReporterId(task.getReporter().getId());
        resp.setSprintId(task.getSprint() != null ? task.getSprint().getId() : null);
        resp.setDueDate(task.getDueDate());
        return resp;
    }
}
