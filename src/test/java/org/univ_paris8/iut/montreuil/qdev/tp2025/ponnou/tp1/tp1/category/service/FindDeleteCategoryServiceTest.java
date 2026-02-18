package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.service;

import org.junit.jupiter.api.*;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.model.Category;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FindDeleteCategoryServiceTest extends CategoryServiceTestBase {

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
}
