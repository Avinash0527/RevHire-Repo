package com.example.revhirehiringplatform.service;

import com.example.revhirehiringplatform.dto.request.JobSeekerProfileRequest;
import com.example.revhirehiringplatform.dto.request.ResumeTextRequest;
import com.example.revhirehiringplatform.model.JobSeekerProfile;
import com.example.revhirehiringplatform.model.ResumeText;
import com.example.revhirehiringplatform.model.User;
import com.example.revhirehiringplatform.repository.JobSeekerProfileRepository;
import com.example.revhirehiringplatform.repository.ResumeTextRepository;
import com.example.revhirehiringplatform.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class JobSeekerProfileServiceTest {

    @Mock
    private JobSeekerProfileRepository profileRepository;

    @Mock
    private ResumeTextRepository resumeTextRepository;

    @Mock
    private JobSeekerResumeService resumeService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private com.example.revhirehiringplatform.repository.SkillsMasterRepository skillsMasterRepository;

    @Mock
    private com.example.revhirehiringplatform.repository.SeekerSkillMapRepository seekerSkillMapRepository;

    @Mock
    private com.example.revhirehiringplatform.repository.ApplicationRepository applicationRepository;

    @InjectMocks
    private JobSeekerProfileService profileService;

    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@ex.com");
    }

    @Test
    void testUpdateProfile_WithPhone_UpdatesUserPhone() {
        JobSeekerProfileRequest dto = new JobSeekerProfileRequest();
        dto.setHeadline("Dev");
        dto.setPhone("+111222333");

        JobSeekerProfile existingProfile = new JobSeekerProfile();
        existingProfile.setUser(testUser);

        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(existingProfile));
        when(profileRepository.save(any(JobSeekerProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        JobSeekerProfile result = profileService.updateProfile(dto, null, testUser);

        assertNotNull(result);
        assertEquals("Dev", result.getHeadline());
        assertEquals("+111222333", testUser.getPhone());
        verify(userRepository, times(1)).save(testUser);
        verify(profileRepository, times(1)).save(any(JobSeekerProfile.class));
    }

    @Test
    void testUpdateProfile_WithoutPhone_DoesNotUpdateUser() {
        JobSeekerProfileRequest dto = new JobSeekerProfileRequest();
        dto.setHeadline("Dev");


        JobSeekerProfile existingProfile = new JobSeekerProfile();
        existingProfile.setUser(testUser);

        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(existingProfile));
        when(profileRepository.save(any(JobSeekerProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        JobSeekerProfile result = profileService.updateProfile(dto, null, testUser);

        assertNotNull(result);
        assertEquals("Dev", result.getHeadline());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testUpdateProfile_WithResume_CallsResumeService() {
        JobSeekerProfileRequest dto = new JobSeekerProfileRequest();
        dto.setHeadline("Dev");

        MockMultipartFile resumeFile = new MockMultipartFile(
                "resume", "test.pdf", "application/pdf", "data".getBytes());

        JobSeekerProfile existingProfile = new JobSeekerProfile();
        existingProfile.setUser(testUser);

        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(existingProfile));
        when(profileRepository.save(any(JobSeekerProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        profileService.updateProfile(dto, resumeFile, testUser);

        verify(resumeService, times(1)).storeFile(resumeFile, existingProfile);
    }

    @Test
    void testUpdateResumeText() {
        ResumeTextRequest dto = new ResumeTextRequest();
        dto.setSkills("Java, Spring");

        JobSeekerProfile existingProfile = new JobSeekerProfile();
        existingProfile.setId(10L);
        existingProfile.setUser(testUser);

        when(profileRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(existingProfile));
        when(resumeTextRepository.findByJobSeekerId(10L)).thenReturn(Optional.empty());
        when(resumeTextRepository.save(any(ResumeText.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResumeText result = profileService.updateResumeText(dto, testUser);

        assertNotNull(result);
        assertEquals("Java, Spring", result.getSkillsText());
        assertEquals(existingProfile, result.getJobSeeker());

        verify(resumeTextRepository, times(1)).save(any(ResumeText.class));
    }

    @Test
    void testGetProfile() {
        JobSeekerProfile profile = new JobSeekerProfile();
        when(profileRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(profile));
        JobSeekerProfile result = profileService.getProfile(testUser);
        assertNotNull(result);
    }

    @Test
    void testGetResumeText() {
        ResumeText rt = new ResumeText();
        when(resumeTextRepository.findByJobSeekerId(10L)).thenReturn(Optional.of(rt));
        ResumeText result = profileService.getResumeText(10L);
        assertNotNull(result);
    }

    @Test
    void testGetProfileById() {
        JobSeekerProfile profile = new JobSeekerProfile();
        when(profileRepository.findById(10L)).thenReturn(Optional.of(profile));
        JobSeekerProfile result = profileService.getProfileById(10L);
        assertNotNull(result);
    }

    @Test
    void testDeleteProfile() {
        JobSeekerProfile profile = new JobSeekerProfile();
        User profileUser = new User();
        profileUser.setId(1L);
        profileUser.setRole(User.Role.JOB_SEEKER);
        profile.setUser(profileUser);
        when(profileRepository.findById(10L)).thenReturn(Optional.of(profile));

        profileService.deleteProfile(10L, profileUser);

        verify(userRepository, times(1)).save(profileUser);
    }

    @Test
    void testGetSeekerApplications() {
        com.example.revhirehiringplatform.model.JobPost jobPost = new com.example.revhirehiringplatform.model.JobPost();
        jobPost.setId(1L);
        jobPost.setTitle("Title");
        com.example.revhirehiringplatform.model.Company company = new com.example.revhirehiringplatform.model.Company();
        company.setName("Comp");
        jobPost.setCompany(company);

        com.example.revhirehiringplatform.model.Application app = new com.example.revhirehiringplatform.model.Application();
        app.setId(10L);
        app.setJobPost(jobPost);
        JobSeekerProfile p = new JobSeekerProfile();
        p.setId(5L);
        User u = new User();
        u.setName("Name");
        u.setEmail("E");
        p.setUser(u);
        app.setJobSeeker(p);

        when(applicationRepository.findByJobSeekerId(10L)).thenReturn(java.util.List.of(app));
        java.util.List<com.example.revhirehiringplatform.dto.response.ApplicationResponse> apps = profileService.getSeekerApplications(10L,
                testUser);
        assertEquals(1, apps.size());
    }

    @Test
    void testGetSeekerSkills() {
        com.example.revhirehiringplatform.model.SeekerSkillMap ssm = new com.example.revhirehiringplatform.model.SeekerSkillMap();
        com.example.revhirehiringplatform.model.SkillsMaster sm = new com.example.revhirehiringplatform.model.SkillsMaster();
        sm.setId(1L);
        sm.setSkillName("Java");
        ssm.setSkill(sm);
        when(seekerSkillMapRepository.findByJobSeekerId(10L)).thenReturn(java.util.List.of(ssm));
        java.util.List<com.example.revhirehiringplatform.dto.response.SkillResponse> skills = profileService.getSeekerSkills(10L);
        assertEquals(1, skills.size());
    }
}
