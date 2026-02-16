package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonces.integration;

import org.junit.jupiter.api.*;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.model.Annonce;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.service.AnnonceService;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.model.AnnonceStatus;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.model.Category;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.service.CategoryService;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.config.EntityManagerUtil;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.model.User;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.service.UserService;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.List;

import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Niveau 3a – Tests d'intégration métier
 * Enchaînement complet : création utilisateur → création catégorie →
 * création annonce → publication → recherche
 */
class AnnonceWorkflowIntegrationTest {

    private static EntityManagerFactory emf;
    private MockedStatic<EntityManagerUtil> mockedUtil;

    private UserService userService;
    private CategoryService categoryService;
    private AnnonceService annonceService;

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
        categoryService = new CategoryService();
        annonceService = new AnnonceService();

        mockedUtil = mockStatic(EntityManagerUtil.class);
        mockedUtil.when(EntityManagerUtil::getEntityManager).thenAnswer(inv -> emf.createEntityManager());
    }

    @AfterEach
    void tearDown() {
        mockedUtil.close();
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.createQuery("DELETE FROM Annonce").executeUpdate();
        em.createQuery("DELETE FROM Category").executeUpdate();
        em.createQuery("DELETE FROM User").executeUpdate();
        em.getTransaction().commit();
        em.close();
    }

    // ==================== Workflow complet ====================

    @Test
    @DisplayName("Workflow complet : inscription → catégorie → annonce → publication → recherche")
    void fullWorkflow_createPublishSearch() {
        // 1. Inscription d'un utilisateur
        User user = userService.create("jean", "jean@test.com", "motdepasse123");
        assertNotNull(user.getId());

        // 2. Création d'une catégorie
        Category category = categoryService.create("Immobilier");
        assertNotNull(category.getId());

        // 3. Création d'une annonce (DRAFT par défaut)
        Annonce annonce = annonceService.create(
                "Appartement F3 lumineux",
                "Bel appartement de 60m² en centre ville",
                "10 rue de la Paix, Paris",
                "jean@contact.com",
                user.getId(),
                category.getId());

        assertNotNull(annonce.getId());
        assertEquals(AnnonceStatus.DRAFT, annonce.getStatus());

        // 4. L'annonce DRAFT ne doit PAS apparaître dans les résultats publiés
        List<Annonce> publishedBefore = annonceService.findPublishedPaginated(0, 10);
        assertTrue(publishedBefore.isEmpty());

        // 5. Publication de l'annonce
        Annonce published = annonceService.publish(annonce.getId(), user.getId());
        assertEquals(AnnonceStatus.PUBLISHED, published.getStatus());

        // 6. L'annonce PUBLISHED apparaît maintenant dans les résultats
        List<Annonce> publishedAfter = annonceService.findPublishedPaginated(0, 10);
        assertEquals(1, publishedAfter.size());
        assertEquals("Appartement F3 lumineux", publishedAfter.get(0).getTitle());

        // 7. Recherche par mot-clé
        List<Annonce> searchResults = annonceService.searchByKeywordPaginated("lumineux", 0, 10);
        assertEquals(1, searchResults.size());

        // 8. Recherche par catégorie
        List<Annonce> byCat = annonceService.findByCategoryPaginated(category.getId(), 0, 10);
        assertEquals(1, byCat.size());

        // 9. Recherche avec un mot-clé qui ne matche pas
        List<Annonce> noResults = annonceService.searchByKeywordPaginated("voiture", 0, 10);
        assertTrue(noResults.isEmpty());
    }

    @Test
    @DisplayName("Workflow : création de plusieurs annonces → pagination correcte")
    void workflow_multipleAnnonces_pagination() {
        User user = userService.create("marie", "marie@test.com", "password123");
        Category cat = categoryService.create("Services");

        // Créer 12 annonces publiées
        for (int i = 1; i <= 12; i++) {
            Annonce a = annonceService.create(
                    "Service " + i, "Description du service " + i,
                    "Paris", "m@t.com", user.getId(), cat.getId());
            annonceService.publish(a.getId(), user.getId());
        }

        // Page 0, taille 5 → 5 résultats
        List<Annonce> page0 = annonceService.findPublishedPaginated(0, 5);
        assertEquals(5, page0.size());

        // Page 1, taille 5 → 5 résultats
        List<Annonce> page1 = annonceService.findPublishedPaginated(1, 5);
        assertEquals(5, page1.size());

        // Page 2, taille 5 → 2 résultats
        List<Annonce> page2 = annonceService.findPublishedPaginated(2, 5);
        assertEquals(2, page2.size());

        // Comptage total
        assertEquals(12, annonceService.countPublished());
    }

    @Test
    @DisplayName("Workflow : archivage empêche la re-publication")
    void workflow_archivePreventsRepublish() {
        User user = userService.create("paul", "paul@test.com", "password123");
        Category cat = categoryService.create("Emploi");

        Annonce annonce = annonceService.create("Job", "Desc", "Paris", "m@t.com",
                user.getId(), cat.getId());

        // DRAFT → PUBLISHED → ARCHIVED
        annonceService.publish(annonce.getId(), user.getId());
        annonceService.archive(annonce.getId(), user.getId());

        // ARCHIVED → PUBLISHED doit échouer
        assertThrows(IllegalStateException.class,
                () -> annonceService.publish(annonce.getId(), user.getId()));

        // L'annonce archivée n'apparaît plus dans les recherches publiées
        assertEquals(0, annonceService.countPublished());
    }

    @Test
    @DisplayName("Workflow : suppression catégorie impossible si annonces liées")
    void workflow_cannotDeleteCategoryWithAnnonces() {
        User user = userService.create("luc", "luc@test.com", "password123");
        Category cat = categoryService.create("Animaux");

        annonceService.create("Chaton", "Desc", "Paris", "m@t.com",
                user.getId(), cat.getId());

        // La suppression doit échouer
        assertThrows(IllegalStateException.class,
                () -> categoryService.delete(cat.getId()));

        // La catégorie existe toujours
        assertTrue(categoryService.findById(cat.getId()).isPresent());
    }
}
