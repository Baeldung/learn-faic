package com.baeldung.jiralite.dto;

import com.baeldung.jiralite.domain.enums.Role;
import jakarta.validation.constraints.NotNull;

public class ChangeRoleRequest {
    @NotNull
    private Role role;

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}
