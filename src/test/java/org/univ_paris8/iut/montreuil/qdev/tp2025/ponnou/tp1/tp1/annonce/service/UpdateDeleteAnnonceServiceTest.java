package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.service;

import org.junit.jupiter.api.*;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.model.Annonce;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.model.AnnonceStatus;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.model.Category;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UpdateDeleteAnnonceServiceTest extends AnnonceServiceTestBase {

    @Test
    @DisplayName("update() modifie les champs de l'annonce")
    void update_shouldModifyAnnonce() {
        Annonce annonce = buildAnnonce(1L, AnnonceStatus.DRAFT);
        Category newCat = new Category("Véhicules");
        newCat.setId(20L);

        when(annonceRepository.findOneWithFilters(eq(em), anyMap(), anyString()))
                .thenReturn(Optional.of(annonce));
        when(categoryRepository.findById(em, 20L)).thenReturn(Optional.of(newCat));
        when(annonceRepository.update(em, annonce)).thenReturn(annonce);

        Annonce updated = annonceService.update(1L, 1L,
                "Nouveau", "Nouvelle desc", "Lyon", "new@t.com", 20L);

        assertEquals("Nouveau", updated.getTitle());
        assertEquals("Nouvelle desc", updated.getDescription());
        assertEquals("Lyon", updated.getAdress());
        assertEquals("new@t.com", updated.getMail());
        assertEquals(newCat, updated.getCategory());
    }

    @Test
    @DisplayName("delete() supprime l'annonce archivée")
    void delete_shouldRemoveAnnonce() {
        Annonce annonce = buildAnnonce(1L, AnnonceStatus.ARCHIVED);

        when(annonceRepository.findOneWithFilters(eq(em), anyMap(), anyString()))
                .thenReturn(Optional.of(annonce));

        annonceService.delete(1L, 1L);

        verify(annonceRepository).delete(em, annonce);
        verify(tx).commit();
    }

    @Test
    @DisplayName("delete() échoue si l'annonce n'est pas archivée")
    void delete_shouldFail_whenNotArchived() {
        Annonce annonce = buildAnnonce(1L, AnnonceStatus.DRAFT);

        when(annonceRepository.findOneWithFilters(eq(em), anyMap(), anyString()))
                .thenReturn(Optional.of(annonce));

        assertThrows(IllegalStateException.class,
                () -> annonceService.delete(1L, 1L));
    }
}
