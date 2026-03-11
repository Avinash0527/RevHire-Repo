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
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @OneToOne(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private JobSeekerProfile jobSeekerProfile;

    @JsonIgnore
    @OneToOne(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private EmployerProfile employerProfile;

    @JsonIgnore
    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private java.util.List<Company> companiesCreated;

    @JsonIgnore
    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private java.util.List<JobPost> jobPosts;

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private java.util.List<Notification> notifications;

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private java.util.List<RefreshToken> refreshTokens;

    @JsonIgnore
    @OneToOne(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private PasswordResetToken passwordResetToken;

    @JsonIgnore
    @OneToMany(mappedBy = "employer", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private java.util.List<SavedResume> savedResumes;

    @JsonIgnore
    @OneToMany(mappedBy = "changedBy", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private java.util.List<AuditLog> auditLogs;

    @JsonIgnore
    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private java.util.List<ApplicationNotes> applicationNotes;

    @JsonIgnore
    @OneToMany(mappedBy = "changedBy", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private java.util.List<ApplicationStatusHistory> applicationStatusChanges;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    private String phone;

    @Column(name = "password_hash", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private Boolean status = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum Role {
        JOB_SEEKER, EMPLOYER, ADMIN
    }
}