package com.example.revhirehiringplatform.service;

import com.example.revhirehiringplatform.model.RefreshToken;
import com.example.revhirehiringplatform.model.User;
import com.example.revhirehiringplatform.repository.RefreshTokenRepository;
import com.example.revhirehiringplatform.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private User user;
    private RefreshToken refreshToken;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("user@test.com");

        refreshToken = new RefreshToken();
        refreshToken.setId(10L);
        refreshToken.setUser(user);
        refreshToken.setToken("some-uuid-token");
        refreshToken.setRevoked(false);
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(10));
    }

    @Test
    void testFindByToken() {
        when(refreshTokenRepository.findByToken("some-uuid-token")).thenReturn(Optional.of(refreshToken));

        Optional<RefreshToken> result = refreshTokenService.findByToken("some-uuid-token");

        assertTrue(result.isPresent());
        assertEquals("some-uuid-token", result.get().getToken());
    }

    @Test
    void testCreateRefreshToken() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArguments()[0]);

        RefreshToken createdToken = refreshTokenService.createRefreshToken(1L);

        assertNotNull(createdToken);
        assertEquals(user, createdToken.getUser());
        assertFalse(createdToken.isRevoked());
        assertNotNull(createdToken.getToken());
        assertNotNull(createdToken.getExpiresAt());
    }

    @Test
    void testVerifyExpiration_Valid() {
        RefreshToken result = refreshTokenService.verifyExpiration(refreshToken);

        assertNotNull(result);
        assertEquals(refreshToken.getToken(), result.getToken());
    }

    @Test
    void testVerifyExpiration_Expired() {
        refreshToken.setExpiresAt(LocalDateTime.now().minusDays(1)); // Expired

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            refreshTokenService.verifyExpiration(refreshToken);
        });

        assertEquals("Refresh token was expired. Please make a new signin request", exception.getMessage());
        verify(refreshTokenRepository, times(1)).delete(refreshToken);
    }

    @Test
    void testVerifyExpiration_Revoked() {
        refreshToken.setRevoked(true); // Revoked

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            refreshTokenService.verifyExpiration(refreshToken);
        });

        assertEquals("Refresh token is revoked. Please make a new signin request", exception.getMessage());
    }

    @Test
    void testDeleteByUserId() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(refreshTokenRepository.deleteByUser(user)).thenReturn(2);

        int deletedCount = refreshTokenService.deleteByUserId(1L);

        assertEquals(2, deletedCount);
        verify(userRepository, times(1)).findById(1L);
        verify(refreshTokenRepository, times(1)).deleteByUser(user);
    }
}

