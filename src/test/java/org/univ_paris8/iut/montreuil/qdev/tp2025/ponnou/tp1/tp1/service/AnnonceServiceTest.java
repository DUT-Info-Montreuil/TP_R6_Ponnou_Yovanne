package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.service;

import org.junit.jupiter.api.*;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.model.Annonce;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.service.AnnonceService;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.model.AnnonceStatus;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.model.Category;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.config.EntityManagerUtil;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.model.User;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.exception.ResourceNotFoundException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.List;

import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Niveau 2 – Tests Service AnnonceService (unitaires avec Mockito)
 * Règles métier : publication, archivage, recherche, pagination
 */
class AnnonceServiceTest {

    private static EntityManagerFactory emf;
    private AnnonceService annonceService;
    private MockedStatic<EntityManagerUtil> mockedUtil;

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
        annonceService = new AnnonceService();
        mockedUtil = mockStatic(EntityManagerUtil.class);
        mockedUtil.when(EntityManagerUtil::getEntityManager).thenAnswer(inv -> emf.createEntityManager());

        // Préparer les données de référence
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        testUser = new User("author", "author@test.com", "password123");
        testCategory = new Category("Immobilier");
        em.persist(testUser);
        em.persist(testCategory);
        em.getTransaction().commit();
        em.close();
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

    // ==================== Tests création ====================

    @Test
    @DisplayName("create() crée une annonce en DRAFT")
    void create_shouldCreateDraftAnnonce() {
        Annonce annonce = annonceService.create("Appart F3", "Bel appartement",
                "Paris 10", "contact@test.com", testUser.getId(), testCategory.getId());

        assertNotNull(annonce.getId());
        assertEquals("Appart F3", annonce.getTitle());
        assertEquals(AnnonceStatus.DRAFT, annonce.getStatus());
        assertNotNull(annonce.getDate());
    }

    @Test
    @DisplayName("create() échoue si l'auteur n'existe pas")
    void create_shouldFail_whenAuthorNotFound() {
        assertThrows(ResourceNotFoundException.class,
                () -> annonceService.create("Titre", "Desc", "Addr", "m@t.com",
                        999L, testCategory.getId()));
    }

    @Test
    @DisplayName("create() échoue si la catégorie n'existe pas")
    void create_shouldFail_whenCategoryNotFound() {
        assertThrows(ResourceNotFoundException.class,
                () -> annonceService.create("Titre", "Desc", "Addr", "m@t.com",
                        testUser.getId(), 999L));
    }

    // ==================== Tests publication (règles métier) ====================

    @Test
    @DisplayName("publish() passe une annonce DRAFT en PUBLISHED")
    void publish_shouldSetStatusToPublished() {
        Annonce annonce = annonceService.create("A publier", "Desc",
                "Paris", "m@t.com", testUser.getId(), testCategory.getId());

        Annonce published = annonceService.publish(annonce.getId(), testUser.getId());

        assertEquals(AnnonceStatus.PUBLISHED, published.getStatus());
    }

