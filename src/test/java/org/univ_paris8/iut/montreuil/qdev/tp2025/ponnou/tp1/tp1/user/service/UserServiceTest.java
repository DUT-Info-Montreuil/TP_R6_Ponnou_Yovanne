package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.config.EntityManagerUtil;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.model.User;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.repository.UserRepository;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Niveau 2 – Tests unitaires UserService (Mockito)
 * Règles métier : unicité username/email, authentification, CRUD
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private EntityManager em;
    @Mock private EntityTransaction tx;

    private UserService userService;
    private MockedStatic<EntityManagerUtil> mockedUtil;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository);

        mockedUtil = mockStatic(EntityManagerUtil.class);
        mockedUtil.when(EntityManagerUtil::getEntityManager).thenReturn(em);
        lenient().when(em.getTransaction()).thenReturn(tx);
    }

    @AfterEach
    void tearDown() {
        mockedUtil.close();
    }

    // ==================== Tests règles métier : création ====================

    @Test
    @DisplayName("create() crée un utilisateur avec succès")
    void create_shouldCreateUser() {
        when(userRepository.countWithFilters(eq(em), eq(Map.of("username", "alice")))).thenReturn(0L);
        when(userRepository.countWithFilters(eq(em), eq(Map.of("email", "alice@test.com")))).thenReturn(0L);
        when(userRepository.save(eq(em), any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(1);
            u.setId(1L);
            return u;
        });

        User user = userService.create("alice", "alice@test.com", "password123");

        assertNotNull(user.getId());
        assertEquals("alice", user.getUsername());
        assertEquals("alice@test.com", user.getEmail());
        verify(tx).commit();
    }

    @Test
    @DisplayName("create() échoue si le username existe déjà")
    void create_shouldFail_whenDuplicateUsername() {
        when(userRepository.countWithFilters(eq(em), eq(Map.of("username", "alice")))).thenReturn(1L);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.create("alice", "other@test.com", "password123"));
        assertTrue(ex.getMessage().contains("nom d'utilisateur existe"));
    }

    @Test
    @DisplayName("create() échoue si l'email existe déjà")
    void create_shouldFail_whenDuplicateEmail() {
        when(userRepository.countWithFilters(eq(em), eq(Map.of("username", "bob")))).thenReturn(0L);
        when(userRepository.countWithFilters(eq(em), eq(Map.of("email", "alice@test.com")))).thenReturn(1L);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.create("bob", "alice@test.com", "password123"));
        assertTrue(ex.getMessage().contains("email existe"));
    }

    // ==================== Tests règles métier : mise à jour ====================

    @Test
    @DisplayName("update() met à jour username et email")
    void update_shouldModifyUser() {
        User existing = new User("charlie", "charlie@test.com", "password123");
        existing.setId(1L);

        when(userRepository.findById(em, 1L)).thenReturn(Optional.of(existing));
        when(userRepository.countWithFilters(eq(em), eq(Map.of("username", "charlie_new")))).thenReturn(0L);
        when(userRepository.countWithFilters(eq(em), eq(Map.of("email", "charlie_new@test.com")))).thenReturn(0L);
        when(userRepository.update(em, existing)).thenReturn(existing);

        User updated = userService.update(1L, "charlie_new", "charlie_new@test.com");

        assertEquals("charlie_new", updated.getUsername());
        assertEquals("charlie_new@test.com", updated.getEmail());
    }

    @Test
    @DisplayName("update() échoue si le nouveau username est déjà pris")
    void update_shouldFail_whenUsernameAlreadyTaken() {
        User bob = new User("bob", "bob@test.com", "password123");
        bob.setId(2L);

        when(userRepository.findById(em, 2L)).thenReturn(Optional.of(bob));
        when(userRepository.countWithFilters(eq(em), eq(Map.of("username", "alice")))).thenReturn(1L);

        assertThrows(IllegalArgumentException.class,
                () -> userService.update(2L, "alice", "bob@test.com"));
    }

    @Test
    @DisplayName("update() échoue pour un ID inexistant")
    void update_shouldFail_whenUserNotFound() {
        when(userRepository.findById(em, 999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> userService.update(999L, "ghost", "ghost@test.com"));
    }

    // ==================== Tests : authentification ====================

    @Test
    @DisplayName("authenticate() retourne l'utilisateur avec les bons identifiants")
    void authenticate_shouldReturnUser_whenCredentialsValid() {
        User dave = new User("dave", "dave@test.com", "secret123");
        dave.setId(1L);

        when(userRepository.findOneWithFilters(eq(em), eq(Map.of("username", "dave", "password", "secret123"))))
                .thenReturn(Optional.of(dave));

        Optional<User> result = userService.authenticate("dave", "secret123");

        assertTrue(result.isPresent());
        assertEquals("dave", result.get().getUsername());
    }

    @Test
    @DisplayName("authenticate() retourne vide avec un mauvais mot de passe")
    void authenticate_shouldReturnEmpty_whenWrongPassword() {
        when(userRepository.findOneWithFilters(eq(em), eq(Map.of("username", "dave", "password", "wrongpassword"))))
                .thenReturn(Optional.empty());

        Optional<User> result = userService.authenticate("dave", "wrongpassword");

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("authenticate() retourne vide avec un username inconnu")
    void authenticate_shouldReturnEmpty_whenUnknownUsername() {
        when(userRepository.findOneWithFilters(eq(em), eq(Map.of("username", "inconnu", "password", "password"))))
                .thenReturn(Optional.empty());

        Optional<User> result = userService.authenticate("inconnu", "password");

        assertTrue(result.isEmpty());
    }

    // ==================== Tests : changement de mot de passe ====================

    @Test
    @DisplayName("changePassword() modifie le mot de passe")
    void changePassword_shouldUpdatePassword() {
        User eve = new User("eve", "eve@test.com", "oldpass123");
        eve.setId(1L);

        when(userRepository.findById(em, 1L)).thenReturn(Optional.of(eve));
        when(userRepository.update(em, eve)).thenReturn(eve);

        userService.changePassword(1L, "newpass456");

        assertEquals("newpass456", eve.getPassword());
        verify(userRepository).update(em, eve);
        verify(tx).commit();
    }

    // ==================== Tests : recherche ====================

    @Test
    @DisplayName("findById() retourne l'utilisateur existant")
    void findById_shouldReturnUser() {
        User frank = new User("frank", "frank@test.com", "password123");
        frank.setId(1L);

        when(userRepository.findById(em, 1L)).thenReturn(Optional.of(frank));

        Optional<User> found = userService.findById(1L);

        assertTrue(found.isPresent());
        assertEquals("frank", found.get().getUsername());
    }

    @Test
    @DisplayName("findByUsername() retourne l'utilisateur par nom")
    void findByUsername_shouldReturnUser() {
        User grace = new User("grace", "grace@test.com", "password123");

        when(userRepository.findOneWithFilters(eq(em), eq(Map.of("username", "grace"))))
                .thenReturn(Optional.of(grace));

        Optional<User> found = userService.findByUsername("grace");

        assertTrue(found.isPresent());
        assertEquals("grace@test.com", found.get().getEmail());
    }

    @Test
    @DisplayName("findAll() retourne tous les utilisateurs")
    void findAll_shouldReturnAllUsers() {
        when(userRepository.findWithFilters(eq(em), isNull(), isNull(), isNull(),
                eq("createdAt DESC"), isNull()))
                .thenReturn(List.of(
                        new User("u1", "u1@t.com", "p"),
                        new User("u2", "u2@t.com", "p"),
                        new User("u3", "u3@t.com", "p")));

        List<User> users = userService.findAll();

        assertEquals(3, users.size());
    }

    // ==================== Tests : suppression ====================

    @Test
    @DisplayName("delete() supprime l'utilisateur")
    void delete_shouldRemoveUser() {
        when(userRepository.deleteById(em, 1L)).thenReturn(true);

        userService.delete(1L);

        verify(userRepository).deleteById(em, 1L);
        verify(tx).commit();
    }

    // ==================== Vérification des appels ====================

    @Test
    @DisplayName("Chaque opération du service appelle EntityManagerUtil.getEntityManager()")
    void service_shouldCallEntityManagerUtil() {
        when(userRepository.countWithFilters(eq(em), any(Map.class))).thenReturn(0L);
        when(userRepository.save(eq(em), any(User.class))).thenAnswer(inv -> inv.getArgument(1));

        userService.create("verify", "verify@test.com", "password123");

        mockedUtil.verify(EntityManagerUtil::getEntityManager, atLeastOnce());
    }
}
