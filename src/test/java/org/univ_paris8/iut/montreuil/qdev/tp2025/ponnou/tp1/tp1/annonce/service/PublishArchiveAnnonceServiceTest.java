package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.service;

import org.junit.jupiter.api.*;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.model.Annonce;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.model.AnnonceStatus;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.exception.ResourceNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PublishArchiveAnnonceServiceTest extends AnnonceServiceTestBase {

    @Test
    @DisplayName("publish() passe une annonce DRAFT en PUBLISHED")
    void publish_shouldSetStatusToPublished() {
        Annonce annonce = buildAnnonce(1L, AnnonceStatus.DRAFT);

        when(annonceRepository.findOneWithFilters(eq(em), anyMap(), anyString()))
                .thenReturn(Optional.of(annonce));
        when(annonceRepository.update(em, annonce)).thenReturn(annonce);

        Annonce published = annonceService.publish(1L, 1L);

        assertEquals(AnnonceStatus.PUBLISHED, published.getStatus());
        verify(tx).commit();
    }

    @Test
    @DisplayName("publish() échoue pour une annonce ARCHIVED")
    void publish_shouldFail_whenArchived() {
        Annonce annonce = buildAnnonce(1L, AnnonceStatus.ARCHIVED);

        when(annonceRepository.findOneWithFilters(eq(em), anyMap(), anyString()))
                .thenReturn(Optional.of(annonce));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> annonceService.publish(1L, 1L));
        assertTrue(ex.getMessage().contains("archivée"));
    }

    @Test
    @DisplayName("publish() échoue pour un ID inexistant")
    void publish_shouldFail_whenNotFound() {
        when(annonceRepository.findOneWithFilters(eq(em), anyMap(), anyString()))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> annonceService.publish(999L, 1L));
    }

    @Test
    @DisplayName("archive() passe une annonce en ARCHIVED")
    void archive_shouldSetStatusToArchived() {
        Annonce annonce = buildAnnonce(1L, AnnonceStatus.PUBLISHED);

        when(annonceRepository.findOneWithFilters(eq(em), anyMap(), anyString()))
                .thenReturn(Optional.of(annonce));
        when(annonceRepository.update(em, annonce)).thenReturn(annonce);

        Annonce archived = annonceService.archive(1L, 1L);

        assertEquals(AnnonceStatus.ARCHIVED, archived.getStatus());
    }
}
