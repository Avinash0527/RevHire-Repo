package com.example.revhirehiringplatform.dto.response;

import lombok.Data;

@Data

public class CompanyResponse {
    private Long id;
    private String name;
    private String description;
    private String website;
    private String location;
    private String industry;
    private String size;

    private String userName;
    private String userEmail;
    private String userPhone;
    private String logo;
}