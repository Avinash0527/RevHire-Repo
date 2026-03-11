package com.example.revhirehiringplatform.service;



import com.example.revhirehiringplatform.dto.response.ApplicationResponse;
import com.example.revhirehiringplatform.model.Application;
import com.example.revhirehiringplatform.model.Company;
import com.example.revhirehiringplatform.model.JobPost;
import com.example.revhirehiringplatform.model.JobSeekerProfile;
import com.example.revhirehiringplatform.model.User;
import com.example.revhirehiringplatform.repository.ApplicationRepository;
import com.example.revhirehiringplatform.repository.ApplicationStatusHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ApplicationWithdrawalServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private ApplicationStatusHistoryRepository statusHistoryRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ApplicationWithdrawalService applicationWithdrawalService;

    private Application application;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Seeker");
        user.setEmail("seeker@test.com");

        JobSeekerProfile profile = new JobSeekerProfile();
        profile.setId(10L);
        profile.setUser(user);
        profile.setSummary("Java Dev");

        Company company = new Company();
        company.setName("Tech Corp");

        JobPost jobPost = new JobPost();
        jobPost.setId(100L);
        jobPost.setTitle("Dev");
        jobPost.setCompany(company);

        application = new Application();
        application.setId(1000L);
        application.setJobSeeker(profile);
        application.setJobPost(jobPost);
        application.setStatus(Application.ApplicationStatus.APPLIED);
    }

    @Test
    void testWithdrawApplication() {
        when(applicationRepository.findById(1000L)).thenReturn(Optional.of(application));
        when(applicationRepository.save(any(Application.class))).thenAnswer(i -> i.getArguments()[0]);
        when(statusHistoryRepository.save(any())).thenReturn(null);

        ApplicationResponse response = applicationWithdrawalService.withdrawApplication(1000L, "Found another job",
                user);
        assertNotNull(response);
    }
}
