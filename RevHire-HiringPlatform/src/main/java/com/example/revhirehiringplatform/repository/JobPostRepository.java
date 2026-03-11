package com.example.revhirehiringplatform.repository;

import com.example.revhirehiringplatform.model.JobPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import com.example.revhirehiringplatform.model.User;


@Repository
public interface JobPostRepository extends JpaRepository<JobPost, Long> {
        List<JobPost> findByCreatedBy(User user);

        List<JobPost> findByCompanyId(Long companyId);

        List<JobPost> findByTitleContainingIgnoreCaseOrLocationContainingIgnoreCase(String title, String location);

        @org.springframework.data.jpa.repository.Query("SELECT DISTINCT j FROM JobPost j LEFT JOIN JobSkillMap m ON j.id = m.jobPost.id WHERE "
                +
                "(:title IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
                "(:location IS NULL OR LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
                "(:experience IS NULL OR j.experienceYears <= :experience) AND " +
                "(:company IS NULL OR LOWER(j.company.name) LIKE LOWER(CONCAT('%', :company, '%'))) AND " +
                "(:salary IS NULL OR j.salaryMin >= :salary) AND " +
                "(:useTypeFilter = FALSE OR j.jobType IN :jobTypes) AND " +
                "(:startDate IS NULL OR j.createdAt >= :startDate) AND " +
                "j.status = 'ACTIVE' ORDER BY j.createdAt DESC")
        List<JobPost> findByFilters(
                @org.springframework.data.repository.query.Param("title") String title,
                @org.springframework.data.repository.query.Param("location") String location,
                @org.springframework.data.repository.query.Param("experience") Integer experience,
                @org.springframework.data.repository.query.Param("company") String company,
                @org.springframework.data.repository.query.Param("salary") Double salary,
                @org.springframework.data.repository.query.Param("jobTypes") List<String> jobTypes,
                @org.springframework.data.repository.query.Param("useTypeFilter") boolean useTypeFilter,
                @org.springframework.data.repository.query.Param("startDate") java.time.LocalDateTime startDate);

        @org.springframework.data.jpa.repository.Query("SELECT DISTINCT j FROM JobPost j LEFT JOIN JobSkillMap m ON j.id = m.jobPost.id WHERE "
                +
                "((LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) OR " +
                "(LOWER(j.company.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) OR " +
                "(LOWER(m.skill.skillName) LIKE LOWER(CONCAT('%', :keyword, '%')))) AND " +
                "j.status = 'ACTIVE' ORDER BY j.createdAt DESC")
        List<JobPost> searchByKeyword(@org.springframework.data.repository.query.Param("keyword") String keyword);

        long countByStatus(JobPost.JobStatus status);
}
