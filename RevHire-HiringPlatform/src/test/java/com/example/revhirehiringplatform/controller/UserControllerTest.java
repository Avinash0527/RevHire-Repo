package com.example.revhirehiringplatform.controller;

import com.example.revhirehiringplatform.dto.response.UserResponse;
import com.example.revhirehiringplatform.model.User;
import com.example.revhirehiringplatform.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllUsers() {
        when(userService.getAllUsers()).thenReturn(Collections.singletonList(new UserResponse()));
        ResponseEntity<List<UserResponse>> response = userController.getAllUsers();
        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetUserById() {
        when(userService.getUserById(1L)).thenReturn(new UserResponse());
        ResponseEntity<UserResponse> response = userController.getUserById(1L);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void testUpdateUserStatus() {
        when(userService.updateUserStatus(1L, true)).thenReturn(new UserResponse());
        ResponseEntity<UserResponse> response = userController.updateUserStatus(1L, true);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void testUpdateUserRole() {
        when(userService.updateUserRole(1L, User.Role.ADMIN)).thenReturn(new UserResponse());
        ResponseEntity<UserResponse> response = userController.updateUserRole(1L, User.Role.ADMIN);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void testDeleteUser() {
        ResponseEntity<Void> response = userController.deleteUser(1L);
        assertEquals(204, response.getStatusCode().value());
        verify(userService).deleteUser(1L);
    }

    @Test
    void testGetUserCount() {
        when(userService.getUserCount()).thenReturn(5L);
        ResponseEntity<Long> response = userController.getUserCount();
        assertEquals(200, response.getStatusCode().value());
        assertEquals(5L, response.getBody());
    }

    @Test
    void testGetRoles() {
        ResponseEntity<List<User.Role>> response = userController.getRoles();
        assertEquals(200, response.getStatusCode().value());
        assertEquals(User.Role.values().length, response.getBody().size());
    }
}
