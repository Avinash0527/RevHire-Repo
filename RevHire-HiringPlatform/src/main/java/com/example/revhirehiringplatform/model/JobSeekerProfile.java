package com.example.revhirehiringplatform.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "job_seeker_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobSeekerProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @JsonIgnore
    @OneToMany(mappedBy = "jobSeeker", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private java.util.List<Application> applications;

    @JsonIgnore
    @OneToMany(mappedBy = "jobSeeker", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private java.util.List<SavedJobs> savedJobs;

    @JsonIgnore
    @OneToMany(mappedBy = "jobSeeker", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private java.util.List<SeekerSkillMap> skills;

    @JsonIgnore
    @OneToMany(mappedBy = "jobSeeker", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private java.util.List<ResumeFiles> resumes;

    @JsonIgnore
    @OneToMany(mappedBy = "jobSeeker", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private java.util.List<SavedResume> savedByEmployers;

    @JsonIgnore
    @OneToOne(mappedBy = "jobSeeker", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private ResumeText resumeText;

    private String location;

    @Column(name = "employment_status")
    private String employmentStatus;

    private String headline;
    private String summary;

    @Column(name = "profile_image", columnDefinition = "MEDIUMTEXT")
    private String profileImage;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}