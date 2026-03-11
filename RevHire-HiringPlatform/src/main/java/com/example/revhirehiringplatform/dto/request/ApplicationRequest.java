package com.example.revhirehiringplatform.dto.request;

import lombok.Data;

@Data
public class ApplicationRequest {
    private Long jobId;
    private String coverLetter;
    private Long resumeFileId;
}
