package com.example.revhirehiringplatform.DTO.request;

import com.example.revhirehiringplatform.dto.request.UpdatePasswordRequest;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UpdatePasswordRequestTest {
    @Test
    public void testGettersAndSetters() {
        UpdatePasswordRequest request = new UpdatePasswordRequest();
        assertNotNull(request);
    }
}
