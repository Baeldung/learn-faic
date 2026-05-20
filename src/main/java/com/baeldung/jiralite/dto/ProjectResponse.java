package com.baeldung.jiralite.dto;

import com.baeldung.jiralite.domain.Project;

import java.util.List;

public record ProjectResponse(
    Long id,
    String name,
    String description,
    List<UserResponse> members
) {
    public static ProjectResponse from(Project project) {
        List<UserResponse> members = project.getMembers().stream()
            .map(UserResponse::from)
            .toList();
        return new ProjectResponse(project.getId(), project.getName(), project.getDescription(), members);
    }
}
