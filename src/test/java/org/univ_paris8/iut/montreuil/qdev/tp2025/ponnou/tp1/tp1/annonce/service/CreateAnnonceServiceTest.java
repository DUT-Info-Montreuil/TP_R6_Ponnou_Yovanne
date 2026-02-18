package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.service;

import org.junit.jupiter.api.*;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.model.Annonce;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.model.AnnonceStatus;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.config.EntityManagerUtil;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.exception.ResourceNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CreateAnnonceServiceTest extends AnnonceServiceTestBase {

    @Test
    @DisplayName("create() crée une annonce en DRAFT")
    void create_shouldCreateDraftAnnonce() {
        when(userRepository.findById(em, 1L)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(em, 10L)).thenReturn(Optional.of(testCategory));
        when(annonceRepository.save(eq(em), any(Annonce.class))).thenAnswer(inv -> {
            Annonce a = inv.getArgument(1);
            a.setId(100L);
            return a;
        });

        Annonce annonce = annonceService.create("Appart F3", "Bel appartement",
                "Paris 10", "contact@test.com", 1L, 10L);

        assertEquals("Appart F3", annonce.getTitle());
        assertEquals(AnnonceStatus.DRAFT, annonce.getStatus());
        assertEquals(testUser, annonce.getAuthor());
        assertEquals(testCategory, annonce.getCategory());
        verify(tx).begin();
        verify(tx).commit();
    }

    @Test
    @DisplayName("create() échoue si l'auteur n'existe pas")
    void create_shouldFail_whenAuthorNotFound() {
        when(userRepository.findById(em, 999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> annonceService.create("Titre", "Desc", "Addr", "m@t.com", 999L, 10L));
        verify(tx).rollback();
    }

    @Test
    @DisplayName("create() échoue si la catégorie n'existe pas")
    void create_shouldFail_whenCategoryNotFound() {
        when(userRepository.findById(em, 1L)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(em, 999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> annonceService.create("Titre", "Desc", "Addr", "m@t.com", 1L, 999L));
        verify(tx).rollback();
    }

    @Test
    @DisplayName("Chaque méthode du service utilise EntityManagerUtil")
    void service_shouldUseEntityManagerUtil() {
        when(userRepository.findById(em, 1L)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(em, 10L)).thenReturn(Optional.of(testCategory));
        when(annonceRepository.save(eq(em), any(Annonce.class))).thenAnswer(inv -> inv.getArgument(1));

        annonceService.create("Test", "Desc", "Addr", "m@t.com", 1L, 10L);

        mockedUtil.verify(EntityManagerUtil::getEntityManager, atLeastOnce());
    }
}
