package com.example.revhirehiringplatform.service;

import com.example.revhirehiringplatform.dto.request.JobPostRequest;
import com.example.revhirehiringplatform.dto.response.JobPostResponse;
import com.example.revhirehiringplatform.model.*;
import com.example.revhirehiringplatform.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JobServiceTest {

    @Mock
    private JobPostRepository jobPostRepository;
    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private EmployerProfileRepository employerProfileRepository;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private SkillsMasterRepository skillsMasterRepository;
    @Mock
    private JobSkillMapRepository jobSkillMapRepository;
    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private SavedJobsRepository savedJobsRepository;

    @InjectMocks
    private JobService jobService;

    private User employer;
    private Company company;
    private JobPost jobPost;
    private JobPostRequest jobRequest;

    @BeforeEach
    void setUp() {
        employer = new User();
        employer.setId(1L);
        employer.setEmail("employer@test.com");
        employer.setRole(User.Role.EMPLOYER);

        company = new Company();
        company.setId(10L);
        company.setName("Test Company");
        company.setCreatedBy(employer);

        jobPost = new JobPost();
        jobPost.setId(100L);
        jobPost.setTitle("Software Engineer");
        jobPost.setCompany(company);
        jobPost.setCreatedBy(employer);
        jobPost.setStatus(JobPost.JobStatus.ACTIVE);

        jobRequest = new JobPostRequest();
        jobRequest.setTitle("Software Engineer");
        jobRequest.setDescription("Java developer");
        jobRequest.setCompanyId(10L);
        jobRequest.setDeadline(LocalDate.now().plusDays(10));
    }

    @Test
    void testCreateJob_Success() {
        when(companyRepository.findById(10L)).thenReturn(Optional.of(company));
        when(jobPostRepository.save(any(JobPost.class))).thenReturn(jobPost);

        JobPostResponse response = jobService.createJob(jobRequest, employer);

        assertNotNull(response);
        assertEquals("Software Engineer", response.getTitle());
        verify(jobPostRepository, times(1)).save(any(JobPost.class));
        verify(auditLogService, times(1)).logAction(any(), any(), any(), any(), any(), any());
    }

    @Test
    void testCreateJob_CompanyNotFound() {
        when(companyRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> jobService.createJob(jobRequest, employer));
    }

    @Test
    void testUpdateJob_Success() {
        when(jobPostRepository.findById(100L)).thenReturn(Optional.of(jobPost));
        when(jobPostRepository.save(any(JobPost.class))).thenReturn(jobPost);

        JobPostResponse response = jobService.updateJob(100L, jobRequest, employer);

        assertNotNull(response);
        verify(jobPostRepository, times(1)).save(any(JobPost.class));
    }

    @Test
    void testUpdateJob_Unauthorized() {
        User otherEmployer = new User();
        otherEmployer.setId(2L);
        when(jobPostRepository.findById(100L)).thenReturn(Optional.of(jobPost));

        assertThrows(RuntimeException.class, () -> jobService.updateJob(100L, jobRequest, otherEmployer));
    }

    @Test
    void testUpdateJobStatus_Success() {
        when(jobPostRepository.findById(100L)).thenReturn(Optional.of(jobPost));
        when(jobPostRepository.save(any(JobPost.class))).thenReturn(jobPost);

        JobPostResponse response = jobService.updateJobStatus(100L, JobPost.JobStatus.CLOSED, employer);

        assertNotNull(response);
        verify(jobPostRepository, times(1)).save(any(JobPost.class));
    }

    @Test
    void testDeleteJob_Success() {
        when(jobPostRepository.findById(100L)).thenReturn(Optional.of(jobPost));
        when(savedJobsRepository.findByJobPostId(100L)).thenReturn(java.util.Collections.emptyList());

        jobService.deleteJob(100L, employer);

        verify(jobPostRepository, times(1)).delete(jobPost);
    }

    @Test
    void testGetMyJobs() {
        when(jobPostRepository.findByCreatedBy(employer)).thenReturn(java.util.Collections.singletonList(jobPost));
        java.util.List<JobPostResponse> list = jobService.getMyJobs(employer);
        assertEquals(1, list.size());
    }

    @Test
    void testGetJobById() {
        when(jobPostRepository.findById(100L)).thenReturn(Optional.of(jobPost));
        JobPost result = jobService.getJobById(100L);
        assertNotNull(result);
    }

    @Test
    void testGetJobApplications() {
        when(jobPostRepository.findById(100L)).thenReturn(Optional.of(jobPost));
        when(applicationRepository.findByJobPostId(100L)).thenReturn(java.util.Collections.emptyList());

        java.util.List<com.example.revhirehiringplatform.dto.response.ApplicationResponse> apps = jobService.getJobApplications(100L,
                employer);
        assertEquals(0, apps.size());
    }

    @Test
    void testGetJobSkills() {
        when(jobSkillMapRepository.findByJobPostId(100L)).thenReturn(java.util.Collections.emptyList());
        java.util.List<com.example.revhirehiringplatform.dto.response.SkillResponse> skills = jobService.getJobSkills(100L);
        assertEquals(0, skills.size());
    }

    @Test
    void testGetRecommendedJobs() {
        JobPost activeJob = new JobPost();
        activeJob.setId(200L);
        activeJob.setTitle("Active Job");
        activeJob.setStatus(JobPost.JobStatus.ACTIVE);
        activeJob.setCompany(company);
        activeJob.setCreatedBy(employer);

        JobPost inactiveJob = new JobPost();
        inactiveJob.setId(201L);
        inactiveJob.setTitle("Inactive Job");
        inactiveJob.setStatus(JobPost.JobStatus.CLOSED);
        inactiveJob.setCompany(company);
        inactiveJob.setCreatedBy(employer);

        when(jobPostRepository.findAll()).thenReturn(java.util.Arrays.asList(activeJob, inactiveJob));

        java.util.List<JobPostResponse> recommendations = jobService.getRecommendedJobs(new User());

        assertEquals(1, recommendations.size());
        assertEquals("Active Job", recommendations.get(0).getTitle());
    }

    @Test
    void testCreateJob_WithRequirements() {
        jobRequest.setSkills("Java, Spring Boot");
        when(companyRepository.findById(10L)).thenReturn(Optional.of(company));
        when(jobPostRepository.save(any(JobPost.class))).thenReturn(jobPost);

        SkillsMaster javaSkill = new SkillsMaster();
        javaSkill.setId(1L);
        javaSkill.setSkillName("Java");

        when(skillsMasterRepository.findBySkillNameIgnoreCase("Java")).thenReturn(Optional.of(javaSkill));
        when(skillsMasterRepository.findBySkillNameIgnoreCase("Spring Boot")).thenReturn(Optional.empty());
        when(skillsMasterRepository.save(any(SkillsMaster.class))).thenAnswer(i -> i.getArguments()[0]);

        JobPostResponse response = jobService.createJob(jobRequest, employer);

        assertNotNull(response);
        assertEquals("Software Engineer", response.getTitle());
        verify(jobSkillMapRepository, times(2)).save(any(JobSkillMap.class));
    }

    @Test
    void testCreateJob_Unauthorized() {
        User otherEmployer = new User();
        otherEmployer.setId(2L);

        when(companyRepository.findById(10L)).thenReturn(Optional.of(company));
        when(employerProfileRepository.findByUserId(2L)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> jobService.createJob(jobRequest, otherEmployer));
    }

    @Test
    void testUpdateJob_WithRequirements() {
        jobRequest.setSkills("Java");
        when(jobPostRepository.findById(100L)).thenReturn(Optional.of(jobPost));
        when(jobPostRepository.save(any(JobPost.class))).thenReturn(jobPost);

        SkillsMaster javaSkill = new SkillsMaster();
        javaSkill.setId(1L);
        javaSkill.setSkillName("Java");

        when(jobSkillMapRepository.findByJobPostId(100L)).thenReturn(java.util.Collections.emptyList());
        when(skillsMasterRepository.findBySkillNameIgnoreCase("Java")).thenReturn(Optional.of(javaSkill));

        JobPostResponse response = jobService.updateJob(100L, jobRequest, employer);

        assertNotNull(response);
        verify(jobSkillMapRepository, times(1)).deleteAll(any());
        verify(jobSkillMapRepository, times(1)).save(any(JobSkillMap.class));
    }

    @Test
    void testGetAllJobs() {
        when(jobPostRepository.findAll()).thenReturn(java.util.Collections.singletonList(jobPost));
        java.util.List<JobPostResponse> allJobs = jobService.getAllJobs();
        assertEquals(1, allJobs.size());
    }

    @Test
    void testCreateJob_NoCompanyLinked() {
        jobRequest.setCompanyId(null);
        when(employerProfileRepository.findByUserId(1L)).thenReturn(Optional.empty());

        Exception e = assertThrows(IllegalArgumentException.class, () -> jobService.createJob(jobRequest, employer));
        assertEquals("No company linked to your profile. Please complete your employer profile first.", e.getMessage());
    }

    @Test
    void testUpdateJob_ChangeStatus() {
        jobRequest.setStatus("CLOSED");
        when(jobPostRepository.findById(100L)).thenReturn(Optional.of(jobPost));
        when(jobPostRepository.save(any(JobPost.class))).thenReturn(jobPost);

        JobPostResponse response = jobService.updateJob(100L, jobRequest, employer);
        assertNotNull(response);
    }

    @Test
    void testUpdateJob_InvalidStatus() {
        jobRequest.setStatus("INVALID_STATUS");
        jobRequest.setExperienceYears(5);
        jobRequest.setEducation("B.Tech");
        jobRequest.setOpenings(3);
        jobRequest.setDeadline(LocalDate.now());

        when(jobPostRepository.findById(100L)).thenReturn(Optional.of(jobPost));
        when(jobPostRepository.save(any(JobPost.class))).thenReturn(jobPost);

        assertDoesNotThrow(() -> jobService.updateJob(100L, jobRequest, employer));
    }

    @Test
    void testUpdateJobStatus_Unauthorized() {
        User otherUser = new User();
        otherUser.setId(2L);
        when(jobPostRepository.findById(100L)).thenReturn(Optional.of(jobPost));

        assertThrows(IllegalStateException.class,
                () -> jobService.updateJobStatus(100L, JobPost.JobStatus.CLOSED, otherUser));
    }

    @Test
    void testDeleteJob_Unauthorized() {
        User otherUser = new User();
        otherUser.setId(2L);
        when(jobPostRepository.findById(100L)).thenReturn(Optional.of(jobPost));

        assertThrows(IllegalStateException.class, () -> jobService.deleteJob(100L, otherUser));
    }

    @Test
    void testGetJobApplications_Unauthorized() {
        User otherUser = new User();
        otherUser.setId(2L);
        when(jobPostRepository.findById(100L)).thenReturn(Optional.of(jobPost));

        assertThrows(IllegalStateException.class, () -> jobService.getJobApplications(100L, otherUser));
    }

    @Test
    void testParseSalary_Empty() {
        jobRequest.setSalary("");
        when(companyRepository.findById(10L)).thenReturn(Optional.of(company));
        when(jobPostRepository.save(any(JobPost.class))).thenReturn(jobPost);
        jobService.createJob(jobRequest, employer);
    }

    @Test
    void testParseSalary_Null() {
        jobRequest.setSalary(null);
        when(companyRepository.findById(10L)).thenReturn(Optional.of(company));
        when(jobPostRepository.save(any(JobPost.class))).thenReturn(jobPost);
        jobService.createJob(jobRequest, employer);
    }

    @Test
    void testParseSalary_InvalidFormat() {
        jobRequest.setSalary("invalid-format");
        when(companyRepository.findById(10L)).thenReturn(Optional.of(company));
        when(jobPostRepository.save(any(JobPost.class))).thenReturn(jobPost);
        jobService.createJob(jobRequest, employer);
    }

    @Test
    void testCreateJob_WithEmployerProfile() {
        EmployerProfile profile = new EmployerProfile();
        profile.setCompany(company);
        when(employerProfileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(jobPostRepository.save(any(JobPost.class))).thenReturn(jobPost);
        JobPostResponse response = jobService.createJob(jobRequest, employer);
        assertNotNull(response);
    }
}
