package com.example.revhirehiringplatform.repository;

import com.example.revhirehiringplatform.model.SeekerSkillMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeekerSkillMapRepository extends JpaRepository<SeekerSkillMap, Long> {
    List<SeekerSkillMap> findByJobSeekerId(Long jobSeekerId);

    List<SeekerSkillMap> findBySkillId(Long skillId);
}
