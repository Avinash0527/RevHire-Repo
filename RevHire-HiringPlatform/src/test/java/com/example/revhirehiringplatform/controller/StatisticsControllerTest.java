package com.example.revhirehiringplatform.controller;

import com.example.revhirehiringplatform.model.JobPost;
import com.example.revhirehiringplatform.model.Application;
import com.example.revhirehiringplatform.model.User;
import com.example.revhirehiringplatform.repository.ApplicationRepository;
import com.example.revhirehiringplatform.repository.JobPostRepository;
import com.example.revhirehiringplatform.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StatisticsController.class)
@AutoConfigureMockMvc(addFilters = false)
public class StatisticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JobPostRepository jobPostRepository;

    @MockBean
    private ApplicationRepository applicationRepository;

    @MockBean
    private UserRepository userRepository;

    @Test
    @WithMockUser
    void testGetJobsByLocation() throws Exception {
        JobPost job = new JobPost();
        job.setLocation("New York");
        when(jobPostRepository.findAll()).thenReturn(List.of(job));

        mockMvc.perform(get("/api/stats/jobs/by-location"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testGetJobsByType() throws Exception {
        JobPost job = new JobPost();
        job.setJobType("FULL_TIME");
        when(jobPostRepository.findAll()).thenReturn(List.of(job));

        mockMvc.perform(get("/api/stats/jobs/by-type"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testGetApplicationsByStatus() throws Exception {
        Application app = new Application();
        app.setStatus(Application.ApplicationStatus.APPLIED);
        when(applicationRepository.findAll()).thenReturn(List.of(app));

        mockMvc.perform(get("/api/stats/applications/by-status"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testGetUsersByRole() throws Exception {
        User user = new User();
        user.setRole(User.Role.JOB_SEEKER);
        when(userRepository.findAll()).thenReturn(List.of(user));

        mockMvc.perform(get("/api/stats/users/by-role"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testGetTrendingJobs() throws Exception {
        mockMvc.perform(get("/api/stats/jobs/trending"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testGetConversionRate() throws Exception {
        mockMvc.perform(get("/api/stats/applications/conversion-rate"))
                .andExpect(status().isOk());
    }
}
