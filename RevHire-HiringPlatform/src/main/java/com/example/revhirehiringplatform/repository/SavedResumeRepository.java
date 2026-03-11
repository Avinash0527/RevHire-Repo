package com.example.revhirehiringplatform.repository;


import com.example.revhirehiringplatform.model.SavedResume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedResumeRepository extends JpaRepository<SavedResume, Long> {

    List<SavedResume> findByEmployerId(Long employerId);

    @org.springframework.data.jpa.repository.Query("SELECT s FROM SavedResume s WHERE s.employer.id = :employerId AND s.jobSeeker.id = :seekerId AND s.jobPost.id = :jobId")
    Optional<SavedResume> findByEmployerIdAndJobSeekerIdAndJobPostId(
            @org.springframework.data.repository.query.Param("employerId") Long employerId,
            @org.springframework.data.repository.query.Param("seekerId") Long seekerId,
            @org.springframework.data.repository.query.Param("jobId") Long jobId);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(s) > 0 FROM SavedResume s WHERE s.employer.id = :employerId AND s.jobSeeker.id = :seekerId AND s.jobPost.id = :jobId")
    boolean existsByEmployerIdAndJobSeekerIdAndJobPostId(
            @org.springframework.data.repository.query.Param("employerId") Long employerId,
            @org.springframework.data.repository.query.Param("seekerId") Long seekerId,
            @org.springframework.data.repository.query.Param("jobId") Long jobId);
}