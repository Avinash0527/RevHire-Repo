package com.example.revhirehiringplatform.dto.response;

import com.example.revhirehiringplatform.model.User.Role;
import lombok.Data;

@Data
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private Role role;
    private Boolean status;
}