    @Test
    @DisplayName("publish() échoue pour une annonce ARCHIVED")
    void publish_shouldFail_whenArchived() {
        Annonce annonce = annonceService.create("A archiver", "Desc",
                "Paris", "m@t.com", testUser.getId(), testCategory.getId());
        annonceService.publish(annonce.getId(), testUser.getId());
        annonceService.archive(annonce.getId(), testUser.getId());

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> annonceService.publish(annonce.getId(), testUser.getId()));
        assertTrue(ex.getMessage().contains("archivée"));
    }

    @Test
    @DisplayName("publish() échoue pour un ID inexistant")
    void publish_shouldFail_whenNotFound() {
        assertThrows(ResourceNotFoundException.class,
                () -> annonceService.publish(999L, testUser.getId()));
    }

    // ==================== Tests archivage ====================

    @Test
    @DisplayName("archive() passe une annonce en ARCHIVED")
    void archive_shouldSetStatusToArchived() {
        Annonce annonce = annonceService.create("A archiver", "Desc",
                "Paris", "m@t.com", testUser.getId(), testCategory.getId());
        annonceService.publish(annonce.getId(), testUser.getId());

        Annonce archived = annonceService.archive(annonce.getId(), testUser.getId());

        assertEquals(AnnonceStatus.ARCHIVED, archived.getStatus());
    }

    // ==================== Tests mise à jour ====================

    @Test
    @DisplayName("update() modifie les champs de l'annonce")
    void update_shouldModifyAnnonce() {
        Annonce annonce = annonceService.create("Ancien", "Ancienne desc",
                "Paris", "m@t.com", testUser.getId(), testCategory.getId());

        Annonce updated = annonceService.update(annonce.getId(), testUser.getId(),
                "Nouveau", "Nouvelle desc", "Lyon", "new@t.com", testCategory.getId());

        assertEquals("Nouveau", updated.getTitle());
        assertEquals("Nouvelle desc", updated.getDescription());
        assertEquals("Lyon", updated.getAdress());
        assertEquals("new@t.com", updated.getMail());
    }

    // ==================== Tests recherche et pagination ====================

    @Test
    @DisplayName("findPublishedPaginated() ne retourne que les annonces publiées")
    void findPublishedPaginated_shouldReturnOnlyPublished() {
        annonceService.create("Draft", "D", "P", "m@t.com", testUser.getId(), testCategory.getId());
        Annonce a2 = annonceService.create("Published", "D", "P", "m@t.com", testUser.getId(), testCategory.getId());
        annonceService.publish(a2.getId(), testUser.getId());

        List<Annonce> results = annonceService.findPublishedPaginated(0, 10);

        assertEquals(1, results.size());
        assertEquals(AnnonceStatus.PUBLISHED, results.get(0).getStatus());
    }

    @Test
    @DisplayName("searchByKeywordPaginated() recherche dans titre et description")
    void searchByKeyword_shouldSearchInTitleAndDescription() {
        Annonce a1 = annonceService.create("Appartement lumineux", "Centre ville",
                "P", "m@t.com", testUser.getId(), testCategory.getId());
        annonceService.publish(a1.getId(), testUser.getId());
        Annonce a2 = annonceService.create("Studio", "Très lumineux et calme",
                "P", "m@t.com", testUser.getId(), testCategory.getId());
        annonceService.publish(a2.getId(), testUser.getId());
        Annonce a3 = annonceService.create("Maison", "Grand jardin",
                "P", "m@t.com", testUser.getId(), testCategory.getId());
        annonceService.publish(a3.getId(), testUser.getId());

        List<Annonce> results = annonceService.searchByKeywordPaginated("lumineux", 0, 10);

        assertEquals(2, results.size());
    }

    @Test
    @DisplayName("findByAuthorPaginated() filtre par auteur")
    void findByAuthor_shouldReturnOnlyAuthorAnnonces() {
        // Créer un second auteur
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        User otherUser = new User("other", "other@test.com", "password123");
        em.persist(otherUser);
        em.getTransaction().commit();
        em.close();

        annonceService.create("Par author", "D", "P", "m@t.com", testUser.getId(), testCategory.getId());
        annonceService.create("Par other", "D", "P", "m@t.com", otherUser.getId(), testCategory.getId());

        List<Annonce> results = annonceService.findByAuthorPaginated(testUser.getId(), 0, 10);

        assertEquals(1, results.size());
    }

    @Test
    @DisplayName("countPublished() compte uniquement les annonces publiées")
    void countPublished_shouldCountCorrectly() {
        annonceService.create("Draft", "D", "P", "m@t.com", testUser.getId(), testCategory.getId());
        Annonce a = annonceService.create("Pub", "D", "P", "m@t.com", testUser.getId(), testCategory.getId());
        annonceService.publish(a.getId(), testUser.getId());

        assertEquals(1, annonceService.countPublished());
        assertEquals(2, annonceService.count());
    }

    // ==================== Tests suppression ====================

    @Test
    @DisplayName("delete() supprime l'annonce archivée")
    void delete_shouldRemoveAnnonce() {
        Annonce annonce = annonceService.create("A suppr", "D", "P", "m@t.com",
                testUser.getId(), testCategory.getId());

        // Archivage obligatoire avant suppression (DRAFT → PUBLISHED → ARCHIVED)
        annonceService.publish(annonce.getId(), testUser.getId());
        annonceService.archive(annonce.getId(), testUser.getId());

        annonceService.delete(annonce.getId(), testUser.getId());

        assertTrue(annonceService.findById(annonce.getId()).isEmpty());
    }

    @Test
    @DisplayName("delete() échoue si l'annonce n'est pas archivée")
    void delete_shouldFail_whenNotArchived() {
        Annonce annonce = annonceService.create("Non archivée", "D", "P", "m@t.com",
                testUser.getId(), testCategory.getId());

        assertThrows(IllegalStateException.class,
                () -> annonceService.delete(annonce.getId(), testUser.getId()));
    }

    // ==================== Vérification des appels ====================

    @Test
    @DisplayName("Chaque méthode du service utilise EntityManagerUtil")
    void service_shouldUseEntityManagerUtil() {
        annonceService.create("Test", "Desc", "Addr", "m@t.com",
                testUser.getId(), testCategory.getId());

        mockedUtil.verify(EntityManagerUtil::getEntityManager, atLeastOnce());
    }
}
