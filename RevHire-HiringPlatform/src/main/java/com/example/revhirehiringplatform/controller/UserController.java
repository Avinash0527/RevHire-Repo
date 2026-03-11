package com.example.revhirehiringplatform.controller;

import com.example.revhirehiringplatform.dto.response.UserResponse;
import com.example.revhirehiringplatform.model.User;
import com.example.revhirehiringplatform.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<UserResponse> updateUserStatus(@PathVariable Long id, @RequestParam boolean enabled) {
        return ResponseEntity.ok(userService.updateUserStatus(id, enabled));
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<UserResponse> updateUserRole(@PathVariable Long id, @RequestParam User.Role role) {
        return ResponseEntity.ok(userService.updateUserRole(id, role));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getUserCount() {
        return ResponseEntity.ok(userService.getUserCount());
    }

    @GetMapping("/roles")
    public ResponseEntity<List<User.Role>> getRoles() {
        return ResponseEntity.ok(Arrays.asList(User.Role.values()));
    }
}