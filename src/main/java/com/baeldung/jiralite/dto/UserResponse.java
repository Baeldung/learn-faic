package com.baeldung.jiralite.dto;

import com.baeldung.jiralite.domain.Role;
import com.baeldung.jiralite.domain.User;

public record UserResponse(Long id, String username, Role role) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getRole());
    }
}
