package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.model.Annonce;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.model.AnnonceStatus;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.model.Category;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.exception.ForbiddenOperationException;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.model.User;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class PublishArchiveAnnonceServiceTest extends AnnonceServiceTestBase {

    @Test
    @DisplayName("publish_shouldSetStatusPublished")
    void publish_shouldSetStatusPublished() {
        Annonce annonce = annonce(1L, 1L, AnnonceStatus.DRAFT);
        when(annonceRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(annonce));
        when(annonceRepository.save(any(Annonce.class))).thenAnswer(i -> i.getArgument(0));

        Annonce result = annonceService.publish(1L, 1L);

        assertEquals(AnnonceStatus.PUBLISHED, result.getStatus());
    }

    @Test
    @DisplayName("publish_shouldThrow_whenNotOwner")
    void publish_shouldThrow_whenNotOwner() {
        Annonce annonce = annonce(1L, 2L, AnnonceStatus.DRAFT);
        when(annonceRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(annonce));

        assertThrows(ForbiddenOperationException.class, () -> annonceService.publish(1L, 1L));
    }

    @Test
    @DisplayName("publish_shouldThrow_whenArchived")
    void publish_shouldThrow_whenArchived() {
        Annonce annonce = annonce(1L, 1L, AnnonceStatus.ARCHIVED);
        when(annonceRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(annonce));

        assertThrows(IllegalStateException.class, () -> annonceService.publish(1L, 1L));
    }

    @Test
    @DisplayName("archive_shouldSetStatusArchived")
    void archive_shouldSetStatusArchived() {
        Annonce annonce = annonce(1L, 1L, AnnonceStatus.PUBLISHED);
        when(annonceRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(annonce));
        when(annonceRepository.save(any(Annonce.class))).thenAnswer(i -> i.getArgument(0));

        Annonce result = annonceService.archive(1L, 1L);

        assertEquals(AnnonceStatus.ARCHIVED, result.getStatus());
    }

    @Test
    @DisplayName("archive_shouldThrow_whenNotOwner")
    void archive_shouldThrow_whenNotOwner() {
        Annonce annonce = annonce(1L, 2L, AnnonceStatus.DRAFT);
        when(annonceRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(annonce));

        assertThrows(ForbiddenOperationException.class, () -> annonceService.archive(1L, 1L));
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
