package com.example.revhirehiringplatform.dto.response;
import lombok.Data;

@Data
public class EmployerProfileResponse {
    private String name;
    private String email;
    private String jobTitle;
    private String department;
    private Long companyId;
    private String companyName;
}
