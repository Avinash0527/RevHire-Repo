package com.example.revhirehiringplatform.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class JobPostRequest {
    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    private String responsibilities;
    private String requirements;
    private String skills;
    private String location;

    private String salary;

    private String jobType;

    @NotNull(message = "Deadline is required")
    @Future(message = "Deadline must be in the future")
    private LocalDate deadline;

    private Integer experienceYears;
    private Long companyId;
    private String education;
    private Integer openings;
    private String status;
}