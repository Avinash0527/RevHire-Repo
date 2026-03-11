package com.example.revhirehiringplatform.controller;

import com.example.revhirehiringplatform.dto.response.ApplicationResponse;
import com.example.revhirehiringplatform.model.User;
import com.example.revhirehiringplatform.repository.UserRepository;
import com.example.revhirehiringplatform.security.UserDetailsImpl;
import com.example.revhirehiringplatform.service.ApplicationService;
import com.example.revhirehiringplatform.service.ApplicationUpdateService;
import com.example.revhirehiringplatform.service.ApplicationWithdrawalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@WebMvcTest(ApplicationController.class)
@AutoConfigureMockMvc
public class ApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApplicationService applicationService;

    @MockBean
    private ApplicationUpdateService applicationUpdateService;

    @MockBean
    private ApplicationWithdrawalService withdrawalService;

    @MockBean
    private UserRepository userRepository;

    private User seeker;
    private UserDetailsImpl userDetails;

    @BeforeEach
    void setUp() {
        seeker = new User();
        seeker.setId(1L);
        seeker.setEmail("seeker@test.com");
        seeker.setRole(User.Role.JOB_SEEKER);
        userDetails = UserDetailsImpl.build(seeker);
    }

    @Test
    @WithMockUser(roles = "JOB_SEEKER")
    void testApplyForJob() throws Exception {
        when(userRepository.findById(any())).thenReturn(Optional.of(seeker));
        when(applicationService.applyForJob(any(), any(), any(), any())).thenReturn(new ApplicationResponse());

        mockMvc.perform(post("/api/applications/apply/100")
                        .with(csrf())
                        .with(user(userDetails)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "JOB_SEEKER")
    void testGetMyApplications() throws Exception {
        when(userRepository.findById(any())).thenReturn(Optional.of(seeker));
        when(applicationService.getMyApplications(any())).thenReturn(java.util.Collections.emptyList());

        mockMvc.perform(get("/api/applications/my-applications")
                        .with(user(userDetails)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "EMPLOYER")
    void testGetApplicationsForJob() throws Exception {
        User employer = new User();
        employer.setId(2L);
        employer.setRole(User.Role.EMPLOYER);
        when(userRepository.findById(any())).thenReturn(Optional.of(employer));

        mockMvc.perform(get("/api/applications/job/100")
                        .with(user(UserDetailsImpl.build(employer))))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "EMPLOYER")
    void testUpdateStatus() throws Exception {
        User employer = new User();
        employer.setId(2L);
        employer.setRole(User.Role.EMPLOYER);
        when(userRepository.findById(any())).thenReturn(Optional.of(employer));

        mockMvc.perform(put("/api/applications/1/status")
                        .param("status", "SHORTLISTED")
                        .with(csrf())
                        .with(user(UserDetailsImpl.build(employer))))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "JOB_SEEKER")
    void testWithdrawApplication() throws Exception {
        when(userRepository.findById(any())).thenReturn(Optional.of(seeker));

        mockMvc.perform(put("/api/applications/1/withdraw")
                        .param("reason", "Found another job")
                        .with(csrf())
                        .with(user(userDetails)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "EMPLOYER")
    void testUpdateBulkStatus() throws Exception {
        User employer = new User();
        employer.setId(2L);
        employer.setRole(User.Role.EMPLOYER);
        when(userRepository.findById(any())).thenReturn(Optional.of(employer));

        mockMvc.perform(put("/api/applications/bulk-status")
                        .with(csrf())
                        .with(user(UserDetailsImpl.build(employer)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"applicationIds\":[1,2], \"status\":\"REJECTED\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "EMPLOYER")
    void testSearchApplicantsForJob() throws Exception {
        User employer = new User();
        employer.setId(2L);
        employer.setRole(User.Role.EMPLOYER);
        when(userRepository.findById(any())).thenReturn(Optional.of(employer));

        mockMvc.perform(get("/api/applications/job/100/search")
                        .param("name", "John")
                        .param("status", "APPLIED")
                        .with(user(UserDetailsImpl.build(employer))))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "JOB_SEEKER")
    void testDeleteApplication() throws Exception {
        when(userRepository.findById(any())).thenReturn(Optional.of(seeker));

        mockMvc.perform(delete("/api/applications/1")
                        .with(csrf())
                        .with(user(userDetails)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "EMPLOYER")
    void testGetApplicationSummary() throws Exception {
        User employer = new User();
        employer.setId(2L);
        employer.setRole(User.Role.EMPLOYER);
        when(userRepository.findById(any())).thenReturn(Optional.of(employer));

        mockMvc.perform(get("/api/applications/job/100/summary")
                        .with(user(UserDetailsImpl.build(employer))))
                .andExpect(status().isOk());
    }
}
