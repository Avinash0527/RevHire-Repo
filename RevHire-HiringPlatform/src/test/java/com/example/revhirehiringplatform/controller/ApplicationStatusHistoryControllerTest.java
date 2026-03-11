package com.example.revhirehiringplatform.controller;



import com.example.revhirehiringplatform.model.User;
import com.example.revhirehiringplatform.repository.ApplicationRepository;
import com.example.revhirehiringplatform.repository.ApplicationStatusHistoryRepository;
import com.example.revhirehiringplatform.repository.UserRepository;
import com.example.revhirehiringplatform.security.UserDetailsImpl;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApplicationStatusHistoryController.class)
@AutoConfigureMockMvc
public class ApplicationStatusHistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApplicationStatusHistoryRepository historyRepository;

    @MockBean
    private ApplicationRepository applicationRepository;

    @MockBean
    private UserRepository userRepository;

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
    void testGetHistoryForApplication() throws Exception {
        when(userRepository.findById(any())).thenReturn(Optional.of(employer));

        com.example.revhirehiringplatform.model.Application app = new com.example.revhirehiringplatform.model.Application();
        com.example.revhirehiringplatform.model.JobPost job = new com.example.revhirehiringplatform.model.JobPost();
        job.setCreatedBy(employer);
        app.setJobPost(job);

        com.example.revhirehiringplatform.model.JobSeekerProfile seekerProfile = new com.example.revhirehiringplatform.model.JobSeekerProfile();
        User seekerUser = new User();
        seekerUser.setId(2L);
        seekerProfile.setUser(seekerUser);
        app.setJobSeeker(seekerProfile);

        when(applicationRepository.findById(any())).thenReturn(Optional.of(app));
        when(historyRepository.findByApplicationIdOrderByChangedAtDesc(any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/applications/1/history")
                        .with(user(userDetails)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testGetHistoryForApplication_UserNotFound() throws Exception {
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/applications/1/history")
                        .with(user(userDetails)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void testGetHistoryForApplication_AppNotFound() throws Exception {
        when(userRepository.findById(any())).thenReturn(Optional.of(employer));
        when(applicationRepository.findById(any())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/applications/1/history")
                        .with(user(userDetails)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void testGetHistoryForApplication_UnauthorizedToView() throws Exception {
        User otherUser = new User();
        otherUser.setId(99L);
        otherUser.setRole(User.Role.JOB_SEEKER);
        when(userRepository.findById(any())).thenReturn(Optional.of(otherUser));

        com.example.revhirehiringplatform.model.Application app = new com.example.revhirehiringplatform.model.Application();
        com.example.revhirehiringplatform.model.JobPost job = new com.example.revhirehiringplatform.model.JobPost();
        job.setCreatedBy(employer);
        app.setJobPost(job);

        com.example.revhirehiringplatform.model.JobSeekerProfile seekerProfile = new com.example.revhirehiringplatform.model.JobSeekerProfile();
        User seekerUser = new User();
        seekerUser.setId(2L);
        seekerProfile.setUser(seekerUser);
        app.setJobSeeker(seekerProfile);

        when(applicationRepository.findById(any())).thenReturn(Optional.of(app));

        mockMvc.perform(get("/api/applications/1/history")
                        .with(user(UserDetailsImpl.build(otherUser))))
                .andExpect(status().isForbidden());
    }
}
