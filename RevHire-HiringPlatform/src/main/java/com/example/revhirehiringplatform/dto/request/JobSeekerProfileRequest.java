package com.example.revhirehiringplatform.dto.request;

import lombok.Data;

@Data

public class JobSeekerProfileRequest {
    private String headline;
    private String summary;
    private String location;
    private String phone;
    private String employmentStatus;

    private String title;
    private String objective;
    private String education;
    private String experience;
    private String skills;
    private String projects;
    private String certifications;
    private String profileImage;
}
