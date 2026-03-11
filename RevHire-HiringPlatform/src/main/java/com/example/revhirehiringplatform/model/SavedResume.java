package com.example.revhirehiringplatform.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "saved_resumes", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "employer_id", "job_seeker_id", "job_post_id" })
})
@Data
public class SavedResume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employer_id", nullable = false)
    private User employer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_seeker_id", nullable = false)
    private JobSeekerProfile jobSeeker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_post_id", nullable = false)
    private JobPost jobPost;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime savedAt;
}
