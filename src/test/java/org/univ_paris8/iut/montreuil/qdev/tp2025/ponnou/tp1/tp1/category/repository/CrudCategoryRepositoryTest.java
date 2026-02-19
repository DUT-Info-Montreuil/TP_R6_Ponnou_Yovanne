package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.model.Category;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CrudCategoryRepositoryTest extends CategoryRepositoryTestBase {

    @Test
    @DisplayName("save_shouldPersistCategory")
    void save_shouldPersistCategory() {
        Category category = new Category("Vehicules");

        Category saved = categoryRepository.save(category);

        assertTrue(saved.getId() != null);
        assertEquals("Vehicules", saved.getLabel());
    }

    @Test
    @DisplayName("findById_shouldReturnCategory")
    void findById_shouldReturnCategory() {
        Category found = categoryRepository.findById(existing.getId()).orElseThrow();

        assertEquals(existing.getLabel(), found.getLabel());
    }

    @Test
    @DisplayName("delete_shouldRemoveCategory")
    void delete_shouldRemoveCategory() {
        categoryRepository.deleteById(existing.getId());

        assertTrue(categoryRepository.findById(existing.getId()).isEmpty());
    }
}
