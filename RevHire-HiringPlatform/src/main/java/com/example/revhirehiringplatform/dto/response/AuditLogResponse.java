package com.example.revhirehiringplatform.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AuditLogResponse {
    private Long id;
    private String entityType;
    private Long entityId;
    private String action;
    private String oldValue;
    private String newValue;
    private String changedByName;
    private LocalDateTime changedAt;
    private String details;
}
