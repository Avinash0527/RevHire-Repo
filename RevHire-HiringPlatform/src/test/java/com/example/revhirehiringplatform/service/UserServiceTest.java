package com.example.revhirehiringplatform.service;

import com.example.revhirehiringplatform.dto.response.UserResponse;
import com.example.revhirehiringplatform.model.User;
import com.example.revhirehiringplatform.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("John Doe");
        user.setEmail("john@test.com");
        user.setRole(User.Role.JOB_SEEKER);
    }

    @Test
    void testGetUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse response = userService.getUserById(1L);

        assertNotNull(response);
        assertEquals("John Doe", response.getName());
    }

    @Test
    void testUpdateUserRole_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse response = userService.updateUserRole(1L, User.Role.ADMIN);

        assertNotNull(response);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testDeleteUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteUser(1L);

        verify(userRepository, times(1)).delete(any(User.class));
    }

    @Test
    void testGetAllUsers() {
        when(userRepository.findAll()).thenReturn(java.util.Collections.singletonList(user));
        java.util.List<UserResponse> responses = userService.getAllUsers();
        assertEquals(1, responses.size());
    }

    @Test
    void testGetUserById_NotFound() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> userService.getUserById(2L));
    }

    @Test
    void testUpdateUserStatus_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse response = userService.updateUserStatus(1L, true);
        assertNotNull(response);
    }

    @Test
    void testUpdateUserStatus_NotFound() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> userService.updateUserStatus(2L, true));
    }

    @Test
    void testUpdateUserRole_NotFound() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> userService.updateUserRole(2L, User.Role.ADMIN));
    }

    @Test
    void testGetUserCount() {
        when(userRepository.count()).thenReturn(10L);
        long count = userService.getUserCount();
        assertEquals(10L, count);
    }
}
