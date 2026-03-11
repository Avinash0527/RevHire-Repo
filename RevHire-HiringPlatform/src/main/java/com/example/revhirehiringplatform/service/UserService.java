package com.example.revhirehiringplatform.service;

import com.example.revhirehiringplatform.dto.response.UserResponse;
import com.example.revhirehiringplatform.model.User;
import com.example.revhirehiringplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private static final String USER_NOT_FOUND = "User not found";

    private final UserRepository userRepository;

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public UserResponse getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));
    }

    @Transactional
    public UserResponse updateUserStatus(Long id, boolean enabled) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));
        user.setStatus(enabled);
        return mapToDto(userRepository.save(user));
    }

    @Transactional
    public UserResponse updateUserRole(Long id, User.Role role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));
        user.setRole(role);
        return mapToDto(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));
        userRepository.delete(user);
    }

    public long getUserCount() {
        return userRepository.count();
    }

    private UserResponse mapToDto(User user) {
        UserResponse dto = new UserResponse();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole());
        dto.setStatus(user.getStatus());
        return dto;
    }
}