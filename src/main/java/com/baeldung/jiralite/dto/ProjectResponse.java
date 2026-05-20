package com.baeldung.jiralite.dto;

import com.baeldung.jiralite.domain.Project;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProjectResponse {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private UserResponse createdBy;

    public static ProjectResponse from(Project project) {
        ProjectResponse r = new ProjectResponse();
        r.setId(project.getId());
        r.setName(project.getName());
        r.setDescription(project.getDescription());
        r.setCreatedAt(project.getCreatedAt());
        r.setCreatedBy(UserResponse.from(project.getCreatedBy()));
        return r;
    }
}
