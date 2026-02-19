package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.model.Category;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.exception.ResourceNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class CreateUpdateCategoryServiceTest extends CategoryServiceTestBase {

    @Test
    @DisplayName("create_shouldSave")
    void create_shouldSave() {
        when(categoryRepository.existsByLabel("Immobilier")).thenReturn(false);
        when(categoryMapper.toEntityForCreate("Immobilier")).thenReturn(new Category("Immobilier"));
        when(categoryRepository.save(any(Category.class))).thenAnswer(i -> {
            Category category = i.getArgument(0);
            category.setId(1L);
            return category;
        });

        Category result = categoryService.create("Immobilier");

        assertEquals(1L, result.getId());
        assertEquals("Immobilier", result.getLabel());
    }

    @Test
    @DisplayName("create_shouldThrow_whenLabelExists")
    void create_shouldThrow_whenLabelExists() {
        when(categoryRepository.existsByLabel("Immobilier")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> categoryService.create("Immobilier"));
    }

    @Test
    @DisplayName("update_shouldUpdate")
    void update_shouldUpdate() {
        Category existing = new Category("Ancien");
        existing.setId(1L);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.existsByLabel("Nouveau")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(i -> i.getArgument(0));

        Category result = categoryService.update(1L, "Nouveau");

        assertEquals("Nouveau", result.getLabel());
    }

    @Test
    @DisplayName("update_shouldThrow_whenNotFound")
    void update_shouldThrow_whenNotFound() {
        when(categoryRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.update(404L, "Nouveau"));
    }

    @Test
    @DisplayName("update_shouldThrow_whenNewLabelExists")
    void update_shouldThrow_whenNewLabelExists() {
        Category existing = new Category("Ancien");
        existing.setId(1L);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.existsByLabel("Immo")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> categoryService.update(1L, "Immo"));
    }
}
