package com.example.revhirehiringplatform.service;

import com.example.revhirehiringplatform.dto.response.JobPostResponse;
import com.example.revhirehiringplatform.dto.response.JobSeekerProfileResponse;
import com.example.revhirehiringplatform.model.JobPost;
import com.example.revhirehiringplatform.model.JobSeekerProfile;
import com.example.revhirehiringplatform.model.ResumeText;
import com.example.revhirehiringplatform.repository.ApplicationRepository;
import com.example.revhirehiringplatform.repository.JobPostRepository;
import com.example.revhirehiringplatform.repository.JobSeekerProfileRepository;
import com.example.revhirehiringplatform.repository.ResumeTextRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {

    private final JobPostRepository jobPostRepository;
    private final JobSeekerProfileRepository profileRepository;
    private final ResumeTextRepository resumeTextRepository;
    private final ApplicationRepository applicationRepository;
    private final JobSeekerProfileService profileService;

    public List<JobPostResponse> searchJobs(String keyword) {
        log.info("Searching jobs with keyword: {}", keyword);
        return jobPostRepository.searchByKeyword(keyword).stream()
                .map(this::mapToJobDto)
                .collect(Collectors.toList());
    }

    public List<JobSeekerProfileResponse> searchSeekers(String keyword) {
        log.info("Searching seekers with keyword: {}", keyword);
        return profileRepository.searchByKeyword(keyword).stream()
                .map(this::mapToSeekerDto)
                .collect(Collectors.toList());
    }

    private JobPostResponse mapToJobDto(JobPost jobPost) {
        JobPostResponse dto = new JobPostResponse();
        dto.setId(jobPost.getId());
        dto.setTitle(jobPost.getTitle());
        dto.setDescription(jobPost.getDescription());
        dto.setLocation(jobPost.getLocation());
        dto.setSalary(jobPost.getSalaryMin() + " - " + jobPost.getSalaryMax());
        dto.setJobType(jobPost.getJobType());
        dto.setCompanyName(jobPost.getCompany().getName());
        dto.setPostedDate(jobPost.getCreatedAt() != null ? jobPost.getCreatedAt().toLocalDate() : LocalDate.now());
        dto.setApplicantCount(applicationRepository.countByJobPostId(jobPost.getId()));
        return dto;
    }

    private JobSeekerProfileResponse mapToSeekerDto(JobSeekerProfile profile) {
        JobSeekerProfileResponse dto = new JobSeekerProfileResponse();
        dto.setId(profile.getId());
        dto.setName(profile.getUser().getName());
        dto.setEmail(profile.getUser().getEmail());
        dto.setHeadline(profile.getHeadline());
        dto.setLocation(profile.getLocation());

        ResumeText resumeText = resumeTextRepository.findByJobSeekerId(profile.getId()).orElse(null);
        if (resumeText != null) {
            dto.setSkills(resumeText.getSkillsText());
        }
        dto.setSkillsList(profileService.getSeekerSkills(profile.getId()));

        return dto;
    }
}
