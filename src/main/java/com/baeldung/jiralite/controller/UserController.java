package com.baeldung.jiralite.controller;

import com.baeldung.jiralite.dto.ChangeRoleRequest;
import com.baeldung.jiralite.dto.UserResponse;
import com.baeldung.jiralite.security.UserPrincipal;
import com.baeldung.jiralite.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> listUsers() {
        return ResponseEntity.ok(userService.listUsers());
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<UserResponse> changeRole(@PathVariable Long id,
                                                   @Valid @RequestBody ChangeRoleRequest req,
                                                   @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(userService.changeRole(id, req, principal.getUser()));
    }
}
