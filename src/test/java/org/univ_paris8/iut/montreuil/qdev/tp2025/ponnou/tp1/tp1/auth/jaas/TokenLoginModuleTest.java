package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.jaas;

import org.junit.jupiter.api.*;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.model.TokenStore;

import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires TokenLoginModule (token valide / invalide / expiré)
 */
class TokenLoginModuleTest {

    private TokenLoginModule loginModule;
    private Subject subject;

    @BeforeEach
    void setUp() {
        loginModule = new TokenLoginModule();
        subject = new Subject();
    }

    private CallbackHandler tokenCallbackHandler(String token) {
        return callbacks -> {
            for (Callback callback : callbacks) {
                if (callback instanceof NameCallback) {
                    ((NameCallback) callback).setName(token);
                }
            }
        };
    }

    @Test
    @DisplayName("login() réussit avec un token valide")
    void login_shouldSucceed_whenTokenValid() throws LoginException {
        String token = TokenStore.generateToken(1L, "alice");

        loginModule.initialize(subject, tokenCallbackHandler(token), Map.of(), Map.of());

        boolean result = loginModule.login();

        assertTrue(result);

        TokenStore.removeToken(token);
    }

    @Test
    @DisplayName("login() lève LoginException avec un token invalide")
    void login_shouldThrowLoginException_whenTokenInvalid() {
        loginModule.initialize(subject, tokenCallbackHandler("invalid-token-xyz"), Map.of(), Map.of());

        LoginException ex = assertThrows(LoginException.class, () -> loginModule.login());
        assertTrue(ex.getMessage().contains("invalide"));
    }

    @Test
    @DisplayName("login() lève LoginException avec un token expiré (supprimé)")
    void login_shouldThrowLoginException_whenTokenExpired() {
        String token = TokenStore.generateToken(1L, "bob");
        TokenStore.removeToken(token);

        loginModule.initialize(subject, tokenCallbackHandler(token), Map.of(), Map.of());

        LoginException ex = assertThrows(LoginException.class, () -> loginModule.login());
        assertTrue(ex.getMessage().contains("invalide") || ex.getMessage().contains("expiré"));
    }

    @Test
    @DisplayName("login() lève LoginException avec un token vide")
    void login_shouldThrowLoginException_whenTokenBlank() {
        loginModule.initialize(subject, tokenCallbackHandler("   "), Map.of(), Map.of());

        LoginException ex = assertThrows(LoginException.class, () -> loginModule.login());
        assertTrue(ex.getMessage().contains("manquant"));
    }

    @Test
    @DisplayName("login() lève LoginException avec un token null")
    void login_shouldThrowLoginException_whenTokenNull() {
        CallbackHandler nullTokenHandler = callbacks -> {
            for (Callback callback : callbacks) {
                if (callback instanceof NameCallback) {
                    // ne pas appeler setName -> getName() retourne null
                }
            }
        };

        loginModule.initialize(subject, nullTokenHandler, Map.of(), Map.of());

        assertThrows(LoginException.class, () -> loginModule.login());
    }

    @Test
    @DisplayName("commit() ajoute UserPrincipal et RolePrincipal au Subject")
    void commit_shouldAddPrincipals_afterSuccessfulLogin() throws LoginException {
        String token = TokenStore.generateToken(5L, "charlie");

        loginModule.initialize(subject, tokenCallbackHandler(token), Map.of(), Map.of());
        loginModule.login();
        loginModule.commit();

        Set<UserPrincipal> users = subject.getPrincipals(UserPrincipal.class);
        Set<RolePrincipal> roles = subject.getPrincipals(RolePrincipal.class);

        assertEquals(1, users.size());
        assertEquals("charlie", users.iterator().next().getUsername());
        assertEquals(5L, users.iterator().next().getUserId());

        assertEquals(1, roles.size());
        assertEquals("ROLE_USER", roles.iterator().next().getName());

        TokenStore.removeToken(token);
    }

    @Test
    @DisplayName("commit() retourne false si login n'a pas été appelé")
    void commit_shouldReturnFalse_whenNotAuthenticated() throws LoginException {
        loginModule.initialize(subject, tokenCallbackHandler("x"), Map.of(), Map.of());

        boolean result = loginModule.commit();

        assertFalse(result);
        assertTrue(subject.getPrincipals().isEmpty());
    }

    @Test
    @DisplayName("logout() supprime les principals du Subject")
    void logout_shouldRemovePrincipals() throws LoginException {
        String token = TokenStore.generateToken(1L, "dave");

        loginModule.initialize(subject, tokenCallbackHandler(token), Map.of(), Map.of());
        loginModule.login();
        loginModule.commit();

        assertFalse(subject.getPrincipals().isEmpty());

        loginModule.logout();

        assertTrue(subject.getPrincipals(UserPrincipal.class).isEmpty());
        assertTrue(subject.getPrincipals(RolePrincipal.class).isEmpty());

        TokenStore.removeToken(token);
    }

    @Test
    @DisplayName("login() lève LoginException si le CallbackHandler échoue")
    void login_shouldThrowLoginException_whenCallbackHandlerFails() {
        CallbackHandler failingHandler = callbacks -> {
            throw new IOException("Callback error");
        };

        loginModule.initialize(subject, failingHandler, Map.of(), Map.of());

        LoginException ex = assertThrows(LoginException.class, () -> loginModule.login());
        assertTrue(ex.getMessage().contains("récupération du token"));
    }

    @Test
    @DisplayName("abort() nettoie l'état interne")
    void abort_shouldCleanup() throws LoginException {
        loginModule.initialize(subject, tokenCallbackHandler("x"), Map.of(), Map.of());

        boolean result = loginModule.abort();

        assertTrue(result);
    }
}
