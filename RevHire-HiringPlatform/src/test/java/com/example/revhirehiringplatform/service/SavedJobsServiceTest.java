package com.example.revhirehiringplatform.service;

import com.example.revhirehiringplatform.dto.response.JobPostResponse;
import com.example.revhirehiringplatform.model.JobPost;
import com.example.revhirehiringplatform.model.JobSeekerProfile;
import com.example.revhirehiringplatform.model.SavedJobs;
import com.example.revhirehiringplatform.model.User;
import com.example.revhirehiringplatform.repository.JobPostRepository;
import com.example.revhirehiringplatform.repository.JobSeekerProfileRepository;
import com.example.revhirehiringplatform.repository.SavedJobsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SavedJobsServiceTest {

    @Mock
    private SavedJobsRepository savedJobsRepository;
    @Mock
    private JobPostRepository jobPostRepository;
    @Mock
    private JobSeekerProfileRepository profileRepository;

    @InjectMocks
    private SavedJobsService savedJobsService;

    private User user;
    private JobSeekerProfile profile;
    private JobPost jobPost;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        profile = new JobSeekerProfile();
        profile.setId(10L);
        profile.setUser(user);

        jobPost = new JobPost();
        jobPost.setId(100L);
        jobPost.setTitle("Test Job");
        jobPost.setCompany(new com.example.revhirehiringplatform.model.Company());
    }

    @Test
    void testSaveJob_Success() {
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(jobPostRepository.findById(100L)).thenReturn(Optional.of(jobPost));
        when(savedJobsRepository.findByJobSeekerIdAndJobPostId(10L, 100L)).thenReturn(Optional.empty());

        savedJobsService.saveJob(100L, user);

        verify(savedJobsRepository).save(any(SavedJobs.class));
    }

    @Test
    void testSaveJob_AlreadySaved_ThrowsException() {
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(jobPostRepository.findById(100L)).thenReturn(Optional.of(jobPost));
        when(savedJobsRepository.findByJobSeekerIdAndJobPostId(10L, 100L)).thenReturn(Optional.of(new SavedJobs()));

        assertThrows(RuntimeException.class, () -> savedJobsService.saveJob(100L, user));
        verify(savedJobsRepository, never()).save(any());
    }

    @Test
    void testSaveJob_ProfileNotFound_ThrowsException() {
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> savedJobsService.saveJob(100L, user));
        verify(savedJobsRepository, never()).save(any());
    }


    @Test
    void testUnsaveJob_Success() {
        SavedJobs savedJob = new SavedJobs();
        savedJob.setJobPost(jobPost);
        savedJob.setJobSeeker(profile);

        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(savedJobsRepository.findByJobSeekerIdAndJobPostId(10L, 100L)).thenReturn(Optional.of(savedJob));

        savedJobsService.unsaveJob(100L, user);

        verify(savedJobsRepository).delete(savedJob);
    }

    @Test
    void testUnsaveJob_NotFound_ThrowsException() {
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(savedJobsRepository.findByJobSeekerIdAndJobPostId(10L, 100L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> savedJobsService.unsaveJob(100L, user));
        verify(savedJobsRepository, never()).delete(any());
    }

    @Test
    void testGetSavedJobs() {
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        SavedJobs savedJob = new SavedJobs();
        savedJob.setJobPost(jobPost);
        when(savedJobsRepository.findByJobSeekerId(10L)).thenReturn(List.of(savedJob));

        List<JobPostResponse> response = savedJobsService.getSavedJobs(user);

        assertNotNull(response);
        assertFalse(response.isEmpty());
    }
}
