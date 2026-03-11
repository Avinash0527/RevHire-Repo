package com.example.revhirehiringplatform.controller;
import com.example.revhirehiringplatform.dto.request.JobSeekerProfileRequest;
import com.example.revhirehiringplatform.dto.request.ResumeTextRequest;
import com.example.revhirehiringplatform.dto.response.ApplicationResponse;
import com.example.revhirehiringplatform.dto.response.JobSeekerProfileResponse;
import com.example.revhirehiringplatform.dto.response.SkillResponse;
import com.example.revhirehiringplatform.model.JobSeekerProfile;
import com.example.revhirehiringplatform.model.ResumeText;
import com.example.revhirehiringplatform.model.User;
import com.example.revhirehiringplatform.security.UserDetailsImpl;
import com.example.revhirehiringplatform.repository.UserRepository;
import com.example.revhirehiringplatform.service.JobSeekerProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/seeker/profile")
@RequiredArgsConstructor
@Slf4j
public class JobSeekerProfileController {

    private final JobSeekerProfileService profileService;
    private final com.example.revhirehiringplatform.service.JobSeekerResumeService resumeService;
    private final UserRepository userRepository;

    private User getUserFromContext(UserDetailsImpl userDetails) {
        if (userDetails == null)
            return null;
        Optional<User> userOpt = userRepository.findById(userDetails.getId());
        return userOpt.orElse(null);
    }

