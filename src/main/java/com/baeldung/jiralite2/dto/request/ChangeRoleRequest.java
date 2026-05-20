package com.baeldung.jiralite2.dto.request;

import com.baeldung.jiralite2.domain.enums.Role;
import jakarta.validation.constraints.NotNull;

public record ChangeRoleRequest(@NotNull Role role) {}
