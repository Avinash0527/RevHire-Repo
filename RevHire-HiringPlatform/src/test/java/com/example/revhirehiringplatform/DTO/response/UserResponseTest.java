package com.example.revhirehiringplatform.DTO.response;

import com.example.revhirehiringplatform.dto.response.UserResponse;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UserResponseTest {
    @Test
    public void testGettersAndSetters() {
        UserResponse response = new UserResponse();
        assertNotNull(response);
    }
}
