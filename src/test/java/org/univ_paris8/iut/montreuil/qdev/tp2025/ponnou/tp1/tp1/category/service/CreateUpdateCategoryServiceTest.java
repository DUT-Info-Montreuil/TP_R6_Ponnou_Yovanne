package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.service;

import org.junit.jupiter.api.*;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.model.Category;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.config.EntityManagerUtil;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CreateUpdateCategoryServiceTest extends CategoryServiceTestBase {

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

    @Test
    @DisplayName("Chaque opération du service appelle EntityManagerUtil")
    void service_shouldCallEntityManagerUtil() {
        when(categoryRepository.countWithFilters(eq(em), any())).thenReturn(0L);
        when(categoryRepository.save(eq(em), any(Category.class))).thenAnswer(inv -> inv.getArgument(1));

        categoryService.create("Test");

        mockedUtil.verify(EntityManagerUtil::getEntityManager, atLeastOnce());
    }
}
