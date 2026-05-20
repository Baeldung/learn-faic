package com.baeldung.jiralite.dto;

import com.baeldung.jiralite.domain.Role;

public record UserResponse(Long id, String username, Role role) {
}
