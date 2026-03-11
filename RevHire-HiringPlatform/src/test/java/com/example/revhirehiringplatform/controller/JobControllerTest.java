package com.example.revhirehiringplatform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.revhirehiringplatform.dto.request.JobPostRequest;
import com.example.revhirehiringplatform.dto.response.JobPostResponse;
import com.example.revhirehiringplatform.model.User;
import com.example.revhirehiringplatform.repository.UserRepository;
import com.example.revhirehiringplatform.security.UserDetailsImpl;
import com.example.revhirehiringplatform.service.JobSearchService;
import com.example.revhirehiringplatform.service.JobService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@WebMvcTest(JobController.class)
@AutoConfigureMockMvc
public class JobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JobService jobService;

    @MockBean
    private JobSearchService jobSearchService;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User employer;
    private UserDetailsImpl userDetails;
    private JobPostRequest jobRequest;
    private JobPostResponse jobResponse;

    @BeforeEach
    void setUp() {
        employer = new User();
        employer.setId(1L);
        employer.setEmail("employer@test.com");
        employer.setRole(User.Role.EMPLOYER);

        userDetails = UserDetailsImpl.build(employer);

        jobRequest = new JobPostRequest();
        jobRequest.setTitle("Software Engineer");
        jobRequest.setDescription("Java developer");
        jobRequest.setCompanyId(10L);
        jobRequest.setDeadline(LocalDate.now().plusDays(10));

        jobResponse = new JobPostResponse();
        jobResponse.setId(100L);
        jobResponse.setTitle("Software Engineer");
    }

    @Test
    @WithMockUser(roles = "EMPLOYER")
    void testCreateJob() throws Exception {
        when(userRepository.findById(any())).thenReturn(Optional.of(employer));
        when(jobService.createJob(any(), any())).thenReturn(jobResponse);

        mockMvc.perform(post("/api/jobs")
                        .with(csrf())
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(jobRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Software Engineer"));
    }

    @Test
    @WithMockUser
    void testGetJobById() throws Exception {
        when(jobService.getJobById(100L)).thenReturn(new com.example.revhirehiringplatform.model.JobPost());
        when(jobService.mapToDto(any())).thenReturn(jobResponse);

        mockMvc.perform(get("/api/jobs/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100));
    }

    @Test
    @WithMockUser
    void testGetAllJobs() throws Exception {
        when(jobSearchService.searchJobs(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(java.util.Collections.singletonList(jobResponse));

        mockMvc.perform(get("/api/jobs")
                        .param("title", "Software Engineer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Software Engineer"));
    }

    @Test
    @WithMockUser(roles = "EMPLOYER")
    void testGetMyJobs() throws Exception {
        when(userRepository.findById(any())).thenReturn(Optional.of(employer));
        when(jobService.getMyJobs(any())).thenReturn(java.util.Collections.singletonList(jobResponse));

        mockMvc.perform(get("/api/jobs/my-jobs")
                        .with(user(userDetails)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "JOB_SEEKER")
    void testGetRecommendations() throws Exception {
        User seeker = new User();
        seeker.setId(2L);
        seeker.setRole(User.Role.JOB_SEEKER);
        when(userRepository.findById(any())).thenReturn(Optional.of(seeker));

        mockMvc.perform(get("/api/jobs/recommendations")
                        .with(user(UserDetailsImpl.build(seeker))))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "EMPLOYER")
    void testUpdateJob() throws Exception {
        when(userRepository.findById(any())).thenReturn(Optional.of(employer));
        when(jobService.updateJob(any(), any(), any())).thenReturn(jobResponse);

        mockMvc.perform(put("/api/jobs/100")
                        .with(csrf())
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(jobRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "EMPLOYER")
    void testUpdateJobStatus() throws Exception {
        when(userRepository.findById(any())).thenReturn(Optional.of(employer));

        mockMvc.perform(put("/api/jobs/100/status")
                        .param("status", "CLOSED")
                        .with(csrf())
                        .with(user(userDetails)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "EMPLOYER")
    void testDeleteJob() throws Exception {
        when(userRepository.findById(any())).thenReturn(Optional.of(employer));

        mockMvc.perform(delete("/api/jobs/100")
                        .with(csrf())
                        .with(user(userDetails)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "EMPLOYER")
    void testGetJobApplications() throws Exception {
        when(userRepository.findById(any())).thenReturn(Optional.of(employer));

        mockMvc.perform(get("/api/jobs/100/applications")
                        .with(user(userDetails)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testGetJobSkills() throws Exception {
        mockMvc.perform(get("/api/jobs/100/skills"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "JOB_SEEKER")
    void testCreateJob_UnauthorizedRole() throws Exception {
        mockMvc.perform(post("/api/jobs")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(jobRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "EMPLOYER")
    void testCreateJob_BadRequest() throws Exception {
        when(userRepository.findById(any())).thenReturn(Optional.of(employer));
        when(jobService.createJob(any(), any())).thenThrow(new RuntimeException("Bad data message"));

        mockMvc.perform(post("/api/jobs")
                        .with(csrf())
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(jobRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Bad data message"));
    }

    @Test
    @WithMockUser(roles = "JOB_SEEKER")
    void testUpdateJob_UnauthorizedRole() throws Exception {
        mockMvc.perform(put("/api/jobs/100")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(jobRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "EMPLOYER")
    void testUpdateJob_BadRequest() throws Exception {
        when(userRepository.findById(any())).thenReturn(Optional.of(employer));
        when(jobService.updateJob(any(), any(), any())).thenThrow(new RuntimeException("Update failed"));

        mockMvc.perform(put("/api/jobs/100")
                        .with(csrf())
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(jobRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Update failed"));
    }

    @Test
    @WithMockUser(roles = "JOB_SEEKER")
    void testDeleteJob_UnauthorizedRole() throws Exception {
        mockMvc.perform(delete("/api/jobs/100")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "EMPLOYER")
    void testDeleteJob_BadRequest() throws Exception {
        when(userRepository.findById(any())).thenReturn(Optional.of(employer));
        doThrow(new RuntimeException("Delete failed")).when(jobService).deleteJob(any(), any());

        mockMvc.perform(delete("/api/jobs/100")
                        .with(csrf())
                        .with(user(userDetails)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Delete failed"));
    }

}