package com.baeldung.jiralite.dto;

import com.baeldung.jiralite.domain.Role;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
    @NotBlank String username,
    @NotBlank String password,
    Role role
) {}
