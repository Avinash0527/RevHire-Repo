package com.example.revhirehiringplatform.repository;

import com.example.revhirehiringplatform.model.ApplicationNotes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationNotesRepository extends JpaRepository<ApplicationNotes, Long> {
    List<ApplicationNotes> findByApplicationId(Long applicationId);

    List<ApplicationNotes> findByCreatedBy_Id(Long authorId);
}
