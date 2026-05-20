package com.baeldung.jiralite.security;

import com.baeldung.jiralite.user.Role;
import com.baeldung.jiralite.web.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {

    public JwtPrincipal require() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof JwtPrincipal principal)) {
            throw new UnauthorizedException("Authentication required");
        }
        return principal;
    }

    public Long id() {
        return require().id();
    }

    public Role role() {
        return require().role();
    }

    public boolean isAdmin() {
        return role() == Role.ADMIN;
    }
}
