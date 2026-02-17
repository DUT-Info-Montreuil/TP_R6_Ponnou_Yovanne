package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.repository.AnnonceRepository;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.model.Category;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.repository.CategoryRepository;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.config.EntityManagerUtil;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private AnnonceRepository annonceRepository;
    @Mock
    private EntityManager em;
    @Mock
    private EntityTransaction tx;

    private CategoryService categoryService;
    private MockedStatic<EntityManagerUtil> mockedUtil;

    @BeforeEach
    void setUp() {
        categoryService = new CategoryService(categoryRepository, annonceRepository);

        mockedUtil = mockStatic(EntityManagerUtil.class);
        mockedUtil.when(EntityManagerUtil::getEntityManager).thenReturn(em);
        lenient().when(em.getTransaction()).thenReturn(tx);
    }

    @AfterEach
    void tearDown() {
        mockedUtil.close();
    }

    // ==================== Tests création ====================

    @Test
    @DisplayName("create() crée une catégorie avec succès")
    void create_shouldCreateCategory() {
        when(categoryRepository.countWithFilters(eq(em), eq(Map.of("label", "Immobilier")))).thenReturn(0L);
        when(categoryRepository.save(eq(em), any(Category.class))).thenAnswer(inv -> {
            Category c = inv.getArgument(1);
            c.setId(1L);
            return c;
        });

        Category cat = categoryService.create("Immobilier");

        assertNotNull(cat.getId());
        assertEquals("Immobilier", cat.getLabel());
        verify(tx).commit();
    }

    @Test
    @DisplayName("create() échoue si le label existe déjà")
    void create_shouldFail_whenDuplicateLabel() {
        when(categoryRepository.countWithFilters(eq(em), eq(Map.of("label", "Immobilier")))).thenReturn(1L);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> categoryService.create("Immobilier"));
        assertTrue(ex.getMessage().contains("catégorie existe"));
    }

    // ==================== Tests mise à jour ====================

    @Test
    @DisplayName("update() modifie le label")
    void update_shouldModifyLabel() {
        Category existing = new Category("Ancien");
        existing.setId(1L);

        when(categoryRepository.findById(em, 1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.countWithFilters(eq(em), eq(Map.of("label", "Nouveau")))).thenReturn(0L);
        when(categoryRepository.update(em, existing)).thenReturn(existing);

        Category updated = categoryService.update(1L, "Nouveau");

        assertEquals("Nouveau", updated.getLabel());
        verify(tx).commit();
    }

    @Test
    @DisplayName("update() échoue si le nouveau label est déjà pris")
    void update_shouldFail_whenLabelAlreadyTaken() {
        Category vehicules = new Category("Véhicules");
        vehicules.setId(2L);

        when(categoryRepository.findById(em, 2L)).thenReturn(Optional.of(vehicules));
        when(categoryRepository.countWithFilters(eq(em), eq(Map.of("label", "Immobilier")))).thenReturn(1L);

        assertThrows(IllegalArgumentException.class,
                () -> categoryService.update(2L, "Immobilier"));
    }

    @Test
    @DisplayName("update() permet de garder le même label (pas de faux doublon)")
    void update_shouldAllow_sameLabelOnSameCategory() {
        Category existing = new Category("Immobilier");
        existing.setId(1L);

        when(categoryRepository.findById(em, 1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.update(em, existing)).thenReturn(existing);

        Category updated = categoryService.update(1L, "Immobilier");

        assertEquals("Immobilier", updated.getLabel());
        verify(categoryRepository, never()).countWithFilters(any(), any());
    }

    // ==================== Tests recherche ====================

    @Test
    @DisplayName("findById() retourne la catégorie existante")
    void findById_shouldReturnCategory() {
        Category cat = new Category("Emploi");
        cat.setId(1L);

        when(categoryRepository.findById(em, 1L)).thenReturn(Optional.of(cat));

        Optional<Category> found = categoryService.findById(1L);

        assertTrue(found.isPresent());
        assertEquals("Emploi", found.get().getLabel());
    }

    @Test
    @DisplayName("findByLabel() retourne la catégorie par label")
    void findByLabel_shouldReturnCategory() {
        Category cat = new Category("Services");
        when(categoryRepository.findOneWithFilters(eq(em), eq(Map.of("label", "Services"))))
                .thenReturn(Optional.of(cat));

        Optional<Category> found = categoryService.findByLabel("Services");

        assertTrue(found.isPresent());
    }

    @Test
    @DisplayName("findAll() retourne toutes les catégories triées par label")
    void findAll_shouldReturnSortedCategories() {
        when(categoryRepository.findWithFilters(eq(em), isNull(), isNull(), isNull(),
                eq("label ASC"), isNull()))
                .thenReturn(List.of(
                        new Category("Animaux"),
                        new Category("Immobilier"),
                        new Category("Véhicules")));

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
        when(annonceRepository.countWithFilters(eq(em), eq(Map.of("category.id", 1L)))).thenReturn(0L);
        when(categoryRepository.deleteById(em, 1L)).thenReturn(true);

        categoryService.delete(1L);

        verify(categoryRepository).deleteById(em, 1L);
        verify(tx).commit();
    }

    @Test
    @DisplayName("delete() échoue si la catégorie contient des annonces")
    void delete_shouldFail_whenCategoryHasAnnonces() {
        when(annonceRepository.countWithFilters(eq(em), eq(Map.of("category.id", 1L)))).thenReturn(3L);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> categoryService.delete(1L));
        assertTrue(ex.getMessage().contains("annonces"));
    }

    // ==================== Vérification des appels ====================

    @Test
    @DisplayName("Chaque opération du service appelle EntityManagerUtil")
    void service_shouldCallEntityManagerUtil() {
        when(categoryRepository.countWithFilters(eq(em), any(Map.class))).thenReturn(0L);
        when(categoryRepository.save(eq(em), any(Category.class))).thenAnswer(inv -> inv.getArgument(1));

        categoryService.create("Test");

        mockedUtil.verify(EntityManagerUtil::getEntityManager, atLeastOnce());
    }
}
