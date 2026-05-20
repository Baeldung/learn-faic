package com.baeldung.jiralite.auth;

import com.baeldung.jiralite.user.Role;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
        @NotBlank String username,
        @NotBlank String password,
        Role role) {
}
