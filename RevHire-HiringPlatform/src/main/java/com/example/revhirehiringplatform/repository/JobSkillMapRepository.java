package com.example.revhirehiringplatform.repository;

import com.example.revhirehiringplatform.model.JobSkillMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobSkillMapRepository extends JpaRepository<JobSkillMap, Long> {
    List<JobSkillMap> findByJobPostId(Long jobPostId);

    List<JobSkillMap> findBySkillId(Long skillId);
}
