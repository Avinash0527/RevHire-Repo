package com.example.revhirehiringplatform.controller;



import com.example.revhirehiringplatform.repository.JobPostRepository;
import com.example.revhirehiringplatform.repository.ApplicationRepository;
import com.example.revhirehiringplatform.repository.UserRepository;
import com.example.revhirehiringplatform.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final JobPostRepository jobPostRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;

    @GetMapping("/admin/metrics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAdminMetrics() {
        try {
            log.info("Fetching admin metrics...");
            Map<String, Object> metrics = new HashMap<>();


            metrics.put("totalUsers", userRepository.count());
            metrics.put("seekers", userRepository.countByRole(com.example.revhirehiringplatform.model.User.Role.JOB_SEEKER));
            metrics.put("employers", userRepository.countByRole(com.example.revhirehiringplatform.model.User.Role.EMPLOYER));
            metrics.put("activeUsers", userRepository.countByStatus(true));
            metrics.put("inactiveUsers", userRepository.countByStatus(false));


            metrics.put("totalJobs", jobPostRepository.count());
            metrics.put("activeJobs", jobPostRepository.countByStatus(com.example.revhirehiringplatform.model.JobPost.JobStatus.ACTIVE));
            metrics.put("closedJobs", jobPostRepository.countByStatus(com.example.revhirehiringplatform.model.JobPost.JobStatus.CLOSED));


            metrics.put("totalApplications", applicationRepository.count());
            metrics.put("applied",
                    applicationRepository.countByStatus(com.example.revhirehiringplatform.model.Application.ApplicationStatus.APPLIED));
            metrics.put("shortlisted",
                    applicationRepository.countByStatus(com.example.revhirehiringplatform.model.Application.ApplicationStatus.SHORTLISTED));
            metrics.put("selected",
                    applicationRepository.countByStatus(com.example.revhirehiringplatform.model.Application.ApplicationStatus.SELECTED));
            metrics.put("rejected",
                    applicationRepository.countByStatus(com.example.revhirehiringplatform.model.Application.ApplicationStatus.REJECTED));

            log.info("Admin metrics fetched successfully: {}", metrics);
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            log.error("Error fetching admin metrics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/employer/metrics")
    public ResponseEntity<Map<String, Object>> getEmployerMetrics(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Map<String, Object> metrics = new HashMap<>();

        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/seeker/metrics")
    public ResponseEntity<Map<String, Object>> getSeekerMetrics(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        Map<String, Object> metrics = new HashMap<>();

        metrics.put("seekerId", userDetails.getId());
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/system/health")
    public ResponseEntity<Map<String, String>> getSystemHealth() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("database", "CONNECTED");
        return ResponseEntity.ok(status);
    }

    @GetMapping("/system/logs")
    public ResponseEntity<java.util.List<String>> getSystemLogs() {
        return ResponseEntity.ok(java.util.List.of("System started", "Database connected"));
    }
}