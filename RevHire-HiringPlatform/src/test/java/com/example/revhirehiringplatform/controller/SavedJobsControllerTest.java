package com.example.revhirehiringplatform.controller;

import com.example.revhirehiringplatform.model.User;
import com.example.revhirehiringplatform.repository.UserRepository;
import com.example.revhirehiringplatform.security.UserDetailsImpl;
import com.example.revhirehiringplatform.service.SavedJobsService;
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

@WebMvcTest(SavedJobsController.class)
@AutoConfigureMockMvc
public class SavedJobsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SavedJobsService savedJobsService;

    @MockBean
    private UserRepository userRepository;

    private User seeker;
    private UserDetailsImpl userDetails;

    @BeforeEach
    void setUp() {
        seeker = new User();
        seeker.setId(1L);
        seeker.setRole(User.Role.JOB_SEEKER);
        userDetails = UserDetailsImpl.build(seeker);
    }

    @Test
    @WithMockUser
    void testSaveJob() throws Exception {
        when(userRepository.findById(any())).thenReturn(Optional.of(seeker));

        mockMvc.perform(post("/api/seeker/saved-jobs/100")
                        .with(csrf())
                        .with(user(userDetails)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testUnsaveJob() throws Exception {
        when(userRepository.findById(any())).thenReturn(Optional.of(seeker));

        mockMvc.perform(delete("/api/seeker/saved-jobs/100")
                        .with(csrf())
                        .with(user(userDetails)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testGetSavedJobs() throws Exception {
        when(userRepository.findById(any())).thenReturn(Optional.of(seeker));
        when(savedJobsService.getSavedJobs(any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/seeker/saved-jobs")
                        .with(user(userDetails)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testIsJobSaved() throws Exception {
        when(userRepository.findById(any())).thenReturn(Optional.of(seeker));

        mockMvc.perform(get("/api/seeker/saved-jobs/100/check")
                        .with(user(userDetails)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testClearSavedJobs() throws Exception {
        when(userRepository.findById(any())).thenReturn(Optional.of(seeker));

        mockMvc.perform(delete("/api/seeker/saved-jobs/clear")
                        .with(csrf())
                        .with(user(userDetails)))
                .andExpect(status().isOk());
    }
}

