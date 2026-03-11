package com.example.revhirehiringplatform.controller;



import com.example.revhirehiringplatform.model.User;
import com.example.revhirehiringplatform.repository.UserRepository;
import com.example.revhirehiringplatform.security.UserDetailsImpl;
import com.example.revhirehiringplatform.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
@AutoConfigureMockMvc
public class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private UserRepository userRepository;

    private User user;
    private UserDetailsImpl userDetails;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setRole(User.Role.JOB_SEEKER);
        userDetails = UserDetailsImpl.build(user);
    }

    @Test
    @WithMockUser
    void testGetMyNotifications() throws Exception {
        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(notificationService.getMyNotifications(any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/notifications")
                        .with(user(userDetails)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testGetUnreadNotifications() throws Exception {
        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(notificationService.getUnreadNotifications(any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/notifications/unread")
                        .with(user(userDetails)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testMarkAsRead() throws Exception {
        when(userRepository.findById(any())).thenReturn(Optional.of(user));

        mockMvc.perform(put("/api/notifications/1/read")
                        .with(csrf())
                        .with(user(userDetails)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testMarkAllAsRead() throws Exception {
        when(userRepository.findById(any())).thenReturn(Optional.of(user));

        mockMvc.perform(put("/api/notifications/mark-all-read")
                        .with(csrf())
                        .with(user(userDetails)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testDeleteNotification() throws Exception {
        when(userRepository.findById(any())).thenReturn(Optional.of(user));

        mockMvc.perform(delete("/api/notifications/1")
                        .with(csrf())
                        .with(user(userDetails)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testDeleteAllNotifications() throws Exception {
        when(userRepository.findById(any())).thenReturn(Optional.of(user));

        mockMvc.perform(delete("/api/notifications/all")
                        .with(csrf())
                        .with(user(userDetails)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testGetNotification() throws Exception {
        when(userRepository.findById(any())).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/notifications/1")
                        .with(user(userDetails)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testBroadcastNotification() throws Exception {
        mockMvc.perform(post("/api/notifications/broadcast")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"Hello\"}"))
                .andExpect(status().isOk());
    }
}
