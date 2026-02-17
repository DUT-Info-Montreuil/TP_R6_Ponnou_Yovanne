package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.jaas;

import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.model.TokenStore;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.config.EntityManagerUtil;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.service.UserService;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

/**
 * Tests unitaires DbLoginModule (H2 + mock EntityManagerUtil)
 */
class DbLoginModuleTest {

    private static EntityManagerFactory emf;
    private static MockedStatic<EntityManagerUtil> mockedUtil;

    private DbLoginModule loginModule;
    private Subject subject;

    @BeforeAll
    static void setUpAll() {
        emf = Persistence.createEntityManagerFactory("MasterAnnoncePU");
        mockedUtil = mockStatic(EntityManagerUtil.class);
        mockedUtil.when(EntityManagerUtil::getEntityManager).thenAnswer(inv -> emf.createEntityManager());
    }

    @AfterAll
    static void tearDownAll() {
        if (mockedUtil != null) mockedUtil.close();
        if (emf != null) emf.close();
    }

    @BeforeEach
    void setUp() {
        loginModule = new DbLoginModule();
        subject = new Subject();

        UserService userService = new UserService();
        userService.create("alice", "alice@test.com", "password123");
    }

    @AfterEach
    void cleanDb() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.createQuery("DELETE FROM User").executeUpdate();
        em.getTransaction().commit();
        em.close();
    }

    private CallbackHandler callbackHandler(String username, String password) {
        return callbacks -> {
            for (Callback callback : callbacks) {
                if (callback instanceof NameCallback) {
                    ((NameCallback) callback).setName(username);
                } else if (callback instanceof PasswordCallback) {
                    ((PasswordCallback) callback).setPassword(password.toCharArray());
                }
            }
        };
    }

    @Test
    @DisplayName("login() réussit avec des identifiants valides")
    void login_shouldSucceed_whenCredentialsValid() throws LoginException {
        loginModule.initialize(subject, callbackHandler("alice", "password123"), Map.of(), Map.of());

        boolean result = loginModule.login();

        assertTrue(result);
    }

    @Test
    @DisplayName("login() lève LoginException avec un mauvais mot de passe")
    void login_shouldThrowLoginException_whenWrongPassword() {
        loginModule.initialize(subject, callbackHandler("alice", "wrongpassword"), Map.of(), Map.of());

        assertThrows(LoginException.class, () -> loginModule.login());
    }

    @Test
    @DisplayName("login() lève LoginException avec un utilisateur inconnu")
    void login_shouldThrowLoginException_whenUnknownUser() {
        loginModule.initialize(subject, callbackHandler("unknown", "password123"), Map.of(), Map.of());

        assertThrows(LoginException.class, () -> loginModule.login());
    }

    @Test
    @DisplayName("commit() ajoute UserPrincipal, RolePrincipal et token au Subject")
    void commit_shouldAddPrincipalsAndToken_afterSuccessfulLogin() throws LoginException {
        loginModule.initialize(subject, callbackHandler("alice", "password123"), Map.of(), Map.of());
        loginModule.login();
        loginModule.commit();

        Set<UserPrincipal> users = subject.getPrincipals(UserPrincipal.class);
        Set<RolePrincipal> roles = subject.getPrincipals(RolePrincipal.class);

        assertEquals(1, users.size());
        assertEquals("alice", users.iterator().next().getUsername());
        assertNotNull(users.iterator().next().getUserId());

        assertEquals(1, roles.size());
        assertEquals("ROLE_USER", roles.iterator().next().getName());

        assertEquals(1, subject.getPublicCredentials(String.class).size());
        String token = subject.getPublicCredentials(String.class).iterator().next();
        assertTrue(TokenStore.getTokenInfo(token).isPresent());
    }

    @Test
    @DisplayName("commit() retourne false si login n'a pas été appelé")
    void commit_shouldReturnFalse_whenNotAuthenticated() throws LoginException {
        loginModule.initialize(subject, callbackHandler("x", "x"), Map.of(), Map.of());

        boolean result = loginModule.commit();

        assertFalse(result);
        assertTrue(subject.getPrincipals().isEmpty());
    }

    @Test
    @DisplayName("logout() supprime le token et nettoie le Subject")
    void logout_shouldRemoveTokenAndCleanSubject() throws LoginException {
        loginModule.initialize(subject, callbackHandler("alice", "password123"), Map.of(), Map.of());
        loginModule.login();
        loginModule.commit();

        String token = subject.getPublicCredentials(String.class).iterator().next();
        assertTrue(TokenStore.getTokenInfo(token).isPresent());

        loginModule.logout();

        assertTrue(subject.getPrincipals().isEmpty());
        assertTrue(subject.getPublicCredentials().isEmpty());
        assertTrue(TokenStore.getTokenInfo(token).isEmpty());
    }

    @Test
    @DisplayName("login() lève LoginException si le CallbackHandler échoue")
    void login_shouldThrowLoginException_whenCallbackHandlerFails() {
        CallbackHandler failingHandler = callbacks -> {
            throw new IOException("Callback error");
        };

        loginModule.initialize(subject, failingHandler, Map.of(), Map.of());

        LoginException ex = assertThrows(LoginException.class, () -> loginModule.login());
        assertTrue(ex.getMessage().contains("récupération des identifiants"));
    }

    @Test
    @DisplayName("abort() nettoie l'état interne")
    void abort_shouldCleanup() throws LoginException {
        loginModule.initialize(subject, callbackHandler("x", "x"), Map.of(), Map.of());

        boolean result = loginModule.abort();

        assertTrue(result);
    }
}
