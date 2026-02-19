package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.dto.AnnoncePatchDTO;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.model.Annonce;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.model.AnnonceStatus;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.model.Category;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.exception.ForbiddenOperationException;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.model.User;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UpdateDeleteAnnonceServiceTest extends AnnonceServiceTestBase {

    @Test
    @DisplayName("update_shouldUpdateFields")
    void update_shouldUpdateFields() {
        Annonce annonce = annonce(1L, 1L, AnnonceStatus.DRAFT);
        Category category = new Category("Vehicules");
        category.setId(20L);

        when(annonceRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(annonce));
        when(categoryRepository.findById(20L)).thenReturn(Optional.of(category));
        when(annonceRepository.save(any(Annonce.class))).thenAnswer(i -> i.getArgument(0));

        Annonce result = annonceService.update(1L, 1L, "Nouveau", "Nouvelle", "Lyon", "new@test.com", 20L);

        assertEquals("Nouveau", result.getTitle());
        assertEquals("Nouvelle", result.getDescription());
        assertEquals("Lyon", result.getAdress());
        assertEquals("new@test.com", result.getMail());
        assertEquals(20L, result.getCategory().getId());
    }

    @Test
    @DisplayName("update_shouldThrow_whenNotOwner")
    void update_shouldThrow_whenNotOwner() {
        when(annonceRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(annonce(1L, 2L, AnnonceStatus.DRAFT)));

        assertThrows(ForbiddenOperationException.class,
                () -> annonceService.update(1L, 1L, "T", "D", "A", "m@test.com", 10L));
    }

    @Test
    @DisplayName("update_shouldThrow_whenPublished")
    void update_shouldThrow_whenPublished() {
        when(annonceRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(annonce(1L, 1L, AnnonceStatus.PUBLISHED)));

        assertThrows(IllegalStateException.class,
                () -> annonceService.update(1L, 1L, "T", "D", "A", "m@test.com", 10L));
    }

    @Test
    @DisplayName("delete_shouldDelete_whenArchived")
    void delete_shouldDelete_whenArchived() {
        Annonce annonce = annonce(1L, 1L, AnnonceStatus.ARCHIVED);
        when(annonceRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(annonce));

        annonceService.delete(1L, 1L);

        verify(annonceRepository).delete(annonce);
    }

    @Test
    @DisplayName("delete_shouldThrow_whenNotArchived")
    void delete_shouldThrow_whenNotArchived() {
        when(annonceRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(annonce(1L, 1L, AnnonceStatus.DRAFT)));

        assertThrows(IllegalStateException.class, () -> annonceService.delete(1L, 1L));
    }

    @Test
    @DisplayName("delete_shouldThrow_whenNotOwner")
    void delete_shouldThrow_whenNotOwner() {
        when(annonceRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(annonce(1L, 2L, AnnonceStatus.ARCHIVED)));

        assertThrows(ForbiddenOperationException.class, () -> annonceService.delete(1L, 1L));
    }

    @Test
    @DisplayName("patch_shouldUpdatePartialFields")
    void patch_shouldUpdatePartialFields() {
        Annonce annonce = annonce(1L, 1L, AnnonceStatus.DRAFT);
        AnnoncePatchDTO dto = new AnnoncePatchDTO();
        dto.setTitle("Patched");

        when(annonceRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(annonce));
        when(annonceRepository.save(any(Annonce.class))).thenAnswer(i -> i.getArgument(0));

        annonceService.patch(1L, 1L, dto);

        verify(annonceMapper).updateAnnonceFromPatchDTO(dto, annonce);
    }

    @Test
    @DisplayName("patch_shouldThrow_whenPublished")
    void patch_shouldThrow_whenPublished() {
        AnnoncePatchDTO dto = new AnnoncePatchDTO();
        dto.setTitle("Patched");
        when(annonceRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(annonce(1L, 1L, AnnonceStatus.PUBLISHED)));

        assertThrows(IllegalStateException.class, () -> annonceService.patch(1L, 1L, dto));
    }

    private Annonce annonce(Long annonceId, Long authorId, AnnonceStatus status) {
        User author = new User("author", "author@test.com", "pwd");
        author.setId(authorId);
        Category category = new Category("Immobilier");
        category.setId(10L);

        Annonce annonce = new Annonce("Titre", "Description", "Paris", "mail@test.com");
        annonce.setId(annonceId);
        annonce.setAuthor(author);
        annonce.setCategory(category);
        annonce.setStatus(status);
        return annonce;
    }
}
