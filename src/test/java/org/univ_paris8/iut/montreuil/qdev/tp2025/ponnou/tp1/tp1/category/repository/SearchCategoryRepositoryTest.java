package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.repository;

import org.junit.jupiter.api.*;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.model.Category;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SearchCategoryRepositoryTest extends CategoryRepositoryTestBase {

    @Test
    @DisplayName("findWithFilters() avec tri alphabetique")
    void findWithFilters_shouldReturnSortedByLabel() {
        em.getTransaction().begin();
        em.persist(new Category("Vehicules"));
        em.persist(new Category("Animaux"));
        em.persist(new Category("Immobilier"));
        em.getTransaction().commit();

        List<Category> categories = categoryRepository.findWithFilters(
                em, null, null, null, "label ASC", null);

        assertEquals(3, categories.size());
        assertEquals("Animaux", categories.get(0).getLabel());
        assertEquals("Immobilier", categories.get(1).getLabel());
        assertEquals("Vehicules", categories.get(2).getLabel());
    }

    @Test
    @DisplayName("findOneWithFilters() trouve par label")
    void findOneWithFilters_shouldFindByLabel() {
        em.getTransaction().begin();
        categoryRepository.save(em, new Category("Emploi"));

        Optional<Category> found = categoryRepository.findOneWithFilters(em, Map.of("label", "Emploi"));

        assertTrue(found.isPresent());
        assertEquals("Emploi", found.get().getLabel());
    }
}
