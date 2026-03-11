package com.example.revhirehiringplatform.service;

import com.example.revhirehiringplatform.dto.response.JobPostResponse;
import com.example.revhirehiringplatform.model.Company;
import com.example.revhirehiringplatform.model.JobPost;
import com.example.revhirehiringplatform.repository.ApplicationRepository;
import com.example.revhirehiringplatform.repository.JobPostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JobSearchServiceTest {

    @Mock
    private JobPostRepository jobPostRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @InjectMocks
    private JobSearchService jobSearchService;

    private JobPost jobPost;

    @BeforeEach
    void setUp() {
        Company company = new Company();
        company.setId(10L);
        company.setName("Test Company");

        jobPost = new JobPost();
        jobPost.setId(100L);
        jobPost.setTitle("Software Engineer");
        jobPost.setDescription("Software Dev");
        jobPost.setLocation("New York");
        jobPost.setSalaryMin(100000.0);
        jobPost.setSalaryMax(150000.0);
        jobPost.setJobType("Full-time");
        jobPost.setCreatedAt(LocalDateTime.now());
        jobPost.setCompany(company);
        jobPost.setExperienceYears(3);
        jobPost.setStatus(JobPost.JobStatus.ACTIVE);
    }

    @Test
    void testSearchJobs_WithFilters() {
        when(jobPostRepository.findByFilters(
                eq("Software Engineer"), eq("New York"), eq(3), eq("Test Company"), eq(120000.0),
                any(List.class), eq(true), any(LocalDateTime.class)))
                .thenReturn(Collections.singletonList(jobPost));

        when(applicationRepository.countByJobPostId(100L)).thenReturn(5L);

        List<JobPostResponse> responses = jobSearchService.searchJobs(
                "Software Engineer", "New York", 3, "Test Company", 120000.0, List.of("Full-time"), 7);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        JobPostResponse response = responses.get(0);
        assertEquals(100L, response.getId());
        assertEquals("Software Engineer", response.getTitle());
        assertEquals("Test Company", response.getCompanyName());
        assertEquals(5L, response.getApplicantCount());
    }

    @Test
    void testSearchJobs_NoFilters() {
        when(jobPostRepository.findByFilters(
                eq(null), eq(null), eq(null), eq(null), eq(null),
                eq(null), eq(false), eq(null)))
                .thenReturn(Collections.singletonList(jobPost));

        when(applicationRepository.countByJobPostId(100L)).thenReturn(0L);

        List<JobPostResponse> responses = jobSearchService.searchJobs(
                null, null, null, null, null, null, null);

        assertNotNull(responses);
        assertEquals(1, responses.size());
    }
}
