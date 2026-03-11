package com.example.revhirehiringplatform.repository;

import com.example.revhirehiringplatform.model.EmployerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployerProfileRepository extends JpaRepository<EmployerProfile, Long> {
    Optional<EmployerProfile> findByUserId(Long userId);

    java.util.List<EmployerProfile> findByCompanyId(Long companyId);
}
