package com.example.revhirehiringplatform.service;

import com.example.revhirehiringplatform.dto.response.ApplicationResponse;
import com.example.revhirehiringplatform.dto.response.ApplicationSummaryResponse;
import com.example.revhirehiringplatform.model.Application;
import com.example.revhirehiringplatform.model.JobPost;
import com.example.revhirehiringplatform.model.JobSeekerProfile;
import com.example.revhirehiringplatform.model.User;
import com.example.revhirehiringplatform.repository.ApplicationRepository;
import com.example.revhirehiringplatform.repository.JobPostRepository;
import com.example.revhirehiringplatform.repository.JobSeekerProfileRepository;
import com.example.revhirehiringplatform.model.ResumeText;
import com.example.revhirehiringplatform.model.ApplicationStatusHistory;
import com.example.revhirehiringplatform.model.SeekerSkillMap;
import com.example.revhirehiringplatform.repository.ResumeTextRepository;
import com.example.revhirehiringplatform.repository.SeekerSkillMapRepository;
import com.example.revhirehiringplatform.repository.ApplicationStatusHistoryRepository;
import com.example.revhirehiringplatform.repository.ResumeFilesRepository;
import com.example.revhirehiringplatform.model.ResumeFiles;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationService {

        private final ApplicationRepository applicationRepository;
        private final JobPostRepository jobPostRepository;
        private final JobSeekerProfileRepository profileRepository;
        private final ResumeTextRepository resumeTextRepository;
        private final SeekerSkillMapRepository seekerSkillMapRepository;
        private final ApplicationStatusHistoryRepository statusHistoryRepository;
        private final ResumeFilesRepository resumeFilesRepository;
        private final NotificationService notificationService;
        private final AuditLogService auditLogService;

        @Transactional
        public ApplicationResponse applyForJob(Long jobId, User user, Long resumeFileId, String coverLetter) {
                log.info("User {} applying for job {}", user.getEmail(), jobId);
                JobSeekerProfile profile = profileRepository.findByUserId(user.getId())
                                .orElseThrow(() -> new RuntimeException("Complete your profile before applying"));

                JobPost jobPost = jobPostRepository.findById(jobId)
                                .orElseThrow(() -> new RuntimeException("Job not found"));

                if (jobPost.getDeadline() != null && LocalDate.now().isAfter(jobPost.getDeadline())) {
                        throw new RuntimeException("Registration closed");
                }

                Application existing = applicationRepository.findByJobSeekerId(profile.getId()).stream()
                                .filter(app -> app.getJobPost().getId().equals(jobId))
                                .findFirst()
                                .orElse(null);

                if (existing != null) {
                        if (existing.getStatus() == Application.ApplicationStatus.WITHDRAWN) {
                                Application.ApplicationStatus oldStatus = existing.getStatus();
                                existing.setStatus(Application.ApplicationStatus.APPLIED);
                                existing.setWithdrawReason(null);
                                Application saved = applicationRepository.save(existing);

                                ApplicationStatusHistory history = new ApplicationStatusHistory();
                                history.setApplication(saved);
                                history.setOldStatus(oldStatus.name());
                                history.setNewStatus(Application.ApplicationStatus.APPLIED.name());
                                history.setChangedBy(user);
                                history.setComment("Re-applied after withdrawal");
                                statusHistoryRepository.save(history);

                                notificationService.createNotification(user,
                                                "You have re-applied for the " + jobPost.getTitle() + " position at "
                                                                + jobPost.getCompany().getName(),
                                                true, "Re-application Received: " + jobPost.getTitle(),
                                                "You have successfully re-applied for the " + jobPost.getTitle()
                                                                + " position at " + jobPost.getCompany().getName()
                                                                + ".");

                                notificationService.createNotification(jobPost.getCreatedBy(),
                                                "Re-application received for " + jobPost.getTitle() + " from "
                                                                + user.getName(),
                                                true, "New Re-application for " + jobPost.getTitle(),
                                                "A new re-application has been received for " + jobPost.getTitle()
                                                                + " from " + user.getName() + ".");

                                auditLogService.logAction(
                                                "Application",
                                                saved.getId(),
                                                "APPLICATION_REAPPLIED",
                                                oldStatus.name(),
                                                "Job: " + jobPost.getTitle() + ", Applicant: "
                                                                + profile.getUser().getName(),
                                                user);

                                return mapToDto(saved);
                        }

                        throw new RuntimeException("You have already applied for this job");
                }

                Application application = new Application();
                application.setJobPost(jobPost);
                application.setJobSeeker(profile);
                application.setCoverLetter(coverLetter);
                application.setStatus(Application.ApplicationStatus.APPLIED);

                log.info("Saving Application: coverLetter_len={}",
                                coverLetter != null ? coverLetter.length() : "NULL");

                if (resumeFileId != null) {
                        ResumeFiles resumeFile = resumeFilesRepository.findById(resumeFileId)
                                        .orElseThrow(() -> new RuntimeException("Resume file not found"));
                        application.setResumeFile(resumeFile);
                }

                Application savedApp = applicationRepository.save(application);

                notificationService.createNotification(user,
                                "You have successfully applied for the " + jobPost.getTitle() + " position at "
                                                + jobPost.getCompany().getName(),
                                true, "Application Received: " + jobPost.getTitle(),
                                "You have successfully applied for the " + jobPost.getTitle() + " position at "
                                                + jobPost.getCompany().getName() + ".");

                notificationService.createNotification(jobPost.getCreatedBy(),
                                "New application received for " + jobPost.getTitle() + " from " + user.getName(),
                                true, "New Application for " + jobPost.getTitle(),
                                "A new application has been received for " + jobPost.getTitle() + " from "
                                                + user.getName() + ".");

                auditLogService.logAction(
                                "Application",
                                savedApp.getId(),
                                "APPLICATION_SUBMITTED",
                                null,
                                "Job: " + jobPost.getTitle() + ", Applicant: " + profile.getUser().getName(),
                                user);

                return mapToDto(savedApp);
        }

        @Transactional(readOnly = true)
        public List<ApplicationResponse> getMyApplications(User user) {
                JobSeekerProfile profile = profileRepository.findByUserId(user.getId())
                                .orElseThrow(() -> new RuntimeException("Profile not found"));
                return applicationRepository.findByJobSeekerId(profile.getId()).stream()
                                .map(this::mapToDto)
                                .toList();
        }

        @Transactional(readOnly = true)
        public List<ApplicationResponse> getApplicationsForEmployer(User employer) {
                return applicationRepository.findByJobPostCreatedBy(employer).stream()
                                .map(this::mapToDto)
                                .toList();
        }

        @Transactional(readOnly = true)
        public List<ApplicationResponse> getApplicationsForJob(Long jobId, User employer) {
                JobPost jobPost = jobPostRepository.findById(jobId)
                                .orElseThrow(() -> new RuntimeException("Job not found"));

                if (!jobPost.getCreatedBy().getId().equals(employer.getId())) {
                        throw new RuntimeException("Unauthorized to view these applications");
                }

                return applicationRepository.findByJobPostId(jobId).stream()
                                .map(this::mapToDto)
                                .toList();
        }

        @Transactional
        public ApplicationResponse updateApplicationStatus(Long applicationId, Application.ApplicationStatus status,
                        User employer) {
                Application application = applicationRepository.findById(applicationId)
                                .orElseThrow(() -> new RuntimeException("Application not found"));

                if (!application.getJobPost().getCreatedBy().getId().equals(employer.getId())) {
                        throw new RuntimeException("Unauthorized to update this application");
                }

                Application.ApplicationStatus oldStatus = application.getStatus();
                application.setStatus(status);
                Application savedApp = applicationRepository.save(application);

                ApplicationStatusHistory history = new ApplicationStatusHistory();
                history.setApplication(savedApp);
                history.setOldStatus(oldStatus != null ? oldStatus.name() : "");
                history.setNewStatus(status.name());
                history.setChangedBy(employer);
                history.setComment("Status updated by employer");
                statusHistoryRepository.save(history);

                notificationService.createNotification(
                                application.getJobSeeker().getUser(),
                                "Your application for " + application.getJobPost().getTitle() + " has been updated to "
                                                + status,
                                true, "Application Status Update: " + application.getJobPost().getTitle(),
                                "Your application status for " + application.getJobPost().getTitle()
                                                + " has been updated to: " + status + ".");

                return mapToDto(savedApp);
        }

        @Transactional
        public List<ApplicationResponse> updateBulkStatus(List<Long> applicationIds,
                        Application.ApplicationStatus status,
                        User employer) {
                List<Application> applications = applicationRepository.findAllById(applicationIds);

                for (Application app : applications) {
                        if (!app.getJobPost().getCreatedBy().getId().equals(employer.getId())) {
                                throw new RuntimeException("Unauthorized to update application " + app.getId());
                        }
                        Application.ApplicationStatus oldStatus = app.getStatus();
                        app.setStatus(status);

                        Application savedApp = applicationRepository.save(app);

                        ApplicationStatusHistory history = new ApplicationStatusHistory();
                        history.setApplication(savedApp);
                        history.setOldStatus(oldStatus != null ? oldStatus.name() : "");
                        history.setNewStatus(status.name());
                        history.setChangedBy(employer);
                        history.setComment("Bulk status updated by employer");
                        statusHistoryRepository.save(history);

                        notificationService.createNotification(
                                        app.getJobSeeker().getUser(),
                                        "Your application for " + app.getJobPost().getTitle() + " has been updated to "
                                                        + status,
                                        true, "Application Status Update: " + app.getJobPost().getTitle(),
                                        "Your application status for " + app.getJobPost().getTitle()
                                                        + " has been updated to: " + status + ".");
                }

                return applications.stream().map(this::mapToDto).toList();
        }

        @Transactional(readOnly = true)
        public List<ApplicationResponse> searchApplicantsForJob(Long jobId, String name, String skill,
                        String experience, String education, String appliedAfter,
                        Application.ApplicationStatus status, User employer) {
                JobPost jobPost = jobPostRepository.findById(jobId)
                                .orElseThrow(() -> new RuntimeException("Job not found"));

                if (!jobPost.getCreatedBy().getId().equals(employer.getId())) {
                        throw new RuntimeException("Unauthorized to view these applications");
                }

                List<Application> applications = applicationRepository.findByJobPostId(jobId);

                if (name != null && !name.trim().isEmpty()) {
                        applications = applications.stream()
                                        .filter(app -> app.getJobSeeker().getUser().getName().toLowerCase()
                                                        .contains(name.toLowerCase()))
                                        .toList();
                }

                if (status != null) {
                        applications = applications.stream()
                                        .filter(app -> app.getStatus() == status)
                                        .toList();
                }

                if (appliedAfter != null && !appliedAfter.trim().isEmpty()) {
                        LocalDateTime afterDate = LocalDateTime.parse(appliedAfter, DateTimeFormatter.ISO_DATE_TIME);
                        applications = applications.stream()
                                        .filter(app -> app.getAppliedAt() != null
                                                        && app.getAppliedAt().isAfter(afterDate))
                                        .toList();
                }

                List<ApplicationResponse> applicationDtos = applications.stream()
                                .map(this::mapToDto)
                                .toList();

                if (skill != null && !skill.trim().isEmpty()) {
                        applicationDtos = applicationDtos.stream()
                                        .filter(dto -> dto.getJobSeekerSkills() != null
                                                        && dto.getJobSeekerSkills().toLowerCase()
                                                                        .contains(skill.toLowerCase()))
                                        .toList();
                }

                if (experience != null && !experience.trim().isEmpty()) {
                        applicationDtos = applicationDtos.stream()
                                        .filter(dto -> dto.getJobSeekerExperience() != null
                                                        && dto.getJobSeekerExperience().toLowerCase()
                                                                        .contains(experience.toLowerCase()))
                                        .toList();
                }

                if (education != null && !education.trim().isEmpty()) {
                        applicationDtos = applicationDtos.stream()
                                        .filter(dto -> dto.getJobSeekerEducation() != null
                                                        && dto.getJobSeekerEducation().toLowerCase()
                                                                        .contains(education.toLowerCase()))
                                        .toList();
                }

                return applicationDtos;
        }

        @Transactional
        public void deleteApplication(Long applicationId, User user) {
                Application application = applicationRepository.findById(applicationId)
                                .orElseThrow(() -> new RuntimeException("Application not found"));

                boolean isSeeker = user.getRole() == User.Role.JOB_SEEKER
                                && application.getJobSeeker().getUser().getId().equals(user.getId());
                boolean isEmployer = user.getRole() == User.Role.EMPLOYER
                                && application.getJobPost().getCreatedBy().getId().equals(user.getId());

                if (!isSeeker && !isEmployer) {
                        throw new IllegalStateException("Unauthorized to delete this application");
                }

                applicationRepository.delete(application);
                auditLogService.logAction("Application", applicationId, "APPLICATION_DELETED", null, null, user);
        }

        @Transactional(readOnly = true)
        public ApplicationSummaryResponse getApplicationSummary(Long jobId, User employer) {
                JobPost jobPost = jobPostRepository.findById(jobId)
                                .orElseThrow(() -> new RuntimeException("Job not found"));

                if (!jobPost.getCreatedBy().getId().equals(employer.getId())) {
                        throw new IllegalStateException("Unauthorized to view this summary");
                }

                List<Application> applications = applicationRepository.findByJobPostId(jobId);

                Map<String, Long> statusCounts = applications.stream()
                                .collect(Collectors.groupingBy(app -> app.getStatus().name(), Collectors.counting()));

                return ApplicationSummaryResponse.builder()
                                .jobId(jobId)
                                .jobTitle(jobPost.getTitle())
                                .totalApplications((long) applications.size())
                                .statusCounts(statusCounts)
                                .build();
        }

        public Resource downloadResume(Long applicationId, User employer) throws Exception {
                Application application = applicationRepository.findById(applicationId)
                                .orElseThrow(() -> new RuntimeException("Application not found"));

                if (!application.getJobPost().getCreatedBy().getId().equals(employer.getId())) {
                        throw new RuntimeException("Unauthorized to download resume for this application");
                }

                ResumeFiles resumeFile = application.getResumeFile();
                if (resumeFile == null) {
                        // Fallback to profile resume
                        List<ResumeFiles> resumes = resumeFilesRepository
                                        .findByJobSeekerId(application.getJobSeeker().getId());
                        if (resumes != null && !resumes.isEmpty()) {
                                resumeFile = resumes.get(0);
                        }
                }

                if (resumeFile == null) {
                        throw new RuntimeException("No resume file found for this applicant.");
                }

                Path filePath = Paths.get("uploads/resumes").resolve(resumeFile.getFilePath()).normalize();
                Resource resource = new UrlResource(filePath.toUri());

                if (!resource.exists() || !resource.isReadable()) {
                        throw new RuntimeException("Resume file not found on server.");
                }

                return resource;
        }

        private ApplicationResponse mapToDto(Application app) {
                ApplicationResponse dto = new ApplicationResponse();
                dto.setId(app.getId());
                dto.setJobId(app.getJobPost().getId());
                dto.setJobTitle(app.getJobPost().getTitle());
                dto.setCompanyName(app.getJobPost().getCompany().getName());
                dto.setJobSeekerId(app.getJobSeeker().getId());
                dto.setJobSeekerName(app.getJobSeeker().getUser().getName());
                dto.setJobSeekerEmail(app.getJobSeeker().getUser().getEmail());

                List<SeekerSkillMap> skills = seekerSkillMapRepository.findByJobSeekerId(app.getJobSeeker().getId());
                if (!skills.isEmpty()) {
                        dto.setJobSeekerSkills(skills.stream()
                                        .map(s -> s.getSkill().getSkillName())
                                        .collect(Collectors.joining(", ")));
                }

                ResumeText resumeText = resumeTextRepository.findByJobSeekerId(app.getJobSeeker().getId()).orElse(null);
                if (resumeText != null) {
                        if (dto.getJobSeekerSkills() == null) {
                                dto.setJobSeekerSkills(resumeText.getSkillsText());
                        }
                        dto.setJobSeekerExperience(resumeText.getExperienceText());
                        dto.setJobSeekerEducation(resumeText.getEducationText());
                }

                dto.setStatus(app.getStatus());
                dto.setCoverLetter(app.getCoverLetter());
                dto.setAppliedAt(app.getAppliedAt());

                // Map Image Fields
                if (app.getJobSeeker() != null) {
                        dto.setJobSeekerProfileImage(app.getJobSeeker().getProfileImage());
                }
                if (app.getJobPost() != null && app.getJobPost().getCompany() != null) {
                        dto.setCompanyLogo(app.getJobPost().getCompany().getLogo());
                }

                return dto;
        }
}