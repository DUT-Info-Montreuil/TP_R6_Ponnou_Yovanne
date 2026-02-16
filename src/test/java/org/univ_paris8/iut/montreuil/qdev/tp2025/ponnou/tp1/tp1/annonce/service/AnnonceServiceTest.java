package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.model.Annonce;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.model.AnnonceStatus;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.repository.AnnonceRepository;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.model.Category;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.repository.CategoryRepository;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.config.EntityManagerUtil;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.exception.ResourceNotFoundException;
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
 * Niveau 2 – Tests unitaires AnnonceService (Mockito)
 * Règles métier : publication, archivage, recherche, pagination
 */
@ExtendWith(MockitoExtension.class)
class AnnonceServiceTest {

    @Mock private AnnonceRepository annonceRepository;
    @Mock private UserRepository userRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private EntityManager em;
    @Mock private EntityTransaction tx;

    private AnnonceService annonceService;
    private MockedStatic<EntityManagerUtil> mockedUtil;

    private User testUser;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        annonceService = new AnnonceService(annonceRepository, userRepository, categoryRepository);

        mockedUtil = mockStatic(EntityManagerUtil.class);
        mockedUtil.when(EntityManagerUtil::getEntityManager).thenReturn(em);
        lenient().when(em.getTransaction()).thenReturn(tx);
        lenient().when(tx.isActive()).thenReturn(true);

        testUser = new User("author", "author@test.com", "password123");
        testUser.setId(1L);

