package com.example.revhirehiringplatform.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class JobSeekerProfileResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String headline;
    private String summary;
    private String location;
    private String employmentStatus;


    private String title;
    private String objective;
    private String education;
    private String experience;
    private String skills;
    private String projects;
    private String certifications;
    private String appliedRole;
    private Long jobId;

    private List<SkillResponse> skillsList;


    private boolean resumeUploaded;
    private boolean profileSummarySet;
    private boolean skillsSet;
    private boolean experienceSet;
    private boolean educationSet;
    private int completionPercentage;
}