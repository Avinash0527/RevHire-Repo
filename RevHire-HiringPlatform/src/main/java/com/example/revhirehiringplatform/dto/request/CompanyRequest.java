package com.example.revhirehiringplatform.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class CompanyRequest {
    private Long id;

    @NotBlank(message = "Company Name is required")
    private String name;

    private String description;
    private String website;
    private String location;
    private String industry;
    private String size;

    private String userName;
    @Email(message = "Invalid email format")
    private String userEmail;
    private String userPhone;
    private String logo;
}