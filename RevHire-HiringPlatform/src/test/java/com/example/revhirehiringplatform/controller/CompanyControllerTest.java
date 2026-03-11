package com.example.revhirehiringplatform.controller;


import com.example.revhirehiringplatform.dto.response.JobPostResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.revhirehiringplatform.dto.request.CompanyRequest;
import com.example.revhirehiringplatform.dto.response.CompanyResponse;
import com.example.revhirehiringplatform.model.User;
import com.example.revhirehiringplatform.model.JobPost;
import com.example.revhirehiringplatform.model.Application;
import com.example.revhirehiringplatform.repository.UserRepository;
import com.example.revhirehiringplatform.security.UserDetailsImpl;
import com.example.revhirehiringplatform.service.CompanyService;
import com.example.revhirehiringplatform.service.JobService;
import com.example.revhirehiringplatform.repository.JobPostRepository;
import com.example.revhirehiringplatform.repository.ApplicationRepository;
import com.example.revhirehiringplatform.repository.EmployerProfileRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class CompanyControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CompanyService companyService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JobPostRepository jobPostRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private EmployerProfileRepository employerProfileRepository;

    @Mock
    private JobService jobService;

    @InjectMocks
    private CompanyController companyController;

    private ObjectMapper objectMapper = new ObjectMapper();

    private User employer;
    private UserDetailsImpl userDetails;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(companyController)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();

        employer = new User();
        employer.setId(1L);
        employer.setEmail("employer@test.com");
        employer.setRole(User.Role.EMPLOYER);

        userDetails = new UserDetailsImpl(
                1L, "username", "employer@test.com", "1234567890", "password", User.Role.EMPLOYER,
                Collections.emptyList());

        authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
                userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testUpdateProfile_Success() throws Exception {
        CompanyRequest req = new CompanyRequest();
        req.setName("Test Co");

        CompanyResponse res = new CompanyResponse();
        res.setId(10L);
        res.setName("Test Co");

        when(userRepository.findById(1L)).thenReturn(Optional.of(employer));
        when(companyService.createOrUpdateCompanyProfile(any(CompanyRequest.class), eq(employer)))
                .thenReturn(res);

        mockMvc.perform(post("/api/company/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void testUpdateProfile_Unauthorized() throws Exception {
        User seeker = new User();
        seeker.setId(2L);
        seeker.setRole(User.Role.JOB_SEEKER);

        UserDetailsImpl seekerDetails = new UserDetailsImpl(
                2L, "user2", "seeker@test.com", "123", "pass", User.Role.JOB_SEEKER,
                Collections.emptyList());
        Authentication seekerAuth = new UsernamePasswordAuthenticationToken(seekerDetails, null,
                seekerDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(seekerAuth);

        when(userRepository.findById(2L)).thenReturn(Optional.of(seeker));

        CompanyRequest validReq = new CompanyRequest();
        validReq.setName("Valid Name");
        validReq.setDescription("Valid Description");

        mockMvc.perform(post("/api/company/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validReq))
                        .principal(seekerAuth))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Unauthorized"));
    }

    @Test
    void testGetMyCompanies() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(employer));

        com.example.revhirehiringplatform.model.Company mockCompany = new com.example.revhirehiringplatform.model.Company();
        mockCompany.setId(10L);
        when(companyService.getCompaniesForUser(employer)).thenReturn(List.of(mockCompany));

        CompanyResponse mockRes1 = new CompanyResponse();
        mockRes1.setId(10L);
        when(companyService.getCompanyById(10L)).thenReturn(mockRes1);

        mockMvc.perform(get("/api/company/register/companies")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10));
    }

    @Test
    void testGetProfile_Success() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(employer));

        CompanyResponse res = new CompanyResponse();
        res.setId(10L);
        when(companyService.getCompanyProfile(employer)).thenReturn(res);

        mockMvc.perform(get("/api/company/register")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void testGetProfile_NoContent() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(employer));
        when(companyService.getCompanyProfile(employer)).thenReturn(null);

        mockMvc.perform(get("/api/company/register")
                        .principal(authentication))
                .andExpect(status().isNoContent());
    }

    @Test
    void testGetProfileById_Success() throws Exception {
        CompanyResponse res = new CompanyResponse();
        res.setId(5L);
        when(companyService.getCompanyById(5L)).thenReturn(res);

        mockMvc.perform(get("/api/company/register/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void testGetProfileById_NotFound() throws Exception {
        when(companyService.getCompanyById(5L)).thenThrow(new RuntimeException("Not found"));

        mockMvc.perform(get("/api/company/register/5"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetDashboard() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(employer));

        JobPost activeJob = new JobPost();
        activeJob.setStatus(JobPost.JobStatus.ACTIVE);
        JobPost closedJob = new JobPost();
        closedJob.setStatus(JobPost.JobStatus.CLOSED);
        when(jobPostRepository.findByCreatedBy(employer)).thenReturn(List.of(activeJob, closedJob));

        Application app = new Application();
        app.setStatus(Application.ApplicationStatus.REVIEWING);
        when(applicationRepository.findByJobPostCreatedBy(employer)).thenReturn(List.of(app));

        mockMvc.perform(get("/api/company/register/dashboard")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalJobs").value(2))
                .andExpect(jsonPath("$.activeJobs").value(1))
                .andExpect(jsonPath("$.totalApplications").value(1))
                .andExpect(jsonPath("$.pendingReviews").value(1));
    }

    @Test
    void testUpdateCompany_Success() throws Exception {
        CompanyRequest req = new CompanyRequest();
        req.setName("Updated Co");

        CompanyResponse res = new CompanyResponse();
        res.setId(5L);
        res.setName("Updated Co");

        when(userRepository.findById(1L)).thenReturn(Optional.of(employer));
        when(companyService.createOrUpdateCompanyProfile(any(CompanyRequest.class), eq(employer)))
                .thenReturn(res);

        mockMvc.perform(put("/api/company/register/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Co"));
    }

    @Test
    void testDeleteCompany_Success() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(employer));
        doNothing().when(companyService).deleteCompany(5L, employer);

        mockMvc.perform(delete("/api/company/register/5")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(content().string("Company deleted successfully"));
    }

    @Test
    void testGetCompanyJobs() throws Exception {
        JobPost mockJob = new JobPost();
        mockJob.setId(101L);
        when(jobPostRepository.findByCompanyId(10L)).thenReturn(List.of(mockJob));
        JobPostResponse mockJobRes = new JobPostResponse();
        mockJobRes.setId(101L);
        when(jobService.mapToDto(any(JobPost.class))).thenReturn(mockJobRes);

        mockMvc.perform(get("/api/company/register/10/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(101));
    }

    @Test
    void testGetCompanyEmployees() throws Exception {
        com.example.revhirehiringplatform.model.EmployerProfile empProfile = new com.example.revhirehiringplatform.model.EmployerProfile();
        empProfile.setId(201L);
        when(employerProfileRepository.findByCompanyId(10L)).thenReturn(List.of(empProfile));

        mockMvc.perform(get("/api/company/register/10/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(201));
    }
}
