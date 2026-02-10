package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.dao;

import org.junit.jupiter.api.*;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.model.Annonce;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.model.AnnonceStatus;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.model.Category;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.model.User;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Niveau 1 – Tests DAO Annonce : CRUD + recherche et pagination
 */
class AnnonceDAOTest {

    private static EntityManagerFactory emf;
    private EntityManager em;
    private AnnonceDAO annonceDAO;

    private User testUser;
    private Category testCategory;

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
        annonceDAO = new AnnonceDAO();

        // Créer les entités de référence
        testUser = new User("testuser", "testuser@test.com", "password123");
        testCategory = new Category("Immobilier");

        em.getTransaction().begin();
        em.persist(testUser);
        em.persist(testCategory);
        em.getTransaction().commit();
    }

    @AfterEach
    void tearDown() {
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
        em.getTransaction().begin();
        em.createQuery("DELETE FROM Annonce").executeUpdate();
        em.createQuery("DELETE FROM Category").executeUpdate();
        em.createQuery("DELETE FROM User").executeUpdate();
        em.getTransaction().commit();
        em.close();
    }

    private Annonce createAnnonce(String title, String description, AnnonceStatus status) {
        Annonce annonce = new Annonce(title, description, "Paris", "contact@test.com");
        annonce.setAuthor(testUser);
        annonce.setCategory(testCategory);
        annonce.setStatus(status);
        return annonce;
    }

    // ==================== TESTS CRUD ====================

    @Test
    @DisplayName("save() persiste une annonce avec ses relations")
    void save_shouldPersistAnnonce() {
        Annonce annonce = createAnnonce("Appartement F3", "Bel appart lumineux", AnnonceStatus.DRAFT);

        em.getTransaction().begin();
        Annonce saved = annonceDAO.save(em, annonce);

        assertNotNull(saved.getId());
        assertEquals("Appartement F3", saved.getTitle());
        assertEquals(AnnonceStatus.DRAFT, saved.getStatus());
        assertNotNull(saved.getDate());
        assertEquals(testUser.getId(), saved.getAuthor().getId());
        assertEquals(testCategory.getId(), saved.getCategory().getId());
    }

    @Test
    @DisplayName("findById() retourne l'annonce existante")
    void findById_shouldReturnAnnonce() {
        Annonce annonce = createAnnonce("Maison", "Grande maison", AnnonceStatus.DRAFT);
        em.getTransaction().begin();
        annonceDAO.save(em, annonce);

        Optional<Annonce> found = annonceDAO.findById(em, annonce.getId());

        assertTrue(found.isPresent());
        assertEquals("Maison", found.get().getTitle());
    }

    @Test
    @DisplayName("update() met à jour le titre et le statut")
    void update_shouldModifyAnnonce() {
        Annonce annonce = createAnnonce("Ancien titre", "Description", AnnonceStatus.DRAFT);
        em.getTransaction().begin();
        annonceDAO.save(em, annonce);

        annonce.setTitle("Nouveau titre");
        annonce.setStatus(AnnonceStatus.PUBLISHED);
        em.getTransaction().begin();
        Annonce updated = annonceDAO.update(em, annonce);

        assertEquals("Nouveau titre", updated.getTitle());
        assertEquals(AnnonceStatus.PUBLISHED, updated.getStatus());
    }

    @Test
    @DisplayName("deleteById() supprime l'annonce")
    void deleteById_shouldRemoveAnnonce() {
        Annonce annonce = createAnnonce("A supprimer", "Desc", AnnonceStatus.DRAFT);
        em.getTransaction().begin();
        annonceDAO.save(em, annonce);
        Long id = annonce.getId();

        em.getTransaction().begin();
        annonceDAO.deleteById(em, id);

        assertTrue(annonceDAO.findById(em, id).isEmpty());
    }

    // ==================== TESTS RECHERCHE ET PAGINATION ====================

    @Test
    @DisplayName("findWithFilters() filtre par statut PUBLISHED")
    void findWithFilters_shouldFilterByStatus() {
        em.getTransaction().begin();
        em.persist(createAnnonce("Draft 1", "Desc", AnnonceStatus.DRAFT));
        em.persist(createAnnonce("Published 1", "Desc", AnnonceStatus.PUBLISHED));
        em.persist(createAnnonce("Published 2", "Desc", AnnonceStatus.PUBLISHED));
        em.getTransaction().commit();

        List<Annonce> published = annonceDAO.findWithFilters(em,
                Map.of("status", AnnonceStatus.PUBLISHED),
                null, null, "date DESC", null);

        assertEquals(2, published.size());
        assertTrue(published.stream().allMatch(a -> a.getStatus() == AnnonceStatus.PUBLISHED));
    }

    @Test
    @DisplayName("findWithFilters() recherche par mot-clé dans titre et description")
    void findWithFilters_shouldSearchByKeyword() {
        em.getTransaction().begin();
        em.persist(createAnnonce("Appartement lumineux", "Centre ville", AnnonceStatus.PUBLISHED));
        em.persist(createAnnonce("Maison de campagne", "Jardin lumineux", AnnonceStatus.PUBLISHED));
        em.persist(createAnnonce("Studio", "Petit studio", AnnonceStatus.PUBLISHED));
        em.getTransaction().commit();

        List<Annonce> results = annonceDAO.findWithFilters(em,
                Map.of("status", AnnonceStatus.PUBLISHED),
                "lumineux",
                new String[]{"title", "description"},
                "date DESC", null);

        assertEquals(2, results.size());
    }

    @Test
    @DisplayName("findWithFilters() pagine correctement les résultats")
    void findWithFilters_shouldPaginateResults() {
        em.getTransaction().begin();
        for (int i = 0; i < 15; i++) {
            Annonce a = createAnnonce("Annonce " + i, "Description " + i, AnnonceStatus.PUBLISHED);
            em.persist(a);
        }
        em.getTransaction().commit();

        // Page 0, taille 5
        List<Annonce> page0 = annonceDAO.findWithFilters(em,
                Map.of("status", AnnonceStatus.PUBLISHED),
                null, null, "title ASC", null, 0, 5);
        assertEquals(5, page0.size());

        // Page 1, taille 5
        List<Annonce> page1 = annonceDAO.findWithFilters(em,
                Map.of("status", AnnonceStatus.PUBLISHED),
                null, null, "title ASC", null, 1, 5);
        assertEquals(5, page1.size());

        // Page 2, taille 5 (les 5 derniers)
        List<Annonce> page2 = annonceDAO.findWithFilters(em,
                Map.of("status", AnnonceStatus.PUBLISHED),
                null, null, "title ASC", null, 2, 5);
        assertEquals(5, page2.size());

        // Vérifier que les pages ne se chevauchent pas
        assertNotEquals(page0.get(0).getId(), page1.get(0).getId());
    }

    @Test
    @DisplayName("countWithFilters() compte le bon nombre d'annonces publiées")
    void countWithFilters_shouldCountCorrectly() {
        em.getTransaction().begin();
        em.persist(createAnnonce("A1", "D1", AnnonceStatus.PUBLISHED));
        em.persist(createAnnonce("A2", "D2", AnnonceStatus.PUBLISHED));
        em.persist(createAnnonce("A3", "D3", AnnonceStatus.DRAFT));
        em.persist(createAnnonce("A4", "D4", AnnonceStatus.ARCHIVED));
        em.getTransaction().commit();

        long totalCount = annonceDAO.count(em);
        assertEquals(4, totalCount);

        long publishedCount = annonceDAO.countWithFilters(em, Map.of("status", AnnonceStatus.PUBLISHED));
        assertEquals(2, publishedCount);
    }

    @Test
    @DisplayName("findWithFilters() filtre par auteur")
    void findWithFilters_shouldFilterByAuthor() {
        User otherUser = new User("other", "other@test.com", "password123");
        em.getTransaction().begin();
        em.persist(otherUser);
        em.getTransaction().commit();

        Annonce annonceUser1 = createAnnonce("Par testuser", "Desc", AnnonceStatus.PUBLISHED);
        Annonce annonceUser2 = createAnnonce("Par other", "Desc", AnnonceStatus.PUBLISHED);
        annonceUser2.setAuthor(otherUser);

        em.getTransaction().begin();
        em.persist(annonceUser1);
        em.persist(annonceUser2);
        em.getTransaction().commit();

        List<Annonce> byAuthor = annonceDAO.findWithFilters(em,
                Map.of("author.id", testUser.getId()),
                null, null, "date DESC",
                "LEFT JOIN FETCH e.author LEFT JOIN FETCH e.category");

        assertEquals(1, byAuthor.size());
        assertEquals("Par testuser", byAuthor.get(0).getTitle());
    }

    @Test
    @DisplayName("findWithFilters() filtre par catégorie")
    void findWithFilters_shouldFilterByCategory() {
        Category otherCategory = new Category("Véhicules");
        em.getTransaction().begin();
        em.persist(otherCategory);
        em.getTransaction().commit();

        Annonce a1 = createAnnonce("Immobilier annonce", "Desc", AnnonceStatus.PUBLISHED);
        Annonce a2 = createAnnonce("Véhicule annonce", "Desc", AnnonceStatus.PUBLISHED);
        a2.setCategory(otherCategory);

        em.getTransaction().begin();
        em.persist(a1);
        em.persist(a2);
        em.getTransaction().commit();

        Map<String, Object> filters = new HashMap<>();
        filters.put("category.id", testCategory.getId());
        filters.put("status", AnnonceStatus.PUBLISHED);

        List<Annonce> byCat = annonceDAO.findWithFilters(em, filters,
                null, null, "date DESC",
                "LEFT JOIN FETCH e.author LEFT JOIN FETCH e.category");

        assertEquals(1, byCat.size());
        assertEquals("Immobilier annonce", byCat.get(0).getTitle());
    }

    @Test
    @DisplayName("findOneWithFilters() avec JOIN FETCH charge les relations")
    void findOneWithFilters_withJoinFetch_shouldLoadRelations() {
        Annonce annonce = createAnnonce("Test join", "Desc", AnnonceStatus.PUBLISHED);
        em.getTransaction().begin();
        em.persist(annonce);
        em.getTransaction().commit();

        // Nouvel EntityManager pour vérifier le chargement eager via JOIN FETCH
        EntityManager em2 = emf.createEntityManager();
        try {
            Optional<Annonce> found = annonceDAO.findOneWithFilters(em2,
                    Map.of("id", annonce.getId()),
                    "LEFT JOIN FETCH e.author LEFT JOIN FETCH e.category");

            assertTrue(found.isPresent());
            // Ces accès ne doivent pas lancer de LazyInitializationException
            assertEquals("testuser", found.get().getAuthor().getUsername());
            assertEquals("Immobilier", found.get().getCategory().getLabel());
        } finally {
            em2.close();
        }
    }
}
