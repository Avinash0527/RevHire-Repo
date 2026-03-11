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
@Table(name = "applications", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "job_id", "seeker_id" })
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private JobPost jobPost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seeker_id", nullable = false)
    private JobSeekerProfile jobSeeker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_file_id")
    private ResumeFiles resumeFile;

    @Column(name = "cover_letter", columnDefinition = "TEXT")
    private String coverLetter;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private ApplicationStatus status = ApplicationStatus.APPLIED;

    @JsonIgnore
    @OneToMany(mappedBy = "application", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private java.util.List<ApplicationNotes> applicationNotes;

    @JsonIgnore
    @OneToMany(mappedBy = "application", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private java.util.List<ApplicationStatusHistory> statusHistory;

    @Column(name = "withdraw_reason")
    private String withdrawReason;

    @CreationTimestamp
    @Column(name = "applied_at", updatable = false)
    private LocalDateTime appliedAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum ApplicationStatus {
        APPLIED, REVIEWING, SHORTLISTED, SELECTED, REJECTED, WITHDRAWN
    }
}