package com.example.javamusic.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.javamusic.model.RefreshToken;
import com.example.javamusic.repository.RefreshTokenRepository;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Value("${app.jwtRefreshExpirationMs}")
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    // Create and save a refresh token
    public String createRefreshToken(String email) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setUserEmail(email);
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));

        refreshTokenRepository.save(refreshToken);
        return refreshToken.getToken();
    }

    // Validate a refresh token
    public Optional<String> validateRefreshToken(String token) {
        Optional<RefreshToken> refreshToken = refreshTokenRepository.findByToken(token);
        if (refreshToken.isPresent() && refreshToken.get().getExpiryDate().isAfter(Instant.now())) {
            return Optional.of(token);
        }else if(refreshToken.isPresent() && !refreshToken.get().getExpiryDate().isAfter(Instant.now())){
            // Trường hợp token đã hết hạn -> Xóa token
            refreshTokenRepository.delete(refreshToken.get());
        }
        return Optional.empty();
    }

    // Revoke a refresh token
    public void revokeRefreshToken(String email) {
        refreshTokenRepository.deleteByUserEmail(email);
    }
}
