package com.example.revhirehiringplatform.controller;

import com.example.revhirehiringplatform.model.User;
import com.example.revhirehiringplatform.repository.UserRepository;
import com.example.revhirehiringplatform.security.UserDetailsImpl;
import com.example.revhirehiringplatform.service.SavedResumeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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

@WebMvcTest(SavedResumeController.class)
@AutoConfigureMockMvc
public class SavedResumeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SavedResumeService savedResumeService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private com.example.revhirehiringplatform.repository.SavedResumeRepository savedResumeRepository;

    private User employer;
    private UserDetailsImpl userDetails;

    @BeforeEach
    void setUp() {
        employer = new User();
        employer.setId(1L);
        employer.setRole(User.Role.EMPLOYER);
        userDetails = UserDetailsImpl.build(employer);
    }

    @Test
    @WithMockUser
    void testSaveResume() throws Exception {
        when(userRepository.findById(any())).thenReturn(Optional.of(employer));

        mockMvc.perform(post("/api/employer/saved-resumes/200")
                        .param("jobId", "300")
                        .with(csrf())
                        .with(user(userDetails)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testUnsaveResume() throws Exception {
        when(userRepository.findById(any())).thenReturn(Optional.of(employer));

        mockMvc.perform(delete("/api/employer/saved-resumes/200")
                        .param("jobId", "300")
                        .with(csrf())
                        .with(user(userDetails)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testGetSavedResumes() throws Exception {
        when(userRepository.findById(any())).thenReturn(Optional.of(employer));
        when(savedResumeService.getSavedResumes(any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/employer/saved-resumes")
                        .with(user(userDetails)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testIsResumeSaved() throws Exception {
        when(userRepository.findById(any())).thenReturn(Optional.of(employer));

        mockMvc.perform(get("/api/employer/saved-resumes/200/check")
                        .param("jobId", "300")
                        .with(user(userDetails)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testClearSavedResumes() throws Exception {
        when(userRepository.findById(any())).thenReturn(Optional.of(employer));

        mockMvc.perform(delete("/api/employer/saved-resumes/clear")
                        .with(csrf())
                        .with(user(userDetails)))
                .andExpect(status().isOk());
    }
}

