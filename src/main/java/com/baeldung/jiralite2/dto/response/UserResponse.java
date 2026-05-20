package com.baeldung.jiralite2.dto.response;

import com.baeldung.jiralite2.domain.User;

public record UserResponse(Long id, String username, String role) {
    public static UserResponse from(User u) {
        return new UserResponse(u.getId(), u.getUsername(), u.getRole().name());
    }
}
