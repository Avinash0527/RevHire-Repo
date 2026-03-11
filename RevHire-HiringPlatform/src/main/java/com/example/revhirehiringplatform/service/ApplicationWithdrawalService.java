package com.example.revhirehiringplatform.service;

import com.example.revhirehiringplatform.dto.response.ApplicationResponse;
import com.example.revhirehiringplatform.model.Application;
import com.example.revhirehiringplatform.model.User;
import com.example.revhirehiringplatform.model.ApplicationStatusHistory;
import com.example.revhirehiringplatform.repository.ApplicationRepository;
import com.example.revhirehiringplatform.repository.ApplicationStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationWithdrawalService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationStatusHistoryRepository statusHistoryRepository;
    private final NotificationService notificationService;

    @Transactional
    public ApplicationResponse withdrawApplication(Long applicationId, String reason, User user) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (!application.getJobSeeker().getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to withdraw this application");
        }

        Application.ApplicationStatus oldStatus = application.getStatus();
        application.setStatus(Application.ApplicationStatus.WITHDRAWN);
        application.setWithdrawReason(reason);
        Application savedApp = applicationRepository.save(application);

        ApplicationStatusHistory history = new ApplicationStatusHistory();
        history.setApplication(savedApp);
        history.setOldStatus(oldStatus != null ? oldStatus.name() : "");
        history.setNewStatus(Application.ApplicationStatus.WITHDRAWN.name());
        history.setChangedBy(user);
        history.setComment("Application withdrawn by job seeker: " + reason);
        statusHistoryRepository.save(history);

        notificationService.createNotification(user,
                "You have withdrawn your application for the " + application.getJobPost().getTitle() + " position at "
                        + application.getJobPost().getCompany().getName(),
                true, "Application Withdrawn: " + application.getJobPost().getTitle(),
                "Your application for the " + application.getJobPost().getTitle() + " position at "
                        + application.getJobPost().getCompany().getName() + " has been withdrawn.");

        notificationService.createNotification(application.getJobPost().getCreatedBy(),
                "Application withdrawn for " + application.getJobPost().getTitle() + " by " + user.getName(),
                true, "Application Withdrawn for " + application.getJobPost().getTitle(),
                "The application for " + application.getJobPost().getTitle() + " from " + user.getName()
                        + " has been withdrawn.");

        return mapToDto(savedApp);
    }

    private ApplicationResponse mapToDto(Application app) {
        ApplicationResponse dto = new ApplicationResponse();
        dto.setId(app.getId());
        dto.setJobId(app.getJobPost().getId());
        dto.setJobTitle(app.getJobPost().getTitle());
        dto.setCompanyName(app.getJobPost().getCompany().getName());
        dto.setJobSeekerId(app.getJobSeeker().getId());
        dto.setJobSeekerName(app.getJobSeeker().getUser().getName());
        dto.setJobSeekerEmail(app.getJobSeeker().getUser().getEmail());
        dto.setJobSeekerSkills(app.getJobSeeker().getSummary());
        dto.setStatus(app.getStatus());
        dto.setAppliedAt(app.getAppliedAt());
        return dto;
    }
}