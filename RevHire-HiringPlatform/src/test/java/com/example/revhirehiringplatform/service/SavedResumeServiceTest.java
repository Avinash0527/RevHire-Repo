package com.example.revhirehiringplatform.service;

import com.example.revhirehiringplatform.dto.response.JobSeekerProfileResponse;
import com.example.revhirehiringplatform.model.JobSeekerProfile;
import com.example.revhirehiringplatform.model.SavedResume;
import com.example.revhirehiringplatform.model.User;
import com.example.revhirehiringplatform.model.JobPost;
import com.example.revhirehiringplatform.repository.JobSeekerProfileRepository;
import com.example.revhirehiringplatform.repository.SavedResumeRepository;
import com.example.revhirehiringplatform.repository.ApplicationRepository;
import com.example.revhirehiringplatform.repository.ResumeTextRepository;
import com.example.revhirehiringplatform.repository.JobPostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class SavedResumeServiceTest {

    @Mock
    private SavedResumeRepository savedResumeRepository;

    @Mock
    private JobSeekerProfileRepository profileRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private ResumeTextRepository resumeTextRepository;

    @Mock
    private JobPostRepository jobPostRepository;

    @InjectMocks
    private SavedResumeService savedResumeService;

    private User employer;
    private User seekerUser;
    private JobSeekerProfile profile;
    private SavedResume savedResume;
    private JobPost jobPost;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        employer = new User();
        employer.setId(1L);
        employer.setEmail("employer@test.com");
        employer.setRole(User.Role.EMPLOYER);

        seekerUser = new User();
        seekerUser.setId(2L);
        seekerUser.setRole(User.Role.JOB_SEEKER);

        profile = new JobSeekerProfile();
        profile.setId(10L);
        profile.setUser(seekerUser);
        profile.setHeadline("Java Developer");

        savedResume = new SavedResume();
        savedResume.setId(100L);
        savedResume.setEmployer(employer);
        savedResume.setJobSeeker(profile);

        jobPost = new JobPost();
        jobPost.setId(5L);
        jobPost.setCreatedBy(employer);
        jobPost.setTitle("Software Engineer");
        savedResume.setJobPost(jobPost);
    }

    @Test
    void testSaveResume_Success() {
        when(profileRepository.findById(10L)).thenReturn(Optional.of(profile));
        when(jobPostRepository.findById(5L)).thenReturn(Optional.of(jobPost));
        when(savedResumeRepository.existsByEmployerIdAndJobSeekerIdAndJobPostId(1L, 10L, 5L)).thenReturn(false);

        savedResumeService.saveResume(10L, 5L, employer);

        verify(savedResumeRepository, times(1)).save(any(SavedResume.class));
        verify(notificationService, times(1)).createNotification(eq(seekerUser), anyString());
    }

    @Test
    void testSaveResume_AlreadySaved_ThrowsException() {
        when(profileRepository.findById(10L)).thenReturn(Optional.of(profile));
        when(jobPostRepository.findById(5L)).thenReturn(Optional.of(jobPost));
        when(savedResumeRepository.existsByEmployerIdAndJobSeekerIdAndJobPostId(1L, 10L, 5L)).thenReturn(true);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            savedResumeService.saveResume(10L, 5L, employer);
        });

        assertEquals("Resume already saved by this employer", exception.getMessage());
        verify(savedResumeRepository, never()).save(any(SavedResume.class));
    }

    @Test
    void testGetSavedResumes() {
        when(savedResumeRepository.findByEmployerId(1L)).thenReturn(Collections.singletonList(savedResume));
        when(applicationRepository.findTopByJobSeekerIdAndJobPostCreatedByIdOrderByAppliedAtDesc(anyLong(), anyLong()))
                .thenReturn(Optional.empty());
        when(resumeTextRepository.findByJobSeekerId(anyLong())).thenReturn(Optional.empty());

        List<JobSeekerProfileResponse> results = savedResumeService.getSavedResumes(employer);

        assertEquals(1, results.size());
        assertEquals("Java Developer", results.get(0).getHeadline());
    }
}
