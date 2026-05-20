package com.baeldung.jiralite.dto;

import com.baeldung.jiralite.domain.Role;
import jakarta.validation.constraints.NotNull;

public record ChangeRoleRequest(@NotNull Role role) {
}
