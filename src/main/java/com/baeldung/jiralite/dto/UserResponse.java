package com.baeldung.jiralite.dto;

import com.baeldung.jiralite.domain.User;
import com.baeldung.jiralite.domain.enums.Role;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private Role role;
    private LocalDateTime createdAt;

    public static UserResponse from(User user) {
        UserResponse r = new UserResponse();
        r.setId(user.getId());
        r.setUsername(user.getUsername());
        r.setEmail(user.getEmail());
        r.setRole(user.getRole());
        r.setCreatedAt(user.getCreatedAt());
        return r;
    }
}
