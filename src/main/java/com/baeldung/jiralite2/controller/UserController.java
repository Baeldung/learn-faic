package com.baeldung.jiralite2.controller;

import com.baeldung.jiralite2.dto.request.ChangeRoleRequest;
import com.baeldung.jiralite2.dto.response.UserResponse;
import com.baeldung.jiralite2.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserResponse> list() {
        return userService.listAll();
    }

    @PutMapping("/{id}/role")
    public UserResponse changeRole(@PathVariable Long id,
                                   @Valid @RequestBody ChangeRoleRequest req,
                                   @AuthenticationPrincipal UserDetails principal) {
        return userService.changeRole(id, req, principal.getUsername());
    }
}
