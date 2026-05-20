package com.baeldung.jiralite.user;

import jakarta.validation.constraints.NotNull;

public record UpdateRoleRequest(@NotNull Role role) { }
