package com.baeldung.jiralite.security;

import com.baeldung.jiralite.user.Role;

public record JwtPrincipal(Long id, String username, Role role) { }
