package com.example.revhirehiringplatform.repository;

import com.example.revhirehiringplatform.model.ApplicationStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationStatusHistoryRepository extends JpaRepository<ApplicationStatusHistory, Long> {
    List<ApplicationStatusHistory> findByApplicationIdOrderByChangedAtDesc(Long applicationId);

    List<ApplicationStatusHistory> findByChangedBy_Id(Long changedById);
}
