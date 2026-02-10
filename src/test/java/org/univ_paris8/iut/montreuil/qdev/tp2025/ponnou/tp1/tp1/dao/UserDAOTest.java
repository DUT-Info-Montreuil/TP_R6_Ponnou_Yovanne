package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.dao;

import org.junit.jupiter.api.*;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.model.User;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Niveau 1a – Tests CRUD avec base réelle (H2 en mémoire)
 */
class UserDAOTest {

    private static EntityManagerFactory emf;
    private EntityManager em;
    private UserDAO userDAO;

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
        userDAO = new UserDAO();
    }

    @AfterEach
    void tearDown() {
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
        // Nettoyer les données
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
        User saved = userDAO.save(em, user);

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
        userDAO.save(em, user);

        Optional<User> found = userDAO.findById(em, user.getId());

        assertTrue(found.isPresent());
        assertEquals("bob", found.get().getUsername());
    }

    @Test
    @DisplayName("findById() retourne vide pour un ID inexistant")
    void findById_shouldReturnEmpty_whenNotExists() {
        Optional<User> found = userDAO.findById(em, 999L);
        assertTrue(found.isEmpty());
    }

    @Test
    @DisplayName("update() met à jour les champs de l'utilisateur")
    void update_shouldModifyUser() {
        User user = new User("charlie", "charlie@test.com", "password123");
        em.getTransaction().begin();
        userDAO.save(em, user);

        user.setUsername("charlie_updated");
        user.setEmail("charlie_new@test.com");
        em.getTransaction().begin();
        User updated = userDAO.update(em, user);

        assertEquals("charlie_updated", updated.getUsername());
        assertEquals("charlie_new@test.com", updated.getEmail());
    }

    @Test
    @DisplayName("deleteById() supprime l'utilisateur")
    void deleteById_shouldRemoveUser() {
        User user = new User("dave", "dave@test.com", "password123");
        em.getTransaction().begin();
        userDAO.save(em, user);
        Long id = user.getId();

        em.getTransaction().begin();
        userDAO.deleteById(em, id);

        Optional<User> found = userDAO.findById(em, id);
        assertTrue(found.isEmpty());
    }

    @Test
    @DisplayName("delete() avec entité supprime l'utilisateur")
    void delete_shouldRemoveUser() {
        User user = new User("eve", "eve@test.com", "password123");
        em.getTransaction().begin();
        userDAO.save(em, user);
        Long id = user.getId();

        em.getTransaction().begin();
        userDAO.delete(em, user);

        Optional<User> found = userDAO.findById(em, id);
        assertTrue(found.isEmpty());
    }

    // ==================== TESTS RECHERCHE ====================

    @Test
    @DisplayName("findOneWithFilters() trouve un utilisateur par username")
    void findOneWithFilters_shouldFindByUsername() {
        User user = new User("frank", "frank@test.com", "password123");
        em.getTransaction().begin();
        userDAO.save(em, user);

        Optional<User> found = userDAO.findOneWithFilters(em, Map.of("username", "frank"));

        assertTrue(found.isPresent());
        assertEquals("frank@test.com", found.get().getEmail());
    }

    @Test
    @DisplayName("findOneWithFilters() retourne vide si aucun match")
    void findOneWithFilters_shouldReturnEmpty_whenNoMatch() {
        Optional<User> found = userDAO.findOneWithFilters(em, Map.of("username", "inexistant"));
        assertTrue(found.isEmpty());
    }

    @Test
    @DisplayName("countWithFilters() compte correctement les utilisateurs")
    void countWithFilters_shouldReturnCorrectCount() {
        em.getTransaction().begin();
        userDAO.save(em, new User("user1", "user1@test.com", "password123"));
        em.getTransaction().begin();
        userDAO.save(em, new User("user2", "user2@test.com", "password123"));
        em.getTransaction().begin();
        userDAO.save(em, new User("user3", "user3@test.com", "password123"));

        long total = userDAO.count(em);
        assertEquals(3, total);

        long filtered = userDAO.countWithFilters(em, Map.of("username", "user1"));
        assertEquals(1, filtered);
    }

    @Test
    @DisplayName("findWithFilters() avec tri retourne les résultats ordonnés")
    void findWithFilters_shouldReturnOrdered() {
        em.getTransaction().begin();
        userDAO.save(em, new User("zara", "zara@test.com", "password123"));
        em.getTransaction().begin();
        userDAO.save(em, new User("adam", "adam@test.com", "password123"));
        em.getTransaction().begin();
        userDAO.save(em, new User("mia", "mia@test.com", "password123"));

        List<User> users = userDAO.findWithFilters(em, null, null, null, "username ASC", null);

        assertEquals(3, users.size());
        assertEquals("adam", users.get(0).getUsername());
        assertEquals("mia", users.get(1).getUsername());
        assertEquals("zara", users.get(2).getUsername());
    }
}
