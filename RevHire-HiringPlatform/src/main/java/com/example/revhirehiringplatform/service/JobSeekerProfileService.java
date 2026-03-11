package com.example.revhirehiringplatform.service;

import com.example.revhirehiringplatform.dto.request.JobSeekerProfileRequest;
import com.example.revhirehiringplatform.dto.request.ResumeTextRequest;
import com.example.revhirehiringplatform.dto.response.ApplicationResponse;
import com.example.revhirehiringplatform.dto.response.SkillResponse;
import com.example.revhirehiringplatform.model.JobSeekerProfile;
import com.example.revhirehiringplatform.model.ResumeText;
import com.example.revhirehiringplatform.model.SeekerSkillMap;
import com.example.revhirehiringplatform.model.SkillsMaster;
import com.example.revhirehiringplatform.model.User;
import com.example.revhirehiringplatform.repository.ApplicationRepository;
import com.example.revhirehiringplatform.repository.JobSeekerProfileRepository;
import com.example.revhirehiringplatform.repository.ResumeTextRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobSeekerProfileService {

    private static final String PROFILE_NOT_FOUND = "Profile not found";

    private final JobSeekerProfileRepository profileRepository;
    private final ResumeTextRepository resumeTextRepository;
    private final JobSeekerResumeService resumeService;
    private final com.example.revhirehiringplatform.repository.UserRepository userRepository;
    private final com.example.revhirehiringplatform.repository.SkillsMasterRepository skillsMasterRepository;
    private final com.example.revhirehiringplatform.repository.SeekerSkillMapRepository seekerSkillMapRepository;
    private final ApplicationRepository applicationRepository;

    @Transactional
    public JobSeekerProfile updateProfile(JobSeekerProfileRequest profileDto, MultipartFile resumeFile, User user) {
        log.info("Updating profile for user: {}", user.getEmail());
        JobSeekerProfile profile = profileRepository.findByUserId(user.getId()).orElse(new JobSeekerProfile());

        if (profile.getUser() == null) {
            profile.setUser(user);
        }

        if (profileDto.getPhone() != null) {
            user.setPhone(profileDto.getPhone());
            userRepository.save(user);
        }

        profile.setHeadline(profileDto.getHeadline());
        profile.setSummary(profileDto.getSummary());
        profile.setLocation(profileDto.getLocation());
        profile.setEmploymentStatus(profileDto.getEmploymentStatus());
        if (profileDto.getProfileImage() != null) {
            profile.setProfileImage(profileDto.getProfileImage());
        }

        JobSeekerProfile savedProfile = profileRepository.save(profile);

        ResumeText resumeText = resumeTextRepository.findByJobSeekerId(savedProfile.getId()).orElse(new ResumeText());
        resumeText.setJobSeeker(savedProfile);
        if (profileDto.getObjective() != null)
            resumeText.setObjective(profileDto.getObjective());
        if (profileDto.getTitle() != null)
            resumeText.setTitle(profileDto.getTitle());
        if (profileDto.getEducation() != null)
            resumeText.setEducationText(profileDto.getEducation());
        if (profileDto.getExperience() != null)
            resumeText.setExperienceText(profileDto.getExperience());
        if (profileDto.getSkills() != null)
            resumeText.setSkillsText(profileDto.getSkills());
        if (profileDto.getProjects() != null)
            resumeText.setProjectsText(profileDto.getProjects());
        if (profileDto.getCertifications() != null)
            resumeText.setCertificationsText(profileDto.getCertifications());
        resumeTextRepository.save(resumeText);

        if (resumeFile != null && !resumeFile.isEmpty()) {
            resumeService.storeFile(resumeFile, savedProfile);
        }

        return savedProfile;
    }

    @Transactional
    public ResumeText updateResumeText(ResumeTextRequest textDto, User user) {
        log.info("Updating resume text for user: {}", user.getEmail());
        JobSeekerProfile profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Profile not found. Please create a profile first."));

        ResumeText resumeText = resumeTextRepository.findByJobSeekerId(profile.getId()).orElse(new ResumeText());
        resumeText.setJobSeeker(profile);
        resumeText.setTitle(textDto.getTitle());
        resumeText.setObjective(textDto.getObjective());
        resumeText.setEducationText(textDto.getEducation());
        resumeText.setExperienceText(textDto.getExperience());
        resumeText.setSkillsText(textDto.getSkills());
        resumeText.setProjectsText(textDto.getProjects());
        resumeText.setCertificationsText(textDto.getCertifications());

        resumeText.setCertificationsText(textDto.getCertifications());

        if (textDto.getSkills() != null && !textDto.getSkills().trim().isEmpty()) {
            List<String> skillNames = Arrays.stream(textDto.getSkills().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
            List<SeekerSkillMap> existingMaps = seekerSkillMapRepository.findByJobSeekerId(profile.getId());
            seekerSkillMapRepository.deleteAll(existingMaps);

            for (String skillName : skillNames) {
                SkillsMaster skillMaster = skillsMasterRepository.findBySkillNameIgnoreCase(skillName)
                        .orElseGet(() -> {
                            SkillsMaster master = new SkillsMaster();
                            master.setSkillName(skillName);
                            return skillsMasterRepository.save(master);
                        });

                SeekerSkillMap skillMap = new SeekerSkillMap();
                skillMap.setJobSeeker(profile);
                skillMap.setSkill(skillMaster);
                skillMap.setLevel(SeekerSkillMap.SkillLevel.INTERMEDIATE);
                seekerSkillMapRepository.save(skillMap);
            }
        }

        return resumeTextRepository.save(resumeText);
    }

    public JobSeekerProfile getProfile(User user) {
        return profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException(PROFILE_NOT_FOUND));
    }

    public ResumeText getResumeText(Long profileId) {
        return resumeTextRepository.findByJobSeekerId(profileId).orElse(null);
    }

    public JobSeekerProfile getProfileById(Long profileId) {
        return profileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException(PROFILE_NOT_FOUND));
    }

    @Transactional
    public void deleteProfile(Long seekerId, User user) {
        JobSeekerProfile profile = profileRepository.findById(seekerId)
                .orElseThrow(() -> new IllegalArgumentException(PROFILE_NOT_FOUND));

        if (!profile.getUser().getId().equals(user.getId()) && user.getRole() != User.Role.ADMIN) {
            throw new IllegalStateException("Unauthorized to delete this profile");
        }

        User u = profile.getUser();
        if (u != null) {
            u.setStatus(false);
            userRepository.save(u);
        }
        log.info("Profile deactivated: {} by user: {}", seekerId, user.getEmail());
    }

    public List<ApplicationResponse> getSeekerApplications(Long seekerId, User user) {
        if (!user.getId().equals(seekerId) && user.getRole() != User.Role.ADMIN) {
        }
        return applicationRepository.findByJobSeekerId(seekerId).stream()
                .map(this::mapApplicationToDto)
                .toList();
    }

    public List<SkillResponse> getSeekerSkills(Long seekerId) {
        return seekerSkillMapRepository.findByJobSeekerId(seekerId).stream()
                .map(map -> {
                    SkillResponse res = new SkillResponse();
                    res.setId(map.getSkill().getId());
                    res.setName(map.getSkill().getSkillName());
                    res.setLevel(map.getLevel() != null ? map.getLevel().name() : null);
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

        if (app.getJobSeeker() != null) {
            dto.setJobSeekerProfileImage(app.getJobSeeker().getProfileImage());
        }
        if (app.getJobPost() != null && app.getJobPost().getCompany() != null) {
            dto.setCompanyLogo(app.getJobPost().getCompany().getLogo());
        }

        return dto;
    }
}