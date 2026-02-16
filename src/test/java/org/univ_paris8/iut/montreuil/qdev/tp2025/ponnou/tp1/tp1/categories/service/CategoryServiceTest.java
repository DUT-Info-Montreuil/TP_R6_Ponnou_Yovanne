package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.categories.service;

import org.junit.jupiter.api.*;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.model.Annonce;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.model.AnnonceStatus;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.model.Category;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.service.CategoryService;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.config.EntityManagerUtil;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.model.User;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.List;
import java.util.Optional;

import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Niveau 2 – Tests Service CategoryService (unitaires avec Mockito)
 * Règles métier : unicité du label, suppression impossible si annonces liées
 */
class CategoryServiceTest {

    private static EntityManagerFactory emf;
    private CategoryService categoryService;
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
        categoryService = new CategoryService();
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

    // ==================== Tests création ====================

    @Test
    @DisplayName("create() crée une catégorie avec succès")
    void create_shouldCreateCategory() {
        Category cat = categoryService.create("Immobilier");

        assertNotNull(cat.getId());
        assertEquals("Immobilier", cat.getLabel());
    }

    @Test
    @DisplayName("create() échoue si le label existe déjà")
    void create_shouldFail_whenDuplicateLabel() {
        categoryService.create("Immobilier");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> categoryService.create("Immobilier"));
        assertTrue(ex.getMessage().contains("catégorie existe"));
    }

    // ==================== Tests mise à jour ====================

    @Test
    @DisplayName("update() modifie le label")
    void update_shouldModifyLabel() {
        Category cat = categoryService.create("Ancien");

        Category updated = categoryService.update(cat.getId(), "Nouveau");

        assertEquals("Nouveau", updated.getLabel());
    }

    @Test
    @DisplayName("update() échoue si le nouveau label est déjà pris")
    void update_shouldFail_whenLabelAlreadyTaken() {
        categoryService.create("Immobilier");
        Category vehicules = categoryService.create("Véhicules");

        assertThrows(IllegalArgumentException.class,
                () -> categoryService.update(vehicules.getId(), "Immobilier"));
    }

    @Test
    @DisplayName("update() permet de garder le même label (pas de faux doublon)")
    void update_shouldAllow_sameLabelOnSameCategory() {
        Category cat = categoryService.create("Immobilier");

        Category updated = categoryService.update(cat.getId(), "Immobilier");

        assertEquals("Immobilier", updated.getLabel());
    }

    // ==================== Tests recherche ====================

    @Test
    @DisplayName("findById() retourne la catégorie existante")
    void findById_shouldReturnCategory() {
        Category cat = categoryService.create("Emploi");

        Optional<Category> found = categoryService.findById(cat.getId());

        assertTrue(found.isPresent());
        assertEquals("Emploi", found.get().getLabel());
    }

    @Test
    @DisplayName("findByLabel() retourne la catégorie par label")
    void findByLabel_shouldReturnCategory() {
        categoryService.create("Services");

        Optional<Category> found = categoryService.findByLabel("Services");

        assertTrue(found.isPresent());
    }

    @Test
    @DisplayName("findAll() retourne toutes les catégories triées par label")
    void findAll_shouldReturnSortedCategories() {
        categoryService.create("Véhicules");
        categoryService.create("Animaux");
        categoryService.create("Immobilier");

        List<Category> all = categoryService.findAll();

        assertEquals(3, all.size());
        assertEquals("Animaux", all.get(0).getLabel());
        assertEquals("Immobilier", all.get(1).getLabel());
        assertEquals("Véhicules", all.get(2).getLabel());
    }

    // ==================== Tests suppression (règle métier) ====================

    @Test
    @DisplayName("delete() supprime une catégorie sans annonces")
    void delete_shouldRemoveEmptyCategory() {
        Category cat = categoryService.create("Vide");

        categoryService.delete(cat.getId());

        assertTrue(categoryService.findById(cat.getId()).isEmpty());
    }

    @Test
    @DisplayName("delete() échoue si la catégorie contient des annonces")
    void delete_shouldFail_whenCategoryHasAnnonces() {
        Category cat = categoryService.create("Avec annonces");

        // Créer une annonce liée à cette catégorie
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        User user = new User("testuser", "test@test.com", "password123");
        em.persist(user);
        Annonce annonce = new Annonce("Titre", "Desc", "Addr", "m@t.com");
        annonce.setAuthor(user);
        annonce.setCategory(em.find(Category.class, cat.getId()));
        annonce.setStatus(AnnonceStatus.DRAFT);
        em.persist(annonce);
        em.getTransaction().commit();
        em.close();

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> categoryService.delete(cat.getId()));
        assertTrue(ex.getMessage().contains("annonces"));
    }

    // ==================== Vérification des appels ====================

    @Test
    @DisplayName("Chaque opération du service appelle EntityManagerUtil")
    void service_shouldCallEntityManagerUtil() {
        categoryService.create("Test");
        mockedUtil.verify(EntityManagerUtil::getEntityManager, atLeastOnce());
    }
}
