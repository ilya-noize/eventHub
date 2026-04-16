package com.auth.domain;

import com.auth.db.RefreshToken;
import com.auth.db.RefreshTokenRepository;
import com.auth.db.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenService {

    @Value("${jwt.refresh-expiration-min}")
    private long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshToken createRefreshToken(UserEntity user) {
        RefreshToken token = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(TimeUnit.MINUTES.toMillis(refreshTokenDurationMs)))
                .build();
        if (refreshTokenRepository.existsByUser(user)) {
            deleteByUserId(user.getId());
        }

        return refreshTokenRepository.save(token);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new IllegalStateException("Refresh accessToken expired");
        }
        return token;
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public void deleteByUserId(Long userId) {
        refreshTokenRepository.deleteByUser_Id(userId);
    }
}