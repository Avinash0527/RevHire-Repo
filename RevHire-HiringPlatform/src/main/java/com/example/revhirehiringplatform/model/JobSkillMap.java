package com.example.revhirehiringplatform.model;



import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "job_skill_map")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobSkillMap {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private JobPost jobPost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", nullable = false)
    private SkillsMaster skill;

    @Column(name = "is_mandatory")
    private boolean isMandatory = false;
}
