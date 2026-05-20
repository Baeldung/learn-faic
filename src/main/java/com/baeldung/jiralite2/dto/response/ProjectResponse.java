package com.baeldung.jiralite2.dto.response;

import com.baeldung.jiralite2.domain.Project;
import java.util.List;

public record ProjectResponse(Long id, String name, String description, List<UserResponse> members) {
    public static ProjectResponse from(Project p) {
        List<UserResponse> members = p.getMembers().stream()
            .map(UserResponse::from)
            .toList();
        return new ProjectResponse(p.getId(), p.getName(), p.getDescription(), members);
    }
}
