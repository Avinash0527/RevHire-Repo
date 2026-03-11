package com.example.revhirehiringplatform.repository;

import com.example.revhirehiringplatform.model.JobSeekerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobSeekerProfileRepository extends JpaRepository<JobSeekerProfile, Long> {
    Optional<JobSeekerProfile> findByUserId(Long userId);

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT p FROM JobSeekerProfile p LEFT JOIN SeekerSkillMap s ON p.id = s.jobSeeker.id WHERE "
            +
            "LOWER(p.user.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.skill.skillName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<JobSeekerProfile> searchByKeyword(
            @org.springframework.data.repository.query.Param("keyword") String keyword);
}