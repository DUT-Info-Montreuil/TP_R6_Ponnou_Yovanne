package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.model;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class TokenStore {

    private static final ConcurrentHashMap<String, Long> tokens = new ConcurrentHashMap<>();

    private TokenStore() {
    }

    public static String generateToken(Long userId) {
        String token = UUID.randomUUID().toString();
        tokens.put(token, userId);
        return token;
    }

    public static Optional<Long> getUserId(String token) {
        return Optional.ofNullable(tokens.get(token));
    }

    public static void removeToken(String token) {
        tokens.remove(token);
    }
}
