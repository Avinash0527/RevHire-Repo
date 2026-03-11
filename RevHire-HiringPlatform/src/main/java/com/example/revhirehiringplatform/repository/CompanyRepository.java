package com.example.revhirehiringplatform.repository;

import com.example.revhirehiringplatform.model.Company;
import com.example.revhirehiringplatform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    List<Company> findByCreatedBy(User user);

    List<Company> findByCreatedByOrderByNameAsc(User user);
}
