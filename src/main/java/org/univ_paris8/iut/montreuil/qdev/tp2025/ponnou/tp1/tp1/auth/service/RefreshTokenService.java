package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.model.RefreshToken;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.repository.RefreshTokenRepository;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.model.User;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-expiration:604800000}")
    private long refreshExpirationMs;

    @Transactional
    public RefreshToken createRefreshToken(User user) {
        refreshTokenRepository.revokeAllByUserId(user.getId());

        String tokenValue = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plusMillis(refreshExpirationMs);
        RefreshToken refreshToken = new RefreshToken(tokenValue, user, expiresAt);
        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional(readOnly = true)
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Transactional
    public void revokeAllByUser(Long userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
    }
}
