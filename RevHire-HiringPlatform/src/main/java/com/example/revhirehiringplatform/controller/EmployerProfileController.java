package com.example.revhirehiringplatform.controller;

import com.example.revhirehiringplatform.model.EmployerProfile;
import com.example.revhirehiringplatform.repository.EmployerProfileRepository;
import com.example.revhirehiringplatform.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employer-profiles")
@RequiredArgsConstructor
public class EmployerProfileController {

    private final EmployerProfileRepository employerProfileRepository;

    @GetMapping
    public ResponseEntity<List<EmployerProfile>> getAllProfiles() {
        return ResponseEntity.ok(employerProfileRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployerProfile> getProfileById(@PathVariable Long id) {
        return employerProfileRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProfile(@PathVariable Long id, @RequestBody EmployerProfile profileDetails,
                                           @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return employerProfileRepository.findById(id)
                .map(profile -> {
                    if (!profile.getUser().getId().equals(userDetails.getId())) {
                        return ResponseEntity.status(403).body("Unauthorized");
                    }
                    profile.setDesignation(profileDetails.getDesignation());
                    return ResponseEntity.ok(employerProfileRepository.save(profile));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProfile(@PathVariable Long id,
                                           @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return employerProfileRepository.findById(id)
                .map(profile -> {
                    if (!profile.getUser().getId().equals(userDetails.getId())) {
                        return ResponseEntity.status(403).body("Unauthorized");
                    }
                    employerProfileRepository.delete(profile);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<EmployerProfile> getProfileByUserId(@PathVariable Long userId) {
        return employerProfileRepository.findByUserId(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}