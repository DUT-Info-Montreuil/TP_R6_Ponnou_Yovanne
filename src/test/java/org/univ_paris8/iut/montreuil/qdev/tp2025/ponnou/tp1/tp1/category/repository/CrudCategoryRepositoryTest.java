package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.repository;

import org.junit.jupiter.api.*;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.model.Category;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CrudCategoryRepositoryTest extends CategoryRepositoryTestBase {

    @Test
    @DisplayName("save() persiste une categorie")
    void save_shouldPersistCategory() {
        Category cat = new Category("Immobilier");
        em.getTransaction().begin();
        Category saved = categoryRepository.save(em, cat);

        assertNotNull(saved.getId());
        assertEquals("Immobilier", saved.getLabel());
    }

    @Test
    @DisplayName("findById() retourne la categorie existante")
    void findById_shouldReturnCategory() {
        Category cat = new Category("Vehicules");
        em.getTransaction().begin();
        categoryRepository.save(em, cat);

        Optional<Category> found = categoryRepository.findById(em, cat.getId());

        assertTrue(found.isPresent());
        assertEquals("Vehicules", found.get().getLabel());
    }

    @Test
    @DisplayName("update() modifie le label")
    void update_shouldModifyLabel() {
        Category cat = new Category("Ancien label");
        em.getTransaction().begin();
        categoryRepository.save(em, cat);
        em.getTransaction().commit();

        cat.setLabel("Nouveau label");
        em.getTransaction().begin();
        Category updated = categoryRepository.update(em, cat);

        assertEquals("Nouveau label", updated.getLabel());
    }

    @Test
    @DisplayName("deleteById() supprime la categorie")
    void deleteById_shouldRemoveCategory() {
        Category cat = new Category("A supprimer");
        em.getTransaction().begin();
        categoryRepository.save(em, cat);
        Long id = cat.getId();
        em.getTransaction().commit();

        em.getTransaction().begin();
        categoryRepository.deleteById(em, id);

        assertTrue(categoryRepository.findById(em, id).isEmpty());
    }
}
