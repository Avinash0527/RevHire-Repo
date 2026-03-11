package com.example.revhirehiringplatform.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ApplicationNoteResponse {
    private Long id;
    private String noteText;
    private Long createdByUserId;
    private String createdByUserName;
    private LocalDateTime createdAt;
}