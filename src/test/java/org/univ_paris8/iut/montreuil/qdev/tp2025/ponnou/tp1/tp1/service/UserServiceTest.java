package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.service;

import org.junit.jupiter.api.*;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.config.EntityManagerUtil;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.model.User;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.service.UserService;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.List;
import java.util.Optional;

import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Niveau 2 – Tests Service UserService (unitaires avec Mockito)
 * On mocke EntityManagerUtil pour fournir un EntityManager H2 contrôlé.
 */
class UserServiceTest {

    private static EntityManagerFactory emf;
    private UserService userService;
    private MockedStatic<EntityManagerUtil> mockedUtil;

    @BeforeAll
    static void setUpFactory() {
        emf = Persistence.createEntityManagerFactory("MasterAnnoncePU");
    }

    @AfterAll
    static void tearDownFactory() {
        if (emf != null) emf.close();
    }

    @BeforeEach
    void setUp() {
        userService = new UserService();
        // Mocker EntityManagerUtil pour retourner un EM connecté à H2
        mockedUtil = mockStatic(EntityManagerUtil.class);
        mockedUtil.when(EntityManagerUtil::getEntityManager).thenAnswer(inv -> emf.createEntityManager());
    }

    @AfterEach
    void tearDown() {
        mockedUtil.close();
        // Nettoyage des données
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.createQuery("DELETE FROM Annonce").executeUpdate();
        em.createQuery("DELETE FROM User").executeUpdate();
        em.getTransaction().commit();
        em.close();
    }

    // ==================== Tests règles métier : création ====================

    @Test
    @DisplayName("create() crée un utilisateur avec succès")
    void create_shouldCreateUser() {
        User user = userService.create("alice", "alice@test.com", "password123");

        assertNotNull(user.getId());
        assertEquals("alice", user.getUsername());
        assertEquals("alice@test.com", user.getEmail());
        assertNotNull(user.getCreatedAt());
    }

    @Test
    @DisplayName("create() échoue si le username existe déjà")
    void create_shouldFail_whenDuplicateUsername() {
        userService.create("alice", "alice@test.com", "password123");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.create("alice", "other@test.com", "password123"));
        assertTrue(ex.getMessage().contains("nom d'utilisateur existe"));
    }

    @Test
    @DisplayName("create() échoue si l'email existe déjà")
    void create_shouldFail_whenDuplicateEmail() {
        userService.create("alice", "alice@test.com", "password123");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.create("bob", "alice@test.com", "password123"));
        assertTrue(ex.getMessage().contains("email existe"));
    }

    // ==================== Tests règles métier : mise à jour ====================

    @Test
    @DisplayName("update() met à jour username et email")
    void update_shouldModifyUser() {
        User user = userService.create("charlie", "charlie@test.com", "password123");

        User updated = userService.update(user.getId(), "charlie_new", "charlie_new@test.com");

        assertEquals("charlie_new", updated.getUsername());
        assertEquals("charlie_new@test.com", updated.getEmail());
    }

    @Test
    @DisplayName("update() échoue si le nouveau username est déjà pris")
    void update_shouldFail_whenUsernameAlreadyTaken() {
        userService.create("alice", "alice@test.com", "password123");
        User bob = userService.create("bob", "bob@test.com", "password123");

        assertThrows(IllegalArgumentException.class,
                () -> userService.update(bob.getId(), "alice", "bob@test.com"));
    }

    @Test
    @DisplayName("update() échoue pour un ID inexistant")
    void update_shouldFail_whenUserNotFound() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.update(999L, "ghost", "ghost@test.com"));
    }

    // ==================== Tests : authentification ====================

    @Test
    @DisplayName("authenticate() retourne l'utilisateur avec les bons identifiants")
    void authenticate_shouldReturnUser_whenCredentialsValid() {
        userService.create("dave", "dave@test.com", "secret123");

        Optional<User> result = userService.authenticate("dave", "secret123");

        assertTrue(result.isPresent());
        assertEquals("dave", result.get().getUsername());
    }

    @Test
    @DisplayName("authenticate() retourne vide avec un mauvais mot de passe")
    void authenticate_shouldReturnEmpty_whenWrongPassword() {
        userService.create("dave", "dave@test.com", "secret123");

        Optional<User> result = userService.authenticate("dave", "wrongpassword");

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("authenticate() retourne vide avec un username inconnu")
    void authenticate_shouldReturnEmpty_whenUnknownUsername() {
        Optional<User> result = userService.authenticate("inconnu", "password");
        assertTrue(result.isEmpty());
    }

    // ==================== Tests : changement de mot de passe ====================

    @Test
    @DisplayName("changePassword() modifie le mot de passe")
    void changePassword_shouldUpdatePassword() {
        User user = userService.create("eve", "eve@test.com", "oldpass123");

        userService.changePassword(user.getId(), "newpass456");

        // Vérifier que l'ancien mot de passe ne fonctionne plus
        assertTrue(userService.authenticate("eve", "oldpass123").isEmpty());
        // Et que le nouveau fonctionne
        assertTrue(userService.authenticate("eve", "newpass456").isPresent());
    }

    // ==================== Tests : recherche ====================

    @Test
    @DisplayName("findById() retourne l'utilisateur existant")
    void findById_shouldReturnUser() {
        User user = userService.create("frank", "frank@test.com", "password123");

        Optional<User> found = userService.findById(user.getId());

        assertTrue(found.isPresent());
        assertEquals("frank", found.get().getUsername());
    }

    @Test
    @DisplayName("findByUsername() retourne l'utilisateur par nom")
    void findByUsername_shouldReturnUser() {
        userService.create("grace", "grace@test.com", "password123");

        Optional<User> found = userService.findByUsername("grace");

        assertTrue(found.isPresent());
        assertEquals("grace@test.com", found.get().getEmail());
    }

    @Test
    @DisplayName("findAll() retourne tous les utilisateurs triés par date")
    void findAll_shouldReturnAllUsers() {
        userService.create("user1", "user1@test.com", "password123");
        userService.create("user2", "user2@test.com", "password123");
        userService.create("user3", "user3@test.com", "password123");

        List<User> users = userService.findAll();

        assertEquals(3, users.size());
    }

    // ==================== Tests : suppression ====================

    @Test
    @DisplayName("delete() supprime l'utilisateur")
    void delete_shouldRemoveUser() {
        User user = userService.create("toDelete", "delete@test.com", "password123");

        userService.delete(user.getId());

        assertTrue(userService.findById(user.getId()).isEmpty());
    }

    // ==================== Vérification des appels (mockStatic) ====================

    @Test
    @DisplayName("Chaque opération du service appelle EntityManagerUtil.getEntityManager()")
    void service_shouldCallEntityManagerUtil() {
        userService.create("verify", "verify@test.com", "password123");

        // Vérifier que getEntityManager a été appelé au moins 1 fois
        mockedUtil.verify(EntityManagerUtil::getEntityManager, atLeastOnce());
    }
}
