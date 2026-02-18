package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.repository;

import org.junit.jupiter.api.*;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.model.Annonce;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.model.AnnonceStatus;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.model.Category;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.model.User;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class LazyLoadingTest {

    private static EntityManagerFactory emf;
    private AnnonceRepository annonceRepository;
    private User testUser;
    private Category testCategory;

    @BeforeAll
    static void setUpFactory() {
        emf = Persistence.createEntityManagerFactory("MasterAnnoncePU");
    }

    @AfterAll
    static void tearDownFactory() {
        if (emf != null)
            emf.close();
    }

    @BeforeEach
    void setUp() {
        EntityManager em = emf.createEntityManager();
        annonceRepository = new AnnonceRepository();

        testUser = new User("lazyuser", "lazy@test.com", "password123");
        testCategory = new Category("TestCategory");

        em.getTransaction().begin();
        em.persist(testUser);
        em.persist(testCategory);

        // Créer plusieurs annonces pour tester N+1
        for (int i = 0; i < 5; i++) {
            Annonce a = new Annonce("Annonce " + i, "Desc " + i, "Paris", "m@t.com");
            a.setAuthor(testUser);
            a.setCategory(testCategory);
            a.setStatus(AnnonceStatus.PUBLISHED);
            em.persist(a);
        }
        em.getTransaction().commit();
        em.close();
    }

    @AfterEach
    void tearDown() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.createQuery("DELETE FROM Annonce").executeUpdate();
        em.createQuery("DELETE FROM Category").executeUpdate();
        em.createQuery("DELETE FROM User").executeUpdate();
        em.getTransaction().commit();
        em.close();
    }

    // ==================== Problème Lazy Loading ====================

    @Test
    @DisplayName("SANS JOIN FETCH : accès à author hors session lève LazyInitializationException")
    void withoutJoinFetch_shouldFailOnLazyAccess() {
        // Charger l'annonce SANS join fetch dans un EM qu'on ferme ensuite
        EntityManager em = emf.createEntityManager();
        List<Annonce> annonces = annonceRepository.findWithFilters(em,
                Map.of("status", AnnonceStatus.PUBLISHED),
                null, null, "title ASC", null);
        assertFalse(annonces.isEmpty());
        em.close(); // Fermer la session

        // Accéder à la relation lazy hors session → doit échouer
        Annonce annonce = annonces.get(0);
        assertThrows(Exception.class, () -> {
            // Accès au proxy lazy après fermeture de l'EntityManager
            annonce.getAuthor().getUsername();
        });
    }

    @Test
    @DisplayName("AVEC JOIN FETCH : accès à author hors session fonctionne")
    void withJoinFetch_shouldSucceedOnLazyAccess() {
        // Charger l'annonce AVEC join fetch
        EntityManager em = emf.createEntityManager();
        List<Annonce> annonces = annonceRepository.findWithFilters(em,
                Map.of("status", AnnonceStatus.PUBLISHED),
                null, null, "title ASC",
                "LEFT JOIN FETCH e.author LEFT JOIN FETCH e.category");
        assertFalse(annonces.isEmpty());
        em.close(); // Fermer la session

        // Accéder aux relations hors session → doit fonctionner
        Annonce annonce = annonces.get(0);
        assertDoesNotThrow(() -> {
            String username = annonce.getAuthor().getUsername();
            String label = annonce.getCategory().getLabel();
            assertEquals("lazyuser", username);
            assertEquals("TestCategory", label);
        });
    }

    // ==================== Problème N+1 ====================

    @Test
    @DisplayName("Démonstration du problème N+1 : sans JOIN FETCH, chaque accès déclenche une requête")
    void nPlusOne_withoutJoinFetch_eachAccessTriggersQuery() {
        EntityManager em = emf.createEntityManager();

        // Charger 5 annonces SANS join fetch (1 requête)
        List<Annonce> annonces = annonceRepository.findWithFilters(em,
                Map.of("status", AnnonceStatus.PUBLISHED),
                null, null, "title ASC", null);
        assertEquals(5, annonces.size());

        for (Annonce a : annonces) {
            assertNotNull(a.getAuthor().getUsername()); // Chaque appel = 1 requête SQL
        }

        em.close();
    }

    @Test
    @DisplayName("Avec JOIN FETCH : toutes les relations chargées en 1 seule requête")
    void withJoinFetch_allRelationsLoadedInOneQuery() {
        EntityManager em = emf.createEntityManager();

        // Charger 5 annonces AVEC join fetch (1 seule requête avec JOIN)
        List<Annonce> annonces = annonceRepository.findWithFilters(em,
                Map.of("status", AnnonceStatus.PUBLISHED),
                null, null, "title ASC",
                "LEFT JOIN FETCH e.author LEFT JOIN FETCH e.category");
        assertEquals(5, annonces.size());

        em.close(); // Fermer la session

        // Tous les accès fonctionnent sans requête supplémentaire
        for (Annonce a : annonces) {
            assertDoesNotThrow(() -> {
                assertEquals("lazyuser", a.getAuthor().getUsername());
                assertEquals("TestCategory", a.getCategory().getLabel());
            });
        }
    }

    // ==================== findById vs findOneWithFilters + JOIN FETCH
    // ====================

    @Test
    @DisplayName("findById() simple ne charge pas les relations (LAZY)")
    void findById_doesNotLoadRelations() {
        EntityManager em = emf.createEntityManager();
        List<Annonce> all = annonceRepository.findWithFilters(em, null, null, null, null, null);
        Long annonceId = all.get(0).getId();
        em.close();

        // findById retourne l'entité mais les relations sont des proxys
        EntityManager em2 = emf.createEntityManager();
        Optional<Annonce> found = annonceRepository.findById(em2, annonceId);
        assertTrue(found.isPresent());
        em2.close();

        // Hors session, accéder au proxy échoue
        assertThrows(Exception.class, () -> found.get().getAuthor().getUsername());
    }

    @Test
    @DisplayName("findOneWithFilters() + JOIN FETCH charge les relations")
    void findOneWithFilters_withJoin_loadsRelations() {
        EntityManager em = emf.createEntityManager();
        List<Annonce> all = annonceRepository.findWithFilters(em, null, null, null, null, null);
        Long annonceId = all.get(0).getId();
        em.close();

        EntityManager em2 = emf.createEntityManager();
        Optional<Annonce> found = annonceRepository.findOneWithFilters(em2,
                Map.of("id", annonceId),
                "LEFT JOIN FETCH e.author LEFT JOIN FETCH e.category");
        assertTrue(found.isPresent());
        em2.close();

        // Hors session, les données sont disponibles
        assertDoesNotThrow(() -> {
            assertEquals("lazyuser", found.get().getAuthor().getUsername());
            assertEquals("TestCategory", found.get().getCategory().getLabel());
        });
    }
}
