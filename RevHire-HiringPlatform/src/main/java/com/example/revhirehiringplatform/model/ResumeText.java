package com.example.revhirehiringplatform.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "resume_text")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumeText {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "seeker_id", referencedColumnName = "id", nullable = false, unique = true)
    private JobSeekerProfile jobSeeker;

    @Column(name = "title")
    private String title;

    @Column(columnDefinition = "TEXT")
    private String objective;

    @Column(name = "education_text", columnDefinition = "TEXT")
    private String educationText;

    @Column(name = "experience_text", columnDefinition = "TEXT")
    private String experienceText;

    @Column(name = "skills_text", columnDefinition = "TEXT")
    private String skillsText;

    @Column(name = "projects_text", columnDefinition = "TEXT")
    private String projectsText;

    @Column(name = "certifications_text", columnDefinition = "TEXT")
    private String certificationsText;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
