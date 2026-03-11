package com.example.revhirehiringplatform.service;

import com.example.revhirehiringplatform.dto.response.JobSeekerProfileResponse;
import com.example.revhirehiringplatform.model.JobSeekerProfile;
import com.example.revhirehiringplatform.model.ResumeText;
import com.example.revhirehiringplatform.model.SavedResume;
import com.example.revhirehiringplatform.model.User;
import com.example.revhirehiringplatform.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SavedResumeService {

    private final SavedResumeRepository savedResumeRepository;
    private final JobSeekerProfileRepository profileRepository;
    private final ResumeTextRepository resumeTextRepository;
    private final NotificationService notificationService;
    private final ApplicationRepository applicationRepository;
    private final JobPostRepository jobPostRepository;

    @Transactional
    public void saveResume(Long seekerId, Long jobId, User employer) {
        log.info("Employer {} saving resume for job seeker {} and job {}", employer.getEmail(), seekerId, jobId);

        JobSeekerProfile profile = profileRepository.findById(seekerId)
                .orElseThrow(() -> new RuntimeException("Job Seeker profile not found"));

        com.example.revhirehiringplatform.model.JobPost jobPost = jobPostRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        if (jobPost.getCreatedBy() == null || !jobPost.getCreatedBy().getId().equals(employer.getId())) {
            log.error("Save resume UNAUTHORIZED: JobPost createdBy={}, Employer={}",
                    jobPost.getCreatedBy() != null ? jobPost.getCreatedBy().getId() : "NULL",
                    employer.getId());
            throw new RuntimeException("Unauthorized to save resume for this job");
        }

        if (savedResumeRepository.existsByEmployerIdAndJobSeekerIdAndJobPostId(employer.getId(), profile.getId(),
                jobId)) {
            log.warn("Resume already saved: employerId={}, profileId={}, jobId={}", employer.getId(), profile.getId(),
                    jobId);
            throw new RuntimeException("Resume already saved by this employer");
        }

        SavedResume savedResume = new SavedResume();
        savedResume.setEmployer(employer);
        savedResume.setJobSeeker(profile);
        savedResume.setJobPost(jobPost);
        savedResumeRepository.save(savedResume);

        notificationService.createNotification(
                profile.getUser(),
                "An employer has favorited your profile!");
    }

    @Transactional
    public void unsaveResume(Long seekerId, Long jobId, User employer) {
        log.info("Employer {} unsaving resume for job seeker {} and job {}", employer.getEmail(), seekerId, jobId);

        JobSeekerProfile profile = profileRepository.findById(seekerId)
                .orElseThrow(() -> new RuntimeException("Job Seeker profile not found"));

        log.info("Attempting to find SavedResume for employerId={}, profileId={}, jobId={}", employer.getId(),
                profile.getId(), jobId);
        SavedResume savedResume = savedResumeRepository
                .findByEmployerIdAndJobSeekerIdAndJobPostId(employer.getId(), profile.getId(), jobId)
                .orElseThrow(() -> {
                    log.error("Saved resume NOT FOUND for employerId={}, profileId={}, jobId={}", employer.getId(),
                            profile.getId(), jobId);
                    return new RuntimeException("Saved resume not found");
                });

        savedResumeRepository.delete(savedResume);
    }

    @Transactional(readOnly = true)
    public List<JobSeekerProfileResponse> getSavedResumes(User employer) {
        log.info("Fetching saved resumes for employer {}", employer.getEmail());

        return savedResumeRepository.findByEmployerId(employer.getId()).stream()
                .map(savedResume -> {
                    JobSeekerProfile profile = savedResume.getJobSeeker();

                    JobSeekerProfileResponse dto = new JobSeekerProfileResponse();
                    dto.setId(profile.getId());
                    dto.setHeadline(profile.getHeadline());
                    dto.setSummary(profile.getSummary());
                    dto.setLocation(profile.getLocation());
                    dto.setEmploymentStatus(profile.getEmploymentStatus());

                    if (profile.getUser() != null) {
                        dto.setName(profile.getUser().getName());
                        dto.setEmail(profile.getUser().getEmail());
                        dto.setPhone(profile.getUser().getPhone());
                    }

                    if (savedResume.getJobPost() != null) {
                        dto.setAppliedRole(savedResume.getJobPost().getTitle());
                        dto.setJobId(savedResume.getJobPost().getId());
                    } else {
                        applicationRepository.findTopByJobSeekerIdAndJobPostCreatedByIdOrderByAppliedAtDesc(
                                profile.getId(), employer.getId())
                                .ifPresent(app -> {
                                    dto.setAppliedRole(app.getJobPost().getTitle());
                                    dto.setJobId(app.getJobPost().getId());
                                });
                    }
                    Optional<ResumeText> resumeTextOpt = resumeTextRepository.findByJobSeekerId(profile.getId());
                    if (resumeTextOpt.isPresent()) {
                        ResumeText resumeText = resumeTextOpt.get();
                        dto.setObjective(resumeText.getObjective());
                        dto.setEducation(resumeText.getEducationText());
                        dto.setExperience(resumeText.getExperienceText());
                        dto.setSkills(resumeText.getSkillsText());
                        dto.setProjects(resumeText.getProjectsText());
                        dto.setCertifications(resumeText.getCertificationsText());
                    }

                    return dto;
                })
                .toList();
    }
}
