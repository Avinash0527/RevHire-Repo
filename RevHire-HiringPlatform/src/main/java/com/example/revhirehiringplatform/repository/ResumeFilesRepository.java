package com.example.revhirehiringplatform.repository;

import com.example.revhirehiringplatform.model.ResumeFiles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ResumeFilesRepository extends JpaRepository<ResumeFiles, Long> {
    List<ResumeFiles> findByJobSeekerId(Long seekerId);
}
