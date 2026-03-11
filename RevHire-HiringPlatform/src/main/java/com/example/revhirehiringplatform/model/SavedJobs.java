package com.example.revhirehiringplatform.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "saved_jobs", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "seeker_id", "job_id" })
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SavedJobs {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seeker_id", nullable = false)
    private JobSeekerProfile jobSeeker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private JobPost jobPost;

    @CreationTimestamp
    @Column(name = "saved_at", updatable = false)
    private LocalDateTime savedAt;
}
