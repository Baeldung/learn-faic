package com.baeldung.jiralite.dto;

import com.baeldung.jiralite.domain.Role;
import jakarta.validation.constraints.NotNull;

public class ChangeRoleRequest {

    @NotNull
    private Role role;

    public ChangeRoleRequest() {
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
