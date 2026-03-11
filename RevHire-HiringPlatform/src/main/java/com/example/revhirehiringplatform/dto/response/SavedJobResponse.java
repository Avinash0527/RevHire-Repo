package com.example.revhirehiringplatform.dto.response;

import lombok.Data;

@Data
public class SavedJobResponse {
    private Long id;
    private Long jobId;
    private String jobTitle;
    private String companyName;
    private String jobLocation;
    private String companyLogo;
}
