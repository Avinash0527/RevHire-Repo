package com.example.revhirehiringplatform.DTO.request;

import com.example.revhirehiringplatform.dto.request.UserLoginRequest;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


public class UserLoginRequestTest {
    @Test
    public void testGettersAndSetters() {
        UserLoginRequest request = new UserLoginRequest();
        assertNotNull(request);
    }
}