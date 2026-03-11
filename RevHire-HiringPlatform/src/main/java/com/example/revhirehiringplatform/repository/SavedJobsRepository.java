package com.example.revhirehiringplatform.repository;

import com.example.revhirehiringplatform.model.SavedJobs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedJobsRepository extends JpaRepository<SavedJobs, Long> {
    List<SavedJobs> findByJobSeekerId(Long seekerId);

    Optional<SavedJobs> findByJobSeekerIdAndJobPostId(Long seekerId, Long jobPostId);

    List<SavedJobs> findByJobPostId(Long jobPostId);
}