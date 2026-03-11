package com.example.revhirehiringplatform.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
    }

    @Test
    void testGenerateAndValidateJwtToken() {
        Authentication authentication = mock(Authentication.class);
        UserDetailsImpl userDetails = new UserDetailsImpl(1L, "Test", "test@test.com", "999", "password",
                com.example.revhirehiringplatform.model.User.Role.JOB_SEEKER, null);

        when(authentication.getPrincipal()).thenReturn(userDetails);

        String token = jwtUtil.generateJwtToken(authentication);

        assertNotNull(token);
        assertTrue(jwtUtil.validateJwtToken(token));
        assertEquals("Test", jwtUtil.getUserNameFromJwtToken(token));
    }

    @Test
    void testGenerateTokenFromEmail() {
        String token = jwtUtil.generateTokenFromEmail("test@test.com");
        assertNotNull(token);
        assertTrue(jwtUtil.validateJwtToken(token));
        assertEquals("test@test.com", jwtUtil.getUserNameFromJwtToken(token));
    }

    @Test
    void testInvalidJwtToken() {
        assertFalse(jwtUtil.validateJwtToken("invalid-token"));
        assertFalse(jwtUtil.validateJwtToken(jwtUtil.generateTokenFromEmail("test").substring(0, 10)));
    }
}
