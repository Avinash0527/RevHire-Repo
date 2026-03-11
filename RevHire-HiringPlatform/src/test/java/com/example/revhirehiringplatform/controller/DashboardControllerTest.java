package com.example.revhirehiringplatform.controller;
import com.example.revhirehiringplatform.model.User;
import com.example.revhirehiringplatform.repository.ApplicationRepository;
import com.example.revhirehiringplatform.repository.JobPostRepository;
import com.example.revhirehiringplatform.repository.UserRepository;
import com.example.revhirehiringplatform.security.UserDetailsImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class DashboardControllerTest {

    private MockMvc mockMvc;

    @Mock
    private JobPostRepository jobPostRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DashboardController dashboardController;

    private UserDetailsImpl userDetails;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(dashboardController)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();

        userDetails = new UserDetailsImpl(
                1L, "username", "user@test.com", "1234567890", "password",
                User.Role.JOB_SEEKER, Collections.emptyList());

        authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testGetAdminMetrics() throws Exception {
        when(userRepository.count()).thenReturn(10L);
        when(jobPostRepository.count()).thenReturn(5L);
        when(applicationRepository.count()).thenReturn(20L);

        mockMvc.perform(get("/api/dashboard/admin/metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(10))
                .andExpect(jsonPath("$.totalJobs").value(5))
                .andExpect(jsonPath("$.totalApplications").value(20));
    }

    @Test
    void testGetEmployerMetrics() throws Exception {
        mockMvc.perform(get("/api/dashboard/employer/metrics")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }

    @Test
    void testGetSeekerMetrics_WithPrincipal() throws Exception {
        mockMvc.perform(get("/api/dashboard/seeker/metrics")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.seekerId").value(1));
    }

    @Test
    void testGetSystemHealth() throws Exception {
        mockMvc.perform(get("/api/dashboard/system/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.database").value("CONNECTED"));
    }

    @Test
    void testGetSystemLogs() throws Exception {
        mockMvc.perform(get("/api/dashboard/system/logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))));
    }
}
