package com.example.revhirehiringplatform.dto.request;

import lombok.Data;

@Data
public class EmployerProfileRequest {
    private String jobTitle;
    private String department;
    private Long companyId;
}
