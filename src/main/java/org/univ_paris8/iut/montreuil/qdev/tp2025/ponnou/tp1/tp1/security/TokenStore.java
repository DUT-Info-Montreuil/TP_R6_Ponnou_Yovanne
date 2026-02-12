package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.security;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stockage en memoire des tokens d'authentification.
 * Chaque token est associe a l'identifiant de l'utilisateur.
 *
 * Approche stateless : le client recoit un token opaque a la connexion
 * et le renvoie dans le header Authorization a chaque requete.
 * Le serveur ne maintient aucune session HTTP.
 */
public final class TokenStore {

    private static final ConcurrentHashMap<String, Long> tokens = new ConcurrentHashMap<>();

    private TokenStore() {
    }

    /**
     * Genere un token unique et l'associe a un utilisateur.
     */
    public static String generateToken(Long userId) {
        String token = UUID.randomUUID().toString();
        tokens.put(token, userId);
        return token;
    }

    /**
     * Retrouve l'identifiant utilisateur associe a un token.
     */
    public static Optional<Long> getUserId(String token) {
        return Optional.ofNullable(tokens.get(token));
    }

    /**
     * Supprime un token (deconnexion).
     */
    public static void removeToken(String token) {
        tokens.remove(token);
    }
}
