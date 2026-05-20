package com.baeldung.jiralite.service;

import com.baeldung.jiralite.domain.Role;
import com.baeldung.jiralite.domain.User;
import com.baeldung.jiralite.exception.ForbiddenException;

public final class RoleChecker {

    private static final String ACCESS_DENIED = "Access denied: insufficient role";

    private RoleChecker() {
    }

    public static void requireAdmin(User user) {
        if (user.getRole() != Role.ADMIN) {
            throw new ForbiddenException(ACCESS_DENIED);
        }
    }

    public static void requireAdminOrManager(User user) {
        if (user.getRole() != Role.ADMIN && user.getRole() != Role.MANAGER) {
            throw new ForbiddenException(ACCESS_DENIED);
        }
    }

    public static boolean isAdminOrManager(User user) {
        return user.getRole() == Role.ADMIN || user.getRole() == Role.MANAGER;
    }
}
