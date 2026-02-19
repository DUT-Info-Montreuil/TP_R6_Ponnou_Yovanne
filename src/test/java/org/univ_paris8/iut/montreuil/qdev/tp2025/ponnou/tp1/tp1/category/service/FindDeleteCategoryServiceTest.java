package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.model.AnnonceStatus;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.model.Category;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.exception.ResourceNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FindDeleteCategoryServiceTest extends CategoryServiceTestBase {

    @Test
    @DisplayName("delete_shouldDelete_whenNoPublishedAnnonces")
    void delete_shouldDelete_whenNoPublishedAnnonces() {
        when(categoryRepository.existsById(1L)).thenReturn(true);
        when(annonceRepository.countByCategoryIdAndStatus(1L, AnnonceStatus.PUBLISHED)).thenReturn(0L);

        categoryService.delete(1L);

        verify(categoryRepository).deleteById(1L);
    }

    @Test
    @DisplayName("delete_shouldThrow_whenPublishedAnnoncesExist")
    void delete_shouldThrow_whenPublishedAnnoncesExist() {
        when(categoryRepository.existsById(1L)).thenReturn(true);
        when(annonceRepository.countByCategoryIdAndStatus(1L, AnnonceStatus.PUBLISHED)).thenReturn(2L);

        assertThrows(IllegalStateException.class, () -> categoryService.delete(1L));
    }

    @Test
    @DisplayName("delete_shouldThrow_whenNotFound")
    void delete_shouldThrow_whenNotFound() {
        when(categoryRepository.existsById(404L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> categoryService.delete(404L));
    }

    @Test
    @DisplayName("findById_shouldReturnCategory")
    void findById_shouldReturnCategory() {
        Category category = new Category("Immo");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        Optional<Category> result = categoryService.findById(1L);

        assertTrue(result.isPresent());
    }

    @Test
    @DisplayName("findAllPaginated_shouldReturnPage")
    void findAllPaginated_shouldReturnPage() {
        when(categoryRepository.findAll(PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(new Category("Immo"))));

        var result = categoryService.findAllPaginated(PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
    }
}
