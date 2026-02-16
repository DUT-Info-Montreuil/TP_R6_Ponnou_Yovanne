package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.users.repository;

import org.junit.jupiter.api.*;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.model.User;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.repository.UserRepository;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryTest {

    private static EntityManagerFactory emf;
    private EntityManager em;
    private UserRepository userRepository;

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
        em = emf.createEntityManager();
        userRepository = new UserRepository();
    }

    @AfterEach
    void tearDown() {
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
        em.getTransaction().begin();
        em.createQuery("DELETE FROM Annonce").executeUpdate();
        em.createQuery("DELETE FROM User").executeUpdate();
        em.getTransaction().commit();
        em.close();
    }

    // ==================== TESTS CRUD ====================

    @Test
    @DisplayName("save() persiste un utilisateur et lui attribue un ID")
    void save_shouldPersistUser() {
        User user = new User("alice", "alice@test.com", "password123");

        em.getTransaction().begin();
        User saved = userRepository.save(em, user);

        assertNotNull(saved.getId());
        assertEquals("alice", saved.getUsername());
        assertEquals("alice@test.com", saved.getEmail());
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    @DisplayName("findById() retourne l'utilisateur existant")
    void findById_shouldReturnUser_whenExists() {
        User user = new User("bob", "bob@test.com", "password123");
        em.getTransaction().begin();
        userRepository.save(em, user);

        Optional<User> found = userRepository.findById(em, user.getId());

        assertTrue(found.isPresent());
        assertEquals("bob", found.get().getUsername());
    }

    @Test
    @DisplayName("findById() retourne vide pour un ID inexistant")
    void findById_shouldReturnEmpty_whenNotExists() {
        Optional<User> found = userRepository.findById(em, 999L);
        assertTrue(found.isEmpty());
    }

    @Test
    @DisplayName("update() met a jour les champs de l'utilisateur")
    void update_shouldModifyUser() {
        User user = new User("charlie", "charlie@test.com", "password123");
        em.getTransaction().begin();
        userRepository.save(em, user);
        em.getTransaction().commit();

        user.setUsername("charlie_updated");
        user.setEmail("charlie_new@test.com");
        em.getTransaction().begin();
        User updated = userRepository.update(em, user);

        assertEquals("charlie_updated", updated.getUsername());
        assertEquals("charlie_new@test.com", updated.getEmail());
    }

    @Test
    @DisplayName("deleteById() supprime l'utilisateur")
    void deleteById_shouldRemoveUser() {
        User user = new User("dave", "dave@test.com", "password123");
        em.getTransaction().begin();
        userRepository.save(em, user);
        Long id = user.getId();
        em.getTransaction().commit();

        em.getTransaction().begin();
        userRepository.deleteById(em, id);

        Optional<User> found = userRepository.findById(em, id);
        assertTrue(found.isEmpty());
    }

    @Test
    @DisplayName("delete() avec entite supprime l'utilisateur")
    void delete_shouldRemoveUser() {
        User user = new User("eve", "eve@test.com", "password123");
        em.getTransaction().begin();
        userRepository.save(em, user);
        Long id = user.getId();
        em.getTransaction().commit();

        em.getTransaction().begin();
        userRepository.delete(em, user);

        Optional<User> found = userRepository.findById(em, id);
        assertTrue(found.isEmpty());
    }

    // ==================== TESTS RECHERCHE ====================

    @Test
    @DisplayName("findOneWithFilters() trouve un utilisateur par username")
    void findOneWithFilters_shouldFindByUsername() {
        User user = new User("frank", "frank@test.com", "password123");
        em.getTransaction().begin();
        userRepository.save(em, user);

        Optional<User> found = userRepository.findOneWithFilters(em, Map.of("username", "frank"));

        assertTrue(found.isPresent());
        assertEquals("frank@test.com", found.get().getEmail());
    }

    @Test
    @DisplayName("findOneWithFilters() retourne vide si aucun match")
    void findOneWithFilters_shouldReturnEmpty_whenNoMatch() {
        Optional<User> found = userRepository.findOneWithFilters(em, Map.of("username", "inexistant"));
        assertTrue(found.isEmpty());
    }

    @Test
    @DisplayName("countWithFilters() compte correctement les utilisateurs")
    void countWithFilters_shouldReturnCorrectCount() {
        em.getTransaction().begin();
        userRepository.save(em, new User("user1", "user1@test.com", "password123"));
        userRepository.save(em, new User("user2", "user2@test.com", "password123"));
        userRepository.save(em, new User("user3", "user3@test.com", "password123"));
        em.getTransaction().commit();

        long total = userRepository.count(em);
        assertEquals(3, total);

        long filtered = userRepository.countWithFilters(em, Map.of("username", "user1"));
        assertEquals(1, filtered);
    }

    @Test
    @DisplayName("findWithFilters() avec tri retourne les resultats ordonnes")
    void findWithFilters_shouldReturnOrdered() {
        em.getTransaction().begin();
        userRepository.save(em, new User("zara", "zara@test.com", "password123"));
        userRepository.save(em, new User("adam", "adam@test.com", "password123"));
        userRepository.save(em, new User("mia", "mia@test.com", "password123"));
        em.getTransaction().commit();

        List<User> users = userRepository.findWithFilters(em, null, null, null, "username ASC", null);

        assertEquals(3, users.size());
        assertEquals("adam", users.get(0).getUsername());
        assertEquals("mia", users.get(1).getUsername());
        assertEquals("zara", users.get(2).getUsername());
    }
}
