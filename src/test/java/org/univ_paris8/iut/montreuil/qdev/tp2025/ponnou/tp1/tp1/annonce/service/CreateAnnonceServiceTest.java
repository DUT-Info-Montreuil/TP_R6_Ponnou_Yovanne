package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.model.Annonce;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.model.Category;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.exception.ResourceNotFoundException;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.model.User;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class CreateAnnonceServiceTest extends AnnonceServiceTestBase {

    @Test
    @DisplayName("create_shouldReturnSavedAnnonce")
    void create_shouldReturnSavedAnnonce() {
        User author = new User("author", "author@test.com", "pwd");
        author.setId(1L);
        Category category = new Category("Immobilier");
        category.setId(10L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        when(categoryRepository.findById(10L)).thenReturn(Optional.of(category));
        when(annonceRepository.save(any(Annonce.class))).thenAnswer(invocation -> {
            Annonce annonce = invocation.getArgument(0);
            annonce.setId(100L);
            return annonce;
        });

        Annonce result = annonceService.create("Titre", "Description", "Paris", "mail@test.com", 1L, 10L);

        assertNotNull(result.getId());
        assertEquals("Titre", result.getTitle());
        assertEquals(author, result.getAuthor());
        assertEquals(category, result.getCategory());
    }

    @Test
    @DisplayName("create_shouldThrow_whenUserNotFound")
    void create_shouldThrow_whenUserNotFound() {
        when(userRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> annonceService.create("Titre", "Description", "Paris", "mail@test.com", 404L, 10L));
    }

    @Test
    @DisplayName("create_shouldThrow_whenCategoryNotFound")
    void create_shouldThrow_whenCategoryNotFound() {
        User author = new User("author", "author@test.com", "pwd");
        author.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> annonceService.create("Titre", "Description", "Paris", "mail@test.com", 1L, 999L));
    }
}
