package com.example.revhirehiringplatform.controller;


import com.example.revhirehiringplatform.repository.JobPostRepository;
import com.example.revhirehiringplatform.repository.ApplicationRepository;
import com.example.revhirehiringplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatisticsController {

    private final JobPostRepository jobPostRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;

    @GetMapping("/jobs/by-location")
    public ResponseEntity<Map<String, Long>> getJobsByLocation() {

        Map<String, Long> stats = new HashMap<>();
        jobPostRepository.findAll()
                .forEach(j -> stats.put(j.getLocation(), stats.getOrDefault(j.getLocation(), 0L) + 1));
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/jobs/by-type")
    public ResponseEntity<Map<String, Long>> getJobsByType() {
        Map<String, Long> stats = new HashMap<>();
        jobPostRepository.findAll().forEach(j -> stats.put(j.getJobType(), stats.getOrDefault(j.getJobType(), 0L) + 1));
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/applications/by-status")
    public ResponseEntity<Map<String, Long>> getApplicationsByStatus() {
        Map<String, Long> stats = new HashMap<>();
        applicationRepository.findAll()
                .forEach(a -> stats.put(a.getStatus().name(), stats.getOrDefault(a.getStatus().name(), 0L) + 1));
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/users/by-role")
    public ResponseEntity<Map<String, Long>> getUsersByRole() {
        Map<String, Long> stats = new HashMap<>();
        userRepository.findAll()
                .forEach(u -> stats.put(u.getRole().name(), stats.getOrDefault(u.getRole().name(), 0L) + 1));
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/jobs/trending")
    public ResponseEntity<java.util.List<String>> getTrendingJobs() {
        return ResponseEntity.ok(java.util.List.of("Software Engineer", "Data Scientist"));
    }

    @GetMapping("/applications/conversion-rate")
    public ResponseEntity<Double> getConversionRate() {
        return ResponseEntity.ok(0.15);
    }
}