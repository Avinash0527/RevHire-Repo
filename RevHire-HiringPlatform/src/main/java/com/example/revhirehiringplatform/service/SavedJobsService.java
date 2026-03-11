package com.example.revhirehiringplatform.service;

import com.example.revhirehiringplatform.dto.response.JobPostResponse;
import com.example.revhirehiringplatform.model.JobPost;
import com.example.revhirehiringplatform.model.JobSeekerProfile;
import com.example.revhirehiringplatform.model.SavedJobs;
import com.example.revhirehiringplatform.model.User;
import com.example.revhirehiringplatform.repository.JobPostRepository;
import com.example.revhirehiringplatform.repository.JobSeekerProfileRepository;
import com.example.revhirehiringplatform.repository.SavedJobsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SavedJobsService {

    private final SavedJobsRepository savedJobsRepository;
    private final JobPostRepository jobPostRepository;
    private final JobSeekerProfileRepository profileRepository;

    @Transactional
    public void saveJob(Long jobId, User user) {
        JobSeekerProfile profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        JobPost jobPost = jobPostRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        if (savedJobsRepository.findByJobSeekerIdAndJobPostId(profile.getId(), jobId).isPresent()) {
            throw new RuntimeException("Job already saved");
        }

        SavedJobs savedJobs = new SavedJobs();
        savedJobs.setJobSeeker(profile);
        savedJobs.setJobPost(jobPost);
        savedJobsRepository.save(savedJobs);
    }

    @Transactional
    public void unsaveJob(Long jobId, User user) {
        JobSeekerProfile profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        SavedJobs savedJob = savedJobsRepository.findByJobSeekerIdAndJobPostId(profile.getId(), jobId)
                .orElseThrow(() -> new RuntimeException("Saved job not found"));

        savedJobsRepository.delete(savedJob);
    }

    public List<JobPostResponse> getSavedJobs(User user) {
        JobSeekerProfile profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        return savedJobsRepository.findByJobSeekerId(profile.getId()).stream()
                .map(sj -> mapToDto(sj.getJobPost()))
                .collect(Collectors.toList());
    }

    private JobPostResponse mapToDto(JobPost jobPost) {
        JobPostResponse dto = new JobPostResponse();
        dto.setId(jobPost.getId());
        dto.setTitle(jobPost.getTitle());
        dto.setDescription(jobPost.getDescription());
        dto.setLocation(jobPost.getLocation());
        dto.setSalary(jobPost.getSalaryMin() + " - " + jobPost.getSalaryMax());
        dto.setJobType(jobPost.getJobType());
        dto.setPostedDate(jobPost.getCreatedAt() != null ? jobPost.getCreatedAt().toLocalDate() : LocalDate.now());
        dto.setCompanyName(jobPost.getCompany().getName());
        dto.setCompanyLogo(jobPost.getCompany().getLogo());
        return dto;
    }
}
