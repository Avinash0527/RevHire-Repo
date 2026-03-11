package com.example.revhirehiringplatform.controller;

import com.example.revhirehiringplatform.dto.response.ApplicationStatusHistoryResponse;
import com.example.revhirehiringplatform.model.ApplicationStatusHistory;
import com.example.revhirehiringplatform.model.User;
import com.example.revhirehiringplatform.repository.ApplicationRepository;
import com.example.revhirehiringplatform.repository.ApplicationStatusHistoryRepository;
import com.example.revhirehiringplatform.repository.UserRepository;
import com.example.revhirehiringplatform.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications/{applicationId}/history")
@RequiredArgsConstructor
@Slf4j
public class ApplicationStatusHistoryController {

    private final ApplicationStatusHistoryRepository historyRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;

    private User getUserFromContext(UserDetailsImpl userDetails) {
        if (userDetails == null)
            return null;
        return userRepository.findById(userDetails.getId()).orElse(null);
    }

    @GetMapping
    public ResponseEntity<?> getHistoryForApplication(@PathVariable("applicationId") Long applicationId,
                                                      @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = getUserFromContext(userDetails);
        if (user == null) {
            return ResponseEntity.status(403).body("Unauthorized");
        }

        com.example.revhirehiringplatform.model.Application application = applicationRepository.findById(applicationId).orElse(null);
        if (application == null) {
            return ResponseEntity.badRequest().body("Application not found");
        }

        boolean isEmployerOfJob = application.getJobPost().getCreatedBy().getId().equals(user.getId());
        boolean isApplicant = application.getJobSeeker().getUser().getId().equals(user.getId());

        if (!isEmployerOfJob && !isApplicant) {
            return ResponseEntity.status(403).body("Unauthorized to view this application's history");
        }

        List<ApplicationStatusHistoryResponse> history = historyRepository
                .findByApplicationIdOrderByChangedAtDesc(applicationId).stream()
                .map(this::mapToDto)
                .toList();

        return ResponseEntity.ok(history);
    }

    private ApplicationStatusHistoryResponse mapToDto(ApplicationStatusHistory history) {
        ApplicationStatusHistoryResponse dto = new ApplicationStatusHistoryResponse();
        dto.setId(history.getId());
        dto.setOldStatus(history.getOldStatus());
        dto.setNewStatus(history.getNewStatus());
        dto.setChangedByUserName(history.getChangedBy() != null ? history.getChangedBy().getName() : "System");
        dto.setComment(history.getComment());
        dto.setChangedAt(history.getChangedAt());
        return dto;
    }
}