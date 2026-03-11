package com.example.revhirehiringplatform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.revhirehiringplatform.dto.request.JobSeekerProfileRequest;
import com.example.revhirehiringplatform.dto.request.ResumeTextRequest;
import com.example.revhirehiringplatform.dto.response.ApplicationResponse;
import com.example.revhirehiringplatform.dto.response.JobSeekerProfileResponse;
import com.example.revhirehiringplatform.dto.response.SkillResponse;
import com.example.revhirehiringplatform.model.JobSeekerProfile;
import com.example.revhirehiringplatform.model.ResumeText;
import com.example.revhirehiringplatform.model.ResumeFiles;
import com.example.revhirehiringplatform.model.User;
import com.example.revhirehiringplatform.security.UserDetailsImpl;
import com.example.revhirehiringplatform.repository.UserRepository;
import com.example.revhirehiringplatform.service.JobSeekerProfileService;
import com.example.revhirehiringplatform.service.JobSeekerResumeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class JobSeekerProfileControllerTest {

    private MockMvc mockMvc;

    @Mock
    private JobSeekerProfileService profileService;

    @Mock
    private JobSeekerResumeService resumeService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private JobSeekerProfileController controller;

    private UserDetailsImpl seekerDetails;
    private UserDetailsImpl employerDetails;
    private User testUser;
    private User testEmployer;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(
                        new org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver())
                .build();

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("seeker@test.com");
        testUser.setRole(User.Role.JOB_SEEKER);

        seekerDetails = new UserDetailsImpl(
                1L, "seeker@test.com", "seeker@test.com", "+1234567890", "password",
                User.Role.JOB_SEEKER,
                null);

        testEmployer = new User();
        testEmployer.setId(2L);
        testEmployer.setEmail("employer@test.com");
        testEmployer.setRole(User.Role.EMPLOYER);

        employerDetails = new UserDetailsImpl(
                2L, "employer@test.com", "employer@test.com", "+0987654321", "password",
                User.Role.EMPLOYER,
                null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(testEmployer));
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }



    @Test
    void testUpdateProfileMultipart() throws Exception {
        JobSeekerProfileRequest inputDto = new JobSeekerProfileRequest();
        inputDto.setHeadline("Java Engineer");

        MockMultipartFile profilePart = new MockMultipartFile(
                "profile",
                "",
                "application/json",
                objectMapper.writeValueAsString(inputDto).getBytes());

        MockMultipartFile resumePart = new MockMultipartFile(
                "resume",
                "resume.pdf",
                "application/pdf",
                "dummy content".getBytes());

        JobSeekerProfile mockProfile = new JobSeekerProfile();
        mockProfile.setId(10L);
        mockProfile.setHeadline("Java Engineer");
        mockProfile.setUser(testUser);

        when(profileService.updateProfile(any(JobSeekerProfileRequest.class), any(), eq(testUser)))
                .thenReturn(mockProfile);

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                seekerDetails, null, seekerDetails.getAuthorities()));
        mockMvc.perform(multipart("/api/seeker/profile")
                        .file(profilePart)
                        .file(resumePart)
                        .principal(new UsernamePasswordAuthenticationToken(seekerDetails, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.headline").value("Java Engineer"));
    }

    @Test
    void testUpdateProfileMultipart_Unauthorized() throws Exception {
        MockMultipartFile profilePart = new MockMultipartFile("profile", "", "application/json",
                "{}".getBytes());

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                employerDetails, null, employerDetails.getAuthorities()));
        mockMvc.perform(multipart("/api/seeker/profile")
                        .file(profilePart)
                        .principal(new UsernamePasswordAuthenticationToken(employerDetails, null)))
                .andExpect(status().isForbidden());
    }



    @Test
    void testUpdateProfileJson() throws Exception {
        JobSeekerProfileRequest inputDto = new JobSeekerProfileRequest();
        inputDto.setHeadline("Software Engineer");

        JobSeekerProfile mockProfile = new JobSeekerProfile();
        mockProfile.setId(10L);
        mockProfile.setHeadline("Software Engineer");
        mockProfile.setUser(testUser);

        when(profileService.updateProfile(any(JobSeekerProfileRequest.class), eq(null), eq(testUser)))
                .thenReturn(mockProfile);

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                seekerDetails, null, seekerDetails.getAuthorities()));
        mockMvc.perform(post("/api/seeker/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto))
                        .principal(new UsernamePasswordAuthenticationToken(seekerDetails, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.headline").value("Software Engineer"));
    }



    @Test
    void testGetProfile() throws Exception {
        JobSeekerProfile mockProfile = new JobSeekerProfile();
        mockProfile.setId(10L);
        mockProfile.setHeadline("Software Engineer");
        mockProfile.setUser(testUser);

        when(profileService.getProfile(testUser)).thenReturn(mockProfile);
        when(profileService.getResumeText(10L)).thenReturn(new ResumeText());

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                seekerDetails, null, seekerDetails.getAuthorities()));
        mockMvc.perform(get("/api/seeker/profile")
                        .principal(new UsernamePasswordAuthenticationToken(seekerDetails, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.headline").value("Software Engineer"));
    }

    @Test
    void testGetProfile_NotFound() throws Exception {
        when(profileService.getProfile(testUser)).thenThrow(new RuntimeException("Not found"));

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                seekerDetails, null, seekerDetails.getAuthorities()));
        mockMvc.perform(get("/api/seeker/profile")
                        .principal(new UsernamePasswordAuthenticationToken(seekerDetails, null)))
                .andExpect(status().isNotFound());
    }



    @Test
    void testGetResumeText() throws Exception {
        JobSeekerProfile mockProfile = new JobSeekerProfile();
        mockProfile.setId(10L);
        mockProfile.setUser(testUser);

        ResumeText mockResumeText = new ResumeText();
        mockResumeText.setSkillsText("Java");

        when(profileService.getProfile(testUser)).thenReturn(mockProfile);
        when(profileService.getResumeText(10L)).thenReturn(mockResumeText);

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                seekerDetails, null, seekerDetails.getAuthorities()));
        mockMvc.perform(get("/api/seeker/profile/resume-text")
                        .principal(new UsernamePasswordAuthenticationToken(seekerDetails, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.skills").value("Java"));
    }



    @Test
    void testUpdateResumeText() throws Exception {
        ResumeTextRequest inputDto = new ResumeTextRequest();
        inputDto.setSkills("Java");

        ResumeText savedText = new ResumeText();
        savedText.setSkillsText("Java");

        when(profileService.updateResumeText(any(ResumeTextRequest.class), eq(testUser)))
                .thenReturn(savedText);

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                seekerDetails, null, seekerDetails.getAuthorities()));
        mockMvc.perform(post("/api/seeker/profile/resume-text")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto))
                        .principal(new UsernamePasswordAuthenticationToken(seekerDetails, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.skillsText").value("Java"));
    }



    @Test
    void testGetProfileById_AsEmployer() throws Exception {
        JobSeekerProfile mockProfile = new JobSeekerProfile();
        mockProfile.setId(10L);
        mockProfile.setHeadline("Java Dev");
        mockProfile.setUser(testUser);

        when(profileService.getProfileById(1L)).thenReturn(mockProfile);

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                employerDetails, null, employerDetails.getAuthorities()));
        mockMvc.perform(get("/api/seeker/profile/1")
                        .principal(new UsernamePasswordAuthenticationToken(employerDetails, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.headline").value("Java Dev"));
    }

    @Test
    void testGetProfileById_AsSeeker_Unauthorized() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                seekerDetails, null, seekerDetails.getAuthorities()));
        mockMvc.perform(get("/api/seeker/profile/1")
                        .principal(new UsernamePasswordAuthenticationToken(seekerDetails, null)))
                .andExpect(status().isForbidden());
    }



    @Test
    void testDownloadResume_AsEmployer_NotFound() throws Exception {
        when(resumeService.getResumeFile(1L)).thenReturn(null);

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                employerDetails, null, employerDetails.getAuthorities()));
        mockMvc.perform(get("/api/seeker/profile/1/resume/download")
                        .principal(new UsernamePasswordAuthenticationToken(employerDetails, null)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDownloadResume_AsSeeker_Unauthorized() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                seekerDetails, null, seekerDetails.getAuthorities()));
        mockMvc.perform(get("/api/seeker/profile/1/resume/download")
                        .principal(new UsernamePasswordAuthenticationToken(seekerDetails, null)))
                .andExpect(status().isForbidden());
    }



    @Test
    void testUpdateProfileById() throws Exception {
        JobSeekerProfileRequest inputDto = new JobSeekerProfileRequest();
        inputDto.setHeadline("Updated");

        JobSeekerProfile mockProfile = new JobSeekerProfile();
        mockProfile.setId(10L);
        mockProfile.setHeadline("Updated");

        when(profileService.updateProfile(any(), eq(null), eq(testUser))).thenReturn(mockProfile);

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                seekerDetails, null, seekerDetails.getAuthorities()));
        mockMvc.perform(put("/api/seeker/profile/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto))
                        .principal(new UsernamePasswordAuthenticationToken(seekerDetails, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.headline").value("Updated"));
    }

    @Test
    void testUpdateProfileById_Unauthorized() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                seekerDetails, null, seekerDetails.getAuthorities()));
        mockMvc.perform(put("/api/seeker/profile/2") // user id 1 trying to update 2
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .principal(new UsernamePasswordAuthenticationToken(seekerDetails, null)))
                .andExpect(status().isForbidden());
    }



    @Test
    void testDeleteProfile() throws Exception {
        doNothing().when(profileService).deleteProfile(1L, testUser);

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                seekerDetails, null, seekerDetails.getAuthorities()));
        mockMvc.perform(delete("/api/seeker/profile/1")
                        .principal(new UsernamePasswordAuthenticationToken(seekerDetails, null)))
                .andExpect(status().isOk())
                .andExpect(content().string("Profile deleted successfully"));
    }



    @Test
    void testGetSeekerApplications() throws Exception {
        ApplicationResponse app = new ApplicationResponse();
        app.setId(100L);
        when(profileService.getSeekerApplications(1L, testUser)).thenReturn(List.of(app));

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                seekerDetails, null, seekerDetails.getAuthorities()));
        mockMvc.perform(get("/api/seeker/profile/1/applications")
                        .principal(new UsernamePasswordAuthenticationToken(seekerDetails, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100));
    }



    @Test
    void testGetSeekerSkills() throws Exception {
        SkillResponse skill = new SkillResponse();
        skill.setId(50L);
        when(profileService.getSeekerSkills(1L)).thenReturn(List.of(skill));

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                seekerDetails, null, seekerDetails.getAuthorities()));
        mockMvc.perform(get("/api/seeker/profile/1/skills")
                        .principal(new UsernamePasswordAuthenticationToken(seekerDetails, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(50));
    }

}
