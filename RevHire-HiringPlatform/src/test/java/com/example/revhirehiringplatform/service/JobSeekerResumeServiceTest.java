package com.example.revhirehiringplatform.service;
import com.example.revhirehiringplatform.model.JobSeekerProfile;
import com.example.revhirehiringplatform.model.ResumeFiles;
import com.example.revhirehiringplatform.repository.ResumeFilesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JobSeekerResumeServiceTest {

    @Mock
    private ResumeFilesRepository resumeFilesRepository;

    @InjectMocks
    private JobSeekerResumeService jobSeekerResumeService;

    @BeforeEach
    void setUp() {
        jobSeekerResumeService.init();
    }

    @Test
    void testGetResumeFile() {
        ResumeFiles resumeFile = new ResumeFiles();
        when(resumeFilesRepository.findByJobSeekerId(1L)).thenReturn(List.of(resumeFile));

        ResumeFiles result = jobSeekerResumeService.getResumeFile(1L);
        assertNotNull(result);
    }

    @Test
    void testGetResumeFileNotFound() {
        when(resumeFilesRepository.findByJobSeekerId(1L)).thenReturn(List.of());
        ResumeFiles result = jobSeekerResumeService.getResumeFile(1L);
        assertNull(result);
    }

    @Test
    void testStoreFile() {
        MockMultipartFile file = new MockMultipartFile("file", "resume.pdf", "application/pdf",
                "dummy content".getBytes());
        JobSeekerProfile profile = new JobSeekerProfile();

        when(resumeFilesRepository.save(any(ResumeFiles.class))).thenAnswer(i -> i.getArguments()[0]);

        ResumeFiles result = jobSeekerResumeService.storeFile(file, profile);
        assertNotNull(result);
    }
}