    @PostMapping(consumes = { "multipart/form-data" })
    public ResponseEntity<?> updateProfile(
            @RequestPart("profile") JobSeekerProfileRequest profileDto,
            @RequestPart(value = "resume", required = false) MultipartFile resume,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        User user = getUserFromContext(userDetails);
        log.info("Update profile request for user: {}. UserDetails: {}",
                user != null ? user.getEmail() : "null",
                userDetails != null ? userDetails.getUsername() : "null");

        if (user == null || user.getRole() != User.Role.JOB_SEEKER) {
            log.warn("Unauthorized access to update profile. User: {}, Role: {}",
                    user != null ? user.getEmail() : "null",
                    user != null ? user.getRole() : "null");
            return ResponseEntity.status(403).body("Unauthorized");
        }
        try {
            JobSeekerProfile profile = profileService.updateProfile(profileDto, resume, user);
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/resume")
    public ResponseEntity<?> uploadResume(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = getUserFromContext(userDetails);
        if (user == null || user.getRole() != User.Role.JOB_SEEKER) {
            return ResponseEntity.status(403).body("Unauthorized");
        }
        try {
            JobSeekerProfile profile = profileService.getProfile(user);
            com.example.revhirehiringplatform.model.ResumeFiles savedFile = resumeService.storeFile(file, profile);
            return ResponseEntity.ok(savedFile);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping(consumes = { "application/json" })
    public ResponseEntity<?> updateProfileJson(
            @RequestBody JobSeekerProfileRequest profileDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        User user = getUserFromContext(userDetails);
        if (user == null || user.getRole() != User.Role.JOB_SEEKER) {
            return ResponseEntity.status(403).body("Unauthorized");
        }
        try {
            // Null for resume file since this is a JSON-only request
            JobSeekerProfile profile = profileService.updateProfile(profileDto, null, user);
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = getUserFromContext(userDetails);
        if (user == null || user.getRole() != User.Role.JOB_SEEKER) {
            return ResponseEntity.status(403).body("Unauthorized");
        }
        try {
            JobSeekerProfile profile = profileService.getProfile(user);

            JobSeekerProfileResponse dto = new JobSeekerProfileResponse();
            dto.setId(profile.getId());
            dto.setName(user.getName());
            dto.setEmail(user.getEmail());
            dto.setHeadline(profile.getHeadline());
            dto.setSummary(profile.getSummary());
            dto.setLocation(profile.getLocation());
            dto.setPhone(user.getPhone());
            dto.setEmploymentStatus(profile.getEmploymentStatus());

            // Populate Resume Text Fields
            ResumeText resumeText = profileService.getResumeText(profile.getId());
            if (resumeText != null) {
                dto.setTitle(resumeText.getTitle());
                dto.setObjective(resumeText.getObjective());
                dto.setEducation(resumeText.getEducationText());
                dto.setExperience(resumeText.getExperienceText());
                dto.setSkills(resumeText.getSkillsText());
                dto.setProjects(resumeText.getProjectsText());
                dto.setCertifications(resumeText.getCertificationsText());
            }
            dto.setSkillsList(profileService.getSeekerSkills(profile.getId()));

            boolean hasResumeFile = resumeService.getResumeFile(profile.getId()) != null;
            boolean hasSummary = (profile.getSummary() != null && !profile.getSummary().trim().isEmpty()) ||
                    (profile.getHeadline() != null && !profile.getHeadline().trim().isEmpty());
            boolean hasSkills = resumeText != null && resumeText.getSkillsText() != null
                    && !resumeText.getSkillsText().trim().isEmpty();
            boolean hasExperience = resumeText != null && resumeText.getExperienceText() != null
                    && !resumeText.getExperienceText().trim().isEmpty();
            boolean hasEducation = resumeText != null && resumeText.getEducationText() != null
                    && !resumeText.getEducationText().trim().isEmpty();

            int completedTasks = 0;
            if (hasResumeFile)
                completedTasks++;
            if (hasSummary)
                completedTasks++;
            if (hasSkills)
                completedTasks++;
            if (hasExperience)
                completedTasks++;
            if (hasEducation)
                completedTasks++;

            dto.setResumeUploaded(hasResumeFile);
            dto.setProfileSummarySet(hasSummary);
            dto.setSkillsSet(hasSkills);
            dto.setExperienceSet(hasExperience);
            dto.setEducationSet(hasEducation);
            dto.setCompletionPercentage((completedTasks * 100) / 5);

            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/resume-text")
    public ResponseEntity<?> getResumeText(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = getUserFromContext(userDetails);
        if (user == null || user.getRole() != User.Role.JOB_SEEKER) {
            return ResponseEntity.status(403).body("Unauthorized");
        }
        try {
            JobSeekerProfile profile = profileService.getProfile(user);
            ResumeText resumeText = profileService.getResumeText(profile.getId());

            ResumeTextRequest dto = new ResumeTextRequest();
            if (resumeText != null) {
                dto.setTitle(resumeText.getTitle());
                dto.setObjective(resumeText.getObjective());
                dto.setEducation(resumeText.getEducationText());
                dto.setExperience(resumeText.getExperienceText());
                dto.setSkills(resumeText.getSkillsText());
                dto.setProjects(resumeText.getProjectsText());
                dto.setCertifications(resumeText.getCertificationsText());
            }

            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/resume-text")
    public ResponseEntity<?> updateResumeText(
            @RequestBody ResumeTextRequest textDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = getUserFromContext(userDetails);
        if (user == null || user.getRole() != User.Role.JOB_SEEKER) {
            return ResponseEntity.status(403).body("Unauthorized");
        }
        try {
            ResumeText savedText = profileService.updateResumeText(textDto, user);
            return ResponseEntity.ok(savedText);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{seekerId}")
    public ResponseEntity<?> getProfileById(@PathVariable("seekerId") Long seekerId,
                                            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = getUserFromContext(userDetails);
        if (user == null || user.getRole() != User.Role.EMPLOYER) {
            return ResponseEntity.status(403).body("Unauthorized: Only Employers can view full profiles.");
        }
        try {
            JobSeekerProfile profile = profileService.getProfileById(seekerId);

            JobSeekerProfileResponse dto = new JobSeekerProfileResponse();
            dto.setId(profile.getId());
            dto.setName(profile.getUser() != null ? profile.getUser().getName() : null);
            dto.setEmail(profile.getUser() != null ? profile.getUser().getEmail() : null);
            dto.setHeadline(profile.getHeadline());
            dto.setSummary(profile.getSummary());
            dto.setLocation(profile.getLocation());
            dto.setPhone(profile.getUser() != null ? profile.getUser().getPhone() : null);
            dto.setEmploymentStatus(profile.getEmploymentStatus());

            // Populate Resume Text Fields for Employer view
            ResumeText resumeText = profileService.getResumeText(profile.getId());
            if (resumeText != null) {
                dto.setTitle(resumeText.getTitle());
                dto.setObjective(resumeText.getObjective());
                dto.setEducation(resumeText.getEducationText());
                dto.setExperience(resumeText.getExperienceText());
                dto.setSkills(resumeText.getSkillsText());
                dto.setProjects(resumeText.getProjectsText());
                dto.setCertifications(resumeText.getCertificationsText());
            }
            dto.setSkillsList(profileService.getSeekerSkills(profile.getId()));

            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{seekerId}/resume/download")
    public ResponseEntity<?> downloadResume(@PathVariable("seekerId") Long seekerId,
                                            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = getUserFromContext(userDetails);
        if (user == null || user.getRole() != User.Role.EMPLOYER) {
            return ResponseEntity.status(403).body("Unauthorized: Only Employers can download resumes.");
        }
        try {
            com.example.revhirehiringplatform.model.ResumeFiles resumeFile = resumeService.getResumeFile(seekerId);
            if (resumeFile == null) {
                return ResponseEntity.notFound().build();
            }

            java.nio.file.Path filePath = java.nio.file.Paths.get("uploads/resumes").resolve(resumeFile.getFilePath())
                    .normalize();
            org.springframework.core.io.Resource resource = new org.springframework.core.io.UrlResource(
                    filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + resumeFile.getFileName() + "\"")
                    .body(resource);

        } catch (Exception e) {
            log.error("Error downloading resume", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{seekerId}")
    public ResponseEntity<?> updateProfileById(@PathVariable("seekerId") Long seekerId,
                                               @RequestBody JobSeekerProfileRequest profileDto,
                                               @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = getUserFromContext(userDetails);
        if (user == null || !user.getId().equals(seekerId)) {
            return ResponseEntity.status(403).body("Unauthorized");
        }
        try {
            JobSeekerProfile profile = profileService.updateProfile(profileDto, null, user);
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/me")
    public ResponseEntity<?> deleteMyAccount(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = getUserFromContext(userDetails);
        if (user == null) {
            return ResponseEntity.status(403).body("Unauthorized");
        }
        try {
            JobSeekerProfile profile = profileService.getProfile(user);
            profileService.deleteProfile(profile.getId(), user);
            return ResponseEntity.ok("Account deactivated successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{seekerId}")
    public ResponseEntity<?> deleteProfile(@PathVariable("seekerId") Long seekerId,
                                           @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = getUserFromContext(userDetails);
        if (user == null) {
            return ResponseEntity.status(403).body("Unauthorized");
        }
        try {
            profileService.deleteProfile(seekerId, user);
            return ResponseEntity.ok("Profile deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{seekerId}/applications")
    public ResponseEntity<?> getSeekerApplications(@PathVariable("seekerId") Long seekerId,
                                                   @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = getUserFromContext(userDetails);
        if (user == null) {
            return ResponseEntity.status(403).body("Unauthorized");
        }
        try {
            List<ApplicationResponse> applications = profileService.getSeekerApplications(seekerId, user);
            return ResponseEntity.ok(applications);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{seekerId}/skills")
    public ResponseEntity<?> getSeekerSkills(@PathVariable("seekerId") Long seekerId) {
        try {
            List<SkillResponse> skills = profileService.getSeekerSkills(seekerId);
            return ResponseEntity.ok(skills);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}