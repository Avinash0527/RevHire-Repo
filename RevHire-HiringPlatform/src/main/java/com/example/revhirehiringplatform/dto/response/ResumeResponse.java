package com.example.revhirehiringplatform.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ResumeResponse {
    private Long id;
    private String fileType;
    private String fileName;
    private byte[] content;
    private String objective;
    private String education;
    private String experience;
    private String skills;
    private String projects;
    private String certifications;
    private LocalDateTime updatedAt;
}
