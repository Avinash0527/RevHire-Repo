package com.example.revhirehiringplatform.service;

import com.example.revhirehiringplatform.model.RefreshToken;
import com.example.revhirehiringplatform.repository.RefreshTokenRepository;
import com.example.revhirehiringplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final Long refreshTokenDurationMs = 2592000000L;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken createRefreshToken(Long userId) {
        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setUser(userRepository.findById(userId).get());
        refreshToken.setExpiresAt(LocalDateTime.now().plusSeconds(refreshTokenDurationMs / 1000));
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setRevoked(false);

        refreshToken = refreshTokenRepository.save(refreshToken);
        log.info("Refresh token created for user: {}", userId);
        return refreshToken;
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(token);
            log.warn("Refresh token expired for user: {}", token.getUser().getId());
            throw new RuntimeException("Refresh token was expired. Please make a new signin request");
        }

        if (token.isRevoked()) {
            log.warn("Refresh token is revoked for user: {}", token.getUser().getId());
            throw new RuntimeException("Refresh token is revoked. Please make a new signin request");
        }

        return token;
    }

    @Transactional
    public int deleteByUserId(Long userId) {
        log.info("Deleting refresh tokens for user: {}", userId);
        return refreshTokenRepository.deleteByUser(userRepository.findById(userId).get());
    }
}
