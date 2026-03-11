package com.example.revhirehiringplatform.service;

import com.example.revhirehiringplatform.dto.request.JobPostRequest;
import com.example.revhirehiringplatform.dto.response.ApplicationResponse;
import com.example.revhirehiringplatform.dto.response.JobPostResponse;
import com.example.revhirehiringplatform.dto.response.SkillResponse;
import com.example.revhirehiringplatform.model.Company;
import com.example.revhirehiringplatform.model.EmployerProfile;
import com.example.revhirehiringplatform.model.JobPost;
import com.example.revhirehiringplatform.model.JobSkillMap;
import com.example.revhirehiringplatform.model.SkillsMaster;
import com.example.revhirehiringplatform.model.User;

import com.example.revhirehiringplatform.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobService {

    private static final String JOB_NOT_FOUND = "Job not found";
    private static final String JOB_POST_STR = "JobPost";

    private final JobPostRepository jobPostRepository;
    private final CompanyRepository companyRepository;
    private final EmployerProfileRepository employerProfileRepository;
    private final AuditLogService auditLogService;
    private final SkillsMasterRepository skillsMasterRepository;
    private final JobSkillMapRepository jobSkillMapRepository;
    private final ApplicationRepository applicationRepository;
    private final SavedJobsRepository savedJobsRepository;
    private final ApplicationStatusHistoryRepository applicationStatusHistoryRepository;
    private final ApplicationNotesRepository applicationNotesRepository;

    @Transactional
    public JobPostResponse createJob(JobPostRequest jobPostDto, User user) {
        log.info("Creating job: {} for user: {}", jobPostDto.getTitle(), user.getEmail());

        EmployerProfile profile = employerProfileRepository.findByUserId(user.getId())
                .orElse(null);

        Company company;
        if (profile != null && profile.getCompany() != null) {
            company = profile.getCompany();
        } else if (jobPostDto.getCompanyId() != null) {
            company = companyRepository.findById(jobPostDto.getCompanyId())
                    .orElseThrow(() -> new IllegalArgumentException("Company not found"));
            if (company.getCreatedBy() != null && !company.getCreatedBy().getId().equals(user.getId())) {
                throw new IllegalStateException("Unauthorized to post for this company");
            }
        } else {
            throw new IllegalArgumentException(
                    "No company linked to your profile. Please complete your employer profile first.");
        }

        JobPost jobPost = new JobPost();
        jobPost.setTitle(jobPostDto.getTitle());
        jobPost.setDescription(jobPostDto.getDescription());
        jobPost.setLocation(jobPostDto.getLocation());
        jobPost.setSalaryMin(parseSalary(jobPostDto.getSalary(), true));
        jobPost.setSalaryMax(parseSalary(jobPostDto.getSalary(), false));
        jobPost.setJobType(jobPostDto.getJobType());
        jobPost.setDeadline(jobPostDto.getDeadline() != null ? jobPostDto.getDeadline() : LocalDate.now().plusDays(30));
        jobPost.setExperienceYears(jobPostDto.getExperienceYears());
        jobPost.setEducation(jobPostDto.getEducation());
        jobPost.setOpenings(jobPostDto.getOpenings() != null ? jobPostDto.getOpenings() : 1);
        jobPost.setCompany(company);
        jobPost.setCreatedBy(user);
        jobPost.setResponsibilities(jobPostDto.getResponsibilities());
        jobPost.setRequirements(jobPostDto.getRequirements());
        jobPost.setStatus(JobPost.JobStatus.ACTIVE);

        log.info("Saving JobPost: id={}, responsibilities_len={}, requirements_len={}",
                jobPost.getId(),
                jobPost.getResponsibilities() != null ? jobPost.getResponsibilities().length() : "NULL",
                jobPost.getRequirements() != null ? jobPost.getRequirements().length() : "NULL");

        JobPost savedJob = jobPostRepository.save(jobPost);

        if (jobPostDto.getSkills() != null && !jobPostDto.getSkills().trim().isEmpty()) {
            List<String> skillNames = Arrays.stream(jobPostDto.getSkills().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
            for (String skillName : skillNames) {
                SkillsMaster skillMaster = skillsMasterRepository.findBySkillNameIgnoreCase(skillName)
                        .orElseGet(() -> {
                            SkillsMaster master = new SkillsMaster();
                            master.setSkillName(skillName);
                            return skillsMasterRepository.save(master);
                        });
                JobSkillMap skillMap = new JobSkillMap();
                skillMap.setJobPost(savedJob);
                skillMap.setSkill(skillMaster);
                skillMap.setMandatory(true);
                jobSkillMapRepository.save(skillMap);
            }
        }

        auditLogService.logAction(
                JOB_POST_STR,
                savedJob.getId(),
                "JOB_CREATED",
                null,
                "Title: " + savedJob.getTitle(),
                user);

        return mapToDto(savedJob);
    }

    private Double parseSalary(String salary, boolean isMin) {
        if (salary == null || salary.isEmpty())
            return 0.0;
        try {
            String[] parts = salary.split("-");
            if (parts.length > 0) {
                String targetPart = parts[isMin ? 0 : 1].replaceAll("[^0-9.]", "");
                if (targetPart.isEmpty()) {
                    return 0.0;
                }
                return Double.parseDouble(targetPart);
            }
            return 0.0;
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public List<JobPostResponse> getAllJobs() {
        return jobPostRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public JobPost getJobById(Long id) {
        return jobPostRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(JOB_NOT_FOUND));
    }

    public List<JobPostResponse> getMyJobs(User user) {
        return jobPostRepository.findByCreatedBy(user).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<JobPostResponse> getRecommendedJobs(User user) {
        log.info("Getting recommendations for user: {}", user.getEmail());
        return jobPostRepository.findAll().stream()
                .filter(j -> j.getStatus() == JobPost.JobStatus.ACTIVE)
                .limit(10)
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public JobPostResponse updateJob(Long id, JobPostRequest jobDto, User user) {
        JobPost job = jobPostRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(JOB_NOT_FOUND));

        if (!job.getCreatedBy().getId().equals(user.getId())) {
            throw new IllegalStateException("Unauthorized: You can only edit jobs you posted.");
        }

        job.setTitle(jobDto.getTitle());
        job.setDescription(jobDto.getDescription());
        job.setLocation(jobDto.getLocation());
        job.setSalaryMin(parseSalary(jobDto.getSalary(), true));
        job.setSalaryMax(parseSalary(jobDto.getSalary(), false));
        job.setJobType(jobDto.getJobType());
        if (jobDto.getDeadline() != null)
            job.setDeadline(jobDto.getDeadline());
        if (jobDto.getExperienceYears() != null)
            job.setExperienceYears(jobDto.getExperienceYears());
        if (jobDto.getEducation() != null)
            job.setEducation(jobDto.getEducation());
        if (jobDto.getOpenings() != null)
            job.setOpenings(jobDto.getOpenings());
        job.setResponsibilities(jobDto.getResponsibilities());
        job.setRequirements(jobDto.getRequirements());

        log.info("Updating JobPost: id={}, responsibilities_len={}, requirements_len={}",
                job.getId(),
                job.getResponsibilities() != null ? job.getResponsibilities().length() : "NULL",
                job.getRequirements() != null ? job.getRequirements().length() : "NULL");
        if (jobDto.getStatus() != null) {
            try {
                job.setStatus(JobPost.JobStatus.valueOf(jobDto.getStatus()));
            } catch (IllegalArgumentException e) {
            }
        }

        JobPost updatedJob = jobPostRepository.save(job);

        if (jobDto.getSkills() != null && !jobDto.getSkills().trim().isEmpty()) {
            List<String> skillNames = Arrays.stream(jobDto.getSkills().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();

            List<JobSkillMap> existingMaps = jobSkillMapRepository.findByJobPostId(updatedJob.getId());
            jobSkillMapRepository.deleteAll(existingMaps);

            for (String skillName : skillNames) {
                SkillsMaster skillMaster = skillsMasterRepository.findBySkillNameIgnoreCase(skillName)
                        .orElseGet(() -> {
                            SkillsMaster master = new SkillsMaster();
                            master.setSkillName(skillName);
                            return skillsMasterRepository.save(master);
                        });
                JobSkillMap skillMap = new JobSkillMap();
                skillMap.setJobPost(updatedJob);
                skillMap.setSkill(skillMaster);
                skillMap.setMandatory(true);
                jobSkillMapRepository.save(skillMap);
            }
        }

        auditLogService.logAction(
                JOB_POST_STR,
                updatedJob.getId(),
                "JOB_UPDATED",
                "Old Title: " + job.getTitle() + ", Old Type: " + job.getJobType(),
                "New Title: " + updatedJob.getTitle() + ", New Type: " + updatedJob.getJobType(),
                user);

        return mapToDto(updatedJob);
    }

    public JobPostResponse updateJobStatus(Long id, JobPost.JobStatus status, User user) {
        JobPost job = jobPostRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(JOB_NOT_FOUND));

        if (!job.getCreatedBy().getId().equals(user.getId())) {
            throw new IllegalStateException("Unauthorized: You can only edit jobs you posted.");
        }

        job.setStatus(status);
        JobPost updatedJob = jobPostRepository.save(job);

        auditLogService.logAction(
                JOB_POST_STR,
                updatedJob.getId(),
                "JOB_STATUS_UPDATED",
                "Old Status: " + job.getStatus(),
                "New Status: " + updatedJob.getStatus(),
                user);

        return mapToDto(updatedJob);
    }

    public void deleteJob(Long id, User user) {
        JobPost job = jobPostRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(JOB_NOT_FOUND));

        if (!job.getCreatedBy().getId().equals(user.getId())) {
            throw new IllegalStateException("Unauthorized: You can only delete jobs you posted.");
        }

        List<com.example.revhirehiringplatform.model.JobSkillMap> skills = jobSkillMapRepository.findByJobPostId(id);
        jobSkillMapRepository.deleteAll(skills);

        List<com.example.revhirehiringplatform.model.SavedJobs> savedJobs = savedJobsRepository.findByJobPostId(id);
        savedJobsRepository.deleteAll(savedJobs);

        List<com.example.revhirehiringplatform.model.Application> applications = applicationRepository
                .findByJobPostId(id);
        for (com.example.revhirehiringplatform.model.Application app : applications) {
            List<com.example.revhirehiringplatform.model.ApplicationStatusHistory> histories = applicationStatusHistoryRepository
                    .findByApplicationIdOrderByChangedAtDesc(app.getId());
            applicationStatusHistoryRepository.deleteAll(histories);

            List<com.example.revhirehiringplatform.model.ApplicationNotes> notes = applicationNotesRepository
                    .findByApplicationId(app.getId());
            applicationNotesRepository.deleteAll(notes);
        }
        applicationRepository.deleteAll(applications);

        auditLogService.logAction(
                JOB_POST_STR,
                job.getId(),
                "JOB_DELETED",
                "Title: " + job.getTitle(),
                null,
                user);

        jobPostRepository.delete(job);
    }

    public List<ApplicationResponse> getJobApplications(Long id, User user) {
        JobPost job = jobPostRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(JOB_NOT_FOUND));

        if (!job.getCreatedBy().getId().equals(user.getId())) {
            throw new IllegalStateException("Unauthorized to view applications for this job");
        }

        return applicationRepository.findByJobPostId(id).stream()
                .map(this::mapApplicationToDto)
                .toList();
    }

    public List<SkillResponse> getJobSkills(Long id) {
        return jobSkillMapRepository.findByJobPostId(id).stream()
                .map(map -> {
                    SkillResponse res = new SkillResponse();
                    res.setId(map.getSkill().getId());
                    res.setName(map.getSkill().getSkillName());
                    return res;
                })
                .toList();
    }

    private ApplicationResponse mapApplicationToDto(com.example.revhirehiringplatform.model.Application app) {
        ApplicationResponse dto = new ApplicationResponse();
        dto.setId(app.getId());
        dto.setJobId(app.getJobPost().getId());
        dto.setJobTitle(app.getJobPost().getTitle());
        dto.setCompanyName(app.getJobPost().getCompany().getName());
        dto.setJobSeekerId(app.getJobSeeker().getId());
        dto.setJobSeekerName(app.getJobSeeker().getUser().getName());
        dto.setJobSeekerEmail(app.getJobSeeker().getUser().getEmail());
        dto.setStatus(app.getStatus());
        dto.setAppliedAt(app.getAppliedAt());
        return dto;
    }

    public JobPostResponse mapToDto(JobPost jobPost) {
        JobPostResponse dto = new JobPostResponse();
        dto.setId(jobPost.getId());
        dto.setTitle(jobPost.getTitle());
        dto.setResponsibilities(jobPost.getResponsibilities());
        dto.setRequirements(jobPost.getRequirements());

        List<JobSkillMap> skillMaps = jobSkillMapRepository.findByJobPostId(jobPost.getId());
        if (!skillMaps.isEmpty()) {
            dto.setSkills(
                    skillMaps.stream().map(s -> s.getSkill().getSkillName()).collect(Collectors.joining(", ")));
        }

        dto.setLocation(jobPost.getLocation());
        dto.setSalary(jobPost.getSalaryMin() + " - " + jobPost.getSalaryMax());
        dto.setJobType(jobPost.getJobType());
        dto.setDeadline(jobPost.getDeadline());
        dto.setExperienceYears(jobPost.getExperienceYears());
        dto.setEducation(jobPost.getEducation());
        dto.setOpenings(jobPost.getOpenings());
        dto.setStatus(jobPost.getStatus() != null ? jobPost.getStatus().name() : null);
        dto.setPostedDate(jobPost.getCreatedAt() != null ? jobPost.getCreatedAt().toLocalDate() : LocalDate.now());
        dto.setCompanyId(jobPost.getCompany().getId());
        dto.setCompanyName(jobPost.getCompany().getName());
        dto.setCompanyLogo(jobPost.getCompany().getLogo());
        dto.setApplicantCount(applicationRepository.countByJobPostId(jobPost.getId()));
        return dto;
    }
}
