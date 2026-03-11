package com.example.revhirehiringplatform.service;

import com.example.revhirehiringplatform.dto.response.ApplicationResponse;
import com.example.revhirehiringplatform.model.Application;
import com.example.revhirehiringplatform.model.JobPost;
import com.example.revhirehiringplatform.model.JobSeekerProfile;
import com.example.revhirehiringplatform.model.ResumeText;
import com.example.revhirehiringplatform.model.User;
import com.example.revhirehiringplatform.repository.ApplicationRepository;
import com.example.revhirehiringplatform.repository.JobPostRepository;
import com.example.revhirehiringplatform.repository.JobSeekerProfileRepository;
import com.example.revhirehiringplatform.repository.ResumeTextRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private JobPostRepository jobPostRepository;

    @Mock
    private JobSeekerProfileRepository profileRepository;

    @Mock
    private ResumeTextRepository resumeTextRepository;

    @Mock
    private com.example.revhirehiringplatform.repository.SeekerSkillMapRepository seekerSkillMapRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private com.example.revhirehiringplatform.repository.ApplicationStatusHistoryRepository statusHistoryRepository;

    @Mock
    private com.example.revhirehiringplatform.service.AuditLogService auditLogService;

    @InjectMocks
    private ApplicationService applicationService;

    private User employer;
    private User seeker;
    private JobPost jobPost;
    private JobSeekerProfile profile;
    private Application application;
    private ResumeText resumeText;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        employer = new User();
        employer.setId(1L);
        employer.setRole(User.Role.EMPLOYER);

        seeker = new User();
        seeker.setId(2L);
        seeker.setName("Alice Applicant");
        seeker.setEmail("alice@test.com");
        seeker.setRole(User.Role.JOB_SEEKER);

        jobPost = new JobPost();
        jobPost.setId(10L);
        jobPost.setTitle("Java Dev");
        jobPost.setCreatedBy(employer);
        com.example.revhirehiringplatform.model.Company company = new com.example.revhirehiringplatform.model.Company();
        company.setName("Tech Corp");
        jobPost.setCompany(company);

        profile = new JobSeekerProfile();
        profile.setId(20L);
        profile.setUser(seeker);
        profile.setSummary("Great developer");

        application = new Application();
        application.setId(100L);
        application.setJobPost(jobPost);
        application.setJobSeeker(profile);
        application.setStatus(Application.ApplicationStatus.APPLIED);
        application.setAppliedAt(LocalDateTime.now().minusDays(2));

        resumeText = new ResumeText();
        resumeText.setJobSeeker(profile);
        resumeText.setSkillsText("Java, Spring, SQL");
        resumeText.setExperienceText("5 years building web apps");
        resumeText.setEducationText("BS Computer Science");

        when(jobPostRepository.findById(10L)).thenReturn(Optional.of(jobPost));
        when(applicationRepository.findByJobPostId(10L)).thenReturn(Collections.singletonList(application));
        when(resumeTextRepository.findByJobSeekerId(20L)).thenReturn(Optional.of(resumeText));
        when(seekerSkillMapRepository.findByJobSeekerId(20L)).thenReturn(Collections.emptyList());
    }

    @Test
    void testSearchApplicantsForJob_NoFilters() {
        List<ApplicationResponse> results = applicationService.searchApplicantsForJob(
                10L, null, null, null, null, null, null, employer);

        assertEquals(1, results.size());
        ApplicationResponse dto = results.get(0);
        assertEquals("Alice Applicant", dto.getJobSeekerName());
        assertEquals("Java, Spring, SQL", dto.getJobSeekerSkills());
        assertEquals(Application.ApplicationStatus.APPLIED, dto.getStatus());
    }

    @Test
    void testSearchApplicantsForJob_FilterByName() {
        List<ApplicationResponse> resultsMatch = applicationService.searchApplicantsForJob(
                10L, "alice", null, null, null, null, null, employer);
        assertEquals(1, resultsMatch.size());

        List<ApplicationResponse> resultsNoMatch = applicationService.searchApplicantsForJob(
                10L, "bob", null, null, null, null, null, employer);
        assertEquals(0, resultsNoMatch.size());
    }

    @Test
    void testSearchApplicantsForJob_FilterBySkill() {
        List<ApplicationResponse> resultsMatch = applicationService.searchApplicantsForJob(
                10L, null, "spring", null, null, null, null, employer);
        assertEquals(1, resultsMatch.size());

        List<ApplicationResponse> resultsNoMatch = applicationService.searchApplicantsForJob(
                10L, null, "python", null, null, null, null, employer);
        assertEquals(0, resultsNoMatch.size());
    }

    @Test
    void testSearchApplicantsForJob_FilterByExperience() {
        List<ApplicationResponse> resultsMatch = applicationService.searchApplicantsForJob(
                10L, null, null, "5 years", null, null, null, employer);
        assertEquals(1, resultsMatch.size());

        List<ApplicationResponse> resultsNoMatch = applicationService.searchApplicantsForJob(
                10L, null, null, "10 years", null, null, null, employer);
        assertEquals(0, resultsNoMatch.size());
    }

    @Test
    void testSearchApplicantsForJob_FilterByStatus() {
        List<ApplicationResponse> resultsMatch = applicationService.searchApplicantsForJob(
                10L, null, null, null, null, null, Application.ApplicationStatus.APPLIED, employer);
        assertEquals(1, resultsMatch.size());

        List<ApplicationResponse> resultsNoMatch = applicationService.searchApplicantsForJob(
                10L, null, null, null, null, null, Application.ApplicationStatus.REJECTED, employer);
        assertEquals(0, resultsNoMatch.size());
    }

    @Test
    void testSearchApplicantsForJob_FilterByAppliedAfter() {
        String oldDate = LocalDateTime.now().minusDays(5).toString() + "Z";
        String futureDate = LocalDateTime.now().plusDays(1).toString() + "Z";

        List<ApplicationResponse> resultsMatch = applicationService.searchApplicantsForJob(
                10L, null, null, null, null, oldDate, null, employer);
        assertEquals(1, resultsMatch.size());

        List<ApplicationResponse> resultsNoMatch = applicationService.searchApplicantsForJob(
                10L, null, null, null, null, futureDate, null, employer);
        assertEquals(0, resultsNoMatch.size());
    }

    @Test
    void testUpdateApplicationStatus() {
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(application));
        when(applicationRepository.save(org.mockito.ArgumentMatchers.any()))
                .thenAnswer(i -> i.getArguments()[0]);

        ApplicationResponse response = applicationService.updateApplicationStatus(100L,
                Application.ApplicationStatus.SHORTLISTED, employer);
        assertEquals(Application.ApplicationStatus.SHORTLISTED, response.getStatus());
    }

    @Test
    void testUpdateBulkStatus() {
        when(applicationRepository.findAllById(org.mockito.ArgumentMatchers.any()))
                .thenReturn(Collections.singletonList(application));
        when(applicationRepository.saveAll(org.mockito.ArgumentMatchers.any()))
                .thenAnswer(i -> i.getArguments()[0]);

        List<ApplicationResponse> response = applicationService.updateBulkStatus(List.of(100L),
                Application.ApplicationStatus.REJECTED, employer);
        assertEquals(1, response.size());
        assertEquals(Application.ApplicationStatus.REJECTED, response.get(0).getStatus());
    }

    @Test
    void testDeleteApplication() {
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(application));
        org.mockito.Mockito.doNothing().when(applicationRepository).delete(org.mockito.ArgumentMatchers.any());

        applicationService.deleteApplication(100L, seeker);

    }

    @Test
    void testGetApplicationSummary() {
        when(applicationRepository.findByJobPostId(10L)).thenReturn(Collections.singletonList(application));

        com.example.revhirehiringplatform.dto.response.ApplicationSummaryResponse summary = applicationService
                .getApplicationSummary(10L, employer);
        assertEquals(1, summary.getTotalApplications());
    }

    @Test
    void testApplyForJob() {
        when(profileRepository.findByUserId(2L)).thenReturn(Optional.of(profile));
        when(jobPostRepository.findById(10L)).thenReturn(Optional.of(jobPost));
        when(applicationRepository.findByJobSeekerId(profile.getId())).thenReturn(Collections.emptyList());
        when(applicationRepository.save(org.mockito.ArgumentMatchers.any(Application.class))).thenAnswer(i -> {
            Application app = i.getArgument(0);
            app.setId(200L);
            return app;
        });

        ApplicationResponse response = applicationService.applyForJob(10L, seeker, null, null);
        assertEquals(10L, response.getJobId());
    }

    @Test
    void testGetMyApplications() {
        when(profileRepository.findByUserId(2L)).thenReturn(Optional.of(profile));
        when(applicationRepository.findByJobSeekerId(profile.getId()))
                .thenReturn(Collections.singletonList(application));
        List<ApplicationResponse> apps = applicationService.getMyApplications(seeker);
        assertEquals(1, apps.size());
    }

    @Test
    void testGetApplicationsForEmployer() {
        when(applicationRepository.findByJobPostCreatedBy(employer))
                .thenReturn(Collections.singletonList(application));
        List<ApplicationResponse> apps = applicationService.getApplicationsForEmployer(employer);
        assertEquals(1, apps.size());
    }

    @Test
    void testGetApplicationsForJob() {
        when(jobPostRepository.findById(10L)).thenReturn(Optional.of(jobPost));
        when(applicationRepository.findByJobPostId(10L)).thenReturn(Collections.singletonList(application));
        List<ApplicationResponse> apps = applicationService.getApplicationsForJob(10L, employer);
        assertEquals(1, apps.size());
    }
}
