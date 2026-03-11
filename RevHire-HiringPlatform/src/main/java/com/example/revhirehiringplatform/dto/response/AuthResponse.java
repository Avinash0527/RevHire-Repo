package com.example.revhirehiringplatform.dto.response;

import com.example.revhirehiringplatform.model.User.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;
    private String refreshToken;
    private String name;
    private String email;
    private String phone;
    private Role role;
    private Long id;
}
