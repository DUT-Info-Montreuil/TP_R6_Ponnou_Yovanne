package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.model;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class TokenStore {

    private static final ConcurrentHashMap<String, TokenInfo> tokens = new ConcurrentHashMap<>();

    private TokenStore() {
    }

    public static String generateToken(Long userId, String username) {
        String token = UUID.randomUUID().toString();
        tokens.put(token, new TokenInfo(userId, username));
        return token;
    }

    public static Optional<TokenInfo> getTokenInfo(String token) {
        return Optional.ofNullable(tokens.get(token));
    }

    public static Optional<Long> getUserId(String token) {
        return getTokenInfo(token).map(TokenInfo::getUserId);
    }

    public static void removeToken(String token) {
        tokens.remove(token);
    }

    public static class TokenInfo {
        private final Long userId;
        private final String username;

        public TokenInfo(Long userId, String username) {
            this.userId = userId;
            this.username = username;
        }

        public Long getUserId() {
            return userId;
        }

        public String getUsername() {
            return username;
        }
    }
}
