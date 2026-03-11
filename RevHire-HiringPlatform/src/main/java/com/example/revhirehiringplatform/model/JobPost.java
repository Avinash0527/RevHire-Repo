package com.example.revhirehiringplatform.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobPost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @JsonIgnore
    @OneToMany(mappedBy = "jobPost", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private java.util.List<Application> applications;

    @JsonIgnore
    @OneToMany(mappedBy = "jobPost", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private java.util.List<SavedJobs> savedJobs;

    @JsonIgnore
    @OneToMany(mappedBy = "jobPost", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private java.util.List<JobSkillMap> skills;

    @JsonIgnore
    @OneToMany(mappedBy = "jobPost", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private java.util.List<SavedResume> savedResumes;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String responsibilities;

    @Column(name = "requirements_text", columnDefinition = "TEXT")
    private String requirements;

    @Column(name = "experience_years")
    private Integer experienceYears;

    private String education;
    private String location;

    @Column(name = "salary_min")
    private Double salaryMin;

    @Column(name = "salary_max")
    private Double salaryMax;

    @Column(name = "job_type")
    private String jobType;

    private LocalDate deadline;
    private Integer openings;

    @Enumerated(EnumType.STRING)
    private JobStatus status = JobStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum JobStatus {
        ACTIVE, CLOSED, FILLED
    }
}
