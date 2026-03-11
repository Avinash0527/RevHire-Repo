package com.example.revhirehiringplatform.dto.response;

import lombok.Data;
import java.time.LocalDate;

@Data
public class JobPostResponse {
    private Long id;
    private String title;
    private String description;
    private String responsibilities;
    private String requirements;
    private String skills;
    private String location;
    private String salary;
    private String jobType;
    private LocalDate deadline;
    private Integer experienceYears;
    private LocalDate postedDate;
    private Long companyId;
    private String companyName;
    private String education;
    private Integer openings;
    private String status;
    private Long applicantCount;
    private String companyLogo;
}