        testCategory = new Category("Immobilier");
        testCategory.setId(10L);
    }

    @AfterEach
    void tearDown() {
        mockedUtil.close();
    }

    // ==================== Tests création ====================

    @Test
    @DisplayName("create() crée une annonce en DRAFT")
    void create_shouldCreateDraftAnnonce() {
        when(userRepository.findById(em, 1L)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(em, 10L)).thenReturn(Optional.of(testCategory));
        when(annonceRepository.save(eq(em), any(Annonce.class))).thenAnswer(inv -> {
            Annonce a = inv.getArgument(1);
            a.setId(100L);
            return a;
        });

        Annonce annonce = annonceService.create("Appart F3", "Bel appartement",
                "Paris 10", "contact@test.com", 1L, 10L);

        assertEquals("Appart F3", annonce.getTitle());
        assertEquals(AnnonceStatus.DRAFT, annonce.getStatus());
        assertEquals(testUser, annonce.getAuthor());
        assertEquals(testCategory, annonce.getCategory());
        verify(tx).begin();
        verify(tx).commit();
    }

    @Test
    @DisplayName("create() échoue si l'auteur n'existe pas")
    void create_shouldFail_whenAuthorNotFound() {
        when(userRepository.findById(em, 999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> annonceService.create("Titre", "Desc", "Addr", "m@t.com", 999L, 10L));
        verify(tx).rollback();
    }

    @Test
    @DisplayName("create() échoue si la catégorie n'existe pas")
    void create_shouldFail_whenCategoryNotFound() {
        when(userRepository.findById(em, 1L)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(em, 999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> annonceService.create("Titre", "Desc", "Addr", "m@t.com", 1L, 999L));
        verify(tx).rollback();
    }

    // ==================== Tests publication (règles métier) ====================

    @Test
    @DisplayName("publish() passe une annonce DRAFT en PUBLISHED")
    void publish_shouldSetStatusToPublished() {
        Annonce annonce = buildAnnonce(1L, AnnonceStatus.DRAFT);

        when(annonceRepository.findOneWithFilters(eq(em), any(Map.class), anyString()))
                .thenReturn(Optional.of(annonce));
        when(annonceRepository.update(em, annonce)).thenReturn(annonce);

        Annonce published = annonceService.publish(1L, 1L);

        assertEquals(AnnonceStatus.PUBLISHED, published.getStatus());
        verify(tx).commit();
    }

    @Test
    @DisplayName("publish() échoue pour une annonce ARCHIVED")
    void publish_shouldFail_whenArchived() {
        Annonce annonce = buildAnnonce(1L, AnnonceStatus.ARCHIVED);

        when(annonceRepository.findOneWithFilters(eq(em), any(Map.class), anyString()))
                .thenReturn(Optional.of(annonce));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> annonceService.publish(1L, 1L));
        assertTrue(ex.getMessage().contains("archivée"));
    }

    @Test
    @DisplayName("publish() échoue pour un ID inexistant")
    void publish_shouldFail_whenNotFound() {
        when(annonceRepository.findOneWithFilters(eq(em), any(Map.class), anyString()))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> annonceService.publish(999L, 1L));
    }

    // ==================== Tests archivage ====================

    @Test
    @DisplayName("archive() passe une annonce en ARCHIVED")
    void archive_shouldSetStatusToArchived() {
        Annonce annonce = buildAnnonce(1L, AnnonceStatus.PUBLISHED);

        when(annonceRepository.findOneWithFilters(eq(em), any(Map.class), anyString()))
                .thenReturn(Optional.of(annonce));
        when(annonceRepository.update(em, annonce)).thenReturn(annonce);

        Annonce archived = annonceService.archive(1L, 1L);

        assertEquals(AnnonceStatus.ARCHIVED, archived.getStatus());
    }

    // ==================== Tests mise à jour ====================

    @Test
    @DisplayName("update() modifie les champs de l'annonce")
    void update_shouldModifyAnnonce() {
        Annonce annonce = buildAnnonce(1L, AnnonceStatus.DRAFT);
        Category newCat = new Category("Véhicules");
        newCat.setId(20L);

        when(annonceRepository.findOneWithFilters(eq(em), any(Map.class), anyString()))
                .thenReturn(Optional.of(annonce));
        when(categoryRepository.findById(em, 20L)).thenReturn(Optional.of(newCat));
        when(annonceRepository.update(em, annonce)).thenReturn(annonce);

        Annonce updated = annonceService.update(1L, 1L,
                "Nouveau", "Nouvelle desc", "Lyon", "new@t.com", 20L);

        assertEquals("Nouveau", updated.getTitle());
        assertEquals("Nouvelle desc", updated.getDescription());
        assertEquals("Lyon", updated.getAdress());
        assertEquals("new@t.com", updated.getMail());
        assertEquals(newCat, updated.getCategory());
    }

    // ==================== Tests recherche et pagination ====================

    @Test
    @DisplayName("findPublishedPaginated() délègue au repository avec filtre PUBLISHED")
    void findPublishedPaginated_shouldReturnOnlyPublished() {
        Annonce a = buildAnnonce(1L, AnnonceStatus.PUBLISHED);

        when(annonceRepository.findWithFilters(eq(em), any(Map.class), isNull(), isNull(),
                eq("date DESC"), anyString(), eq(0), eq(10)))
                .thenReturn(List.of(a));

        List<Annonce> results = annonceService.findPublishedPaginated(0, 10);

        assertEquals(1, results.size());
        assertEquals(AnnonceStatus.PUBLISHED, results.get(0).getStatus());
    }

    @Test
    @DisplayName("searchByKeywordPaginated() recherche dans titre et description")
    void searchByKeyword_shouldSearchInTitleAndDescription() {
        Annonce a = buildAnnonce(1L, AnnonceStatus.PUBLISHED);
        a.setTitle("Appartement lumineux");

        when(annonceRepository.findWithFilters(eq(em), any(Map.class), eq("lumineux"),
                any(String[].class), eq("date DESC"), anyString(), eq(0), eq(10)))
                .thenReturn(List.of(a));

        List<Annonce> results = annonceService.searchByKeywordPaginated("lumineux", 0, 10);

        assertEquals(1, results.size());
    }

    @Test
    @DisplayName("findByAuthorPaginated() filtre par auteur")
    void findByAuthor_shouldReturnOnlyAuthorAnnonces() {
        Annonce a = buildAnnonce(1L, AnnonceStatus.DRAFT);

        when(annonceRepository.findWithFilters(eq(em), any(Map.class), isNull(), isNull(),
                eq("date DESC"), anyString(), eq(0), eq(10)))
                .thenReturn(List.of(a));

        List<Annonce> results = annonceService.findByAuthorPaginated(1L, 0, 10);

        assertEquals(1, results.size());
    }

    @Test
    @DisplayName("countPublished() compte uniquement les annonces publiées")
    void countPublished_shouldCountCorrectly() {
        when(annonceRepository.countWithFilters(eq(em), any(Map.class))).thenReturn(3L);

        assertEquals(3, annonceService.countPublished());
    }

    // ==================== Tests suppression ====================

    @Test
    @DisplayName("delete() supprime l'annonce archivée")
    void delete_shouldRemoveAnnonce() {
        Annonce annonce = buildAnnonce(1L, AnnonceStatus.ARCHIVED);

        when(annonceRepository.findOneWithFilters(eq(em), any(Map.class), anyString()))
                .thenReturn(Optional.of(annonce));

        annonceService.delete(1L, 1L);

        verify(annonceRepository).delete(em, annonce);
        verify(tx).commit();
    }

    @Test
    @DisplayName("delete() échoue si l'annonce n'est pas archivée")
    void delete_shouldFail_whenNotArchived() {
        Annonce annonce = buildAnnonce(1L, AnnonceStatus.DRAFT);

        when(annonceRepository.findOneWithFilters(eq(em), any(Map.class), anyString()))
                .thenReturn(Optional.of(annonce));

        assertThrows(IllegalStateException.class,
                () -> annonceService.delete(1L, 1L));
    }

    // ==================== Vérification des appels ====================

    @Test
    @DisplayName("Chaque méthode du service utilise EntityManagerUtil")
    void service_shouldUseEntityManagerUtil() {
        when(userRepository.findById(em, 1L)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(em, 10L)).thenReturn(Optional.of(testCategory));
        when(annonceRepository.save(eq(em), any(Annonce.class))).thenAnswer(inv -> inv.getArgument(1));

        annonceService.create("Test", "Desc", "Addr", "m@t.com", 1L, 10L);

        mockedUtil.verify(EntityManagerUtil::getEntityManager, atLeastOnce());
    }

    // ==================== Helper ====================

    private Annonce buildAnnonce(Long id, AnnonceStatus status) {
        Annonce annonce = new Annonce("Titre", "Desc", "Paris", "m@t.com");
        annonce.setId(id);
        annonce.setStatus(status);
        annonce.setAuthor(testUser);
        annonce.setCategory(testCategory);
        return annonce;
    }
}
