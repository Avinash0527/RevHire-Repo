package com.example.revhirehiringplatform.dto.request;

import lombok.Data;

@Data
public class ResumeTextRequest {
    private String title;
    private String objective;
    private String education;
    private String experience;
    private String skills;
    private String projects;
    private String certifications;
}
