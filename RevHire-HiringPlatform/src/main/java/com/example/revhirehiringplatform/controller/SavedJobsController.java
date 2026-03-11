package com.example.revhirehiringplatform.controller;
import com.example.revhirehiringplatform.dto.response.JobPostResponse;
import com.example.revhirehiringplatform.model.User;
import com.example.revhirehiringplatform.security.UserDetailsImpl;
import com.example.revhirehiringplatform.repository.UserRepository;
import com.example.revhirehiringplatform.service.SavedJobsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/seeker/saved-jobs")
@RequiredArgsConstructor
@Slf4j
public class SavedJobsController {

    private final SavedJobsService savedJobsService;
    private final UserRepository userRepository;

    private User getUserFromContext(UserDetailsImpl userDetails) {
        if (userDetails == null)
            return null;
        Optional<User> userOpt = userRepository.findById(userDetails.getId());
        return userOpt.orElse(null);
    }

    @PostMapping("/{jobId}")
    public ResponseEntity<?> saveJob(@PathVariable("jobId") Long jobId,
                                     @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = getUserFromContext(userDetails);
        if (user == null || user.getRole() != User.Role.JOB_SEEKER) {
            return ResponseEntity.status(403).body("Unauthorized");
        }
        try {
            savedJobsService.saveJob(jobId, user);
            return ResponseEntity.ok("Job saved successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{jobId}")
    public ResponseEntity<?> unsaveJob(@PathVariable("jobId") Long jobId,
                                       @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = getUserFromContext(userDetails);
        if (user == null || user.getRole() != User.Role.JOB_SEEKER) {
            return ResponseEntity.status(403).body("Unauthorized");
        }
        try {
            savedJobsService.unsaveJob(jobId, user);
            return ResponseEntity.ok("Job unsaved successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getSavedJobs(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = getUserFromContext(userDetails);
        if (user == null || user.getRole() != User.Role.JOB_SEEKER) {
            return ResponseEntity.status(403).body("Unauthorized");
        }
        List<JobPostResponse> savedJobs = savedJobsService.getSavedJobs(user);
        return ResponseEntity.ok(savedJobs);
    }

    @GetMapping("/{jobId}/check")
    public ResponseEntity<?> isJobSaved(@PathVariable("jobId") Long jobId,
                                        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = getUserFromContext(userDetails);
        if (user == null || user.getRole() != User.Role.JOB_SEEKER) {
            return ResponseEntity.status(403).body("Unauthorized");
        }
        return ResponseEntity.ok(false); // Map to service
    }

    @DeleteMapping("/clear")
    public ResponseEntity<?> clearSavedJobs(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = getUserFromContext(userDetails);
        if (user == null || user.getRole() != User.Role.JOB_SEEKER) {
            return ResponseEntity.status(403).body("Unauthorized");
        }
        return ResponseEntity.ok("Cleared");
    }
}