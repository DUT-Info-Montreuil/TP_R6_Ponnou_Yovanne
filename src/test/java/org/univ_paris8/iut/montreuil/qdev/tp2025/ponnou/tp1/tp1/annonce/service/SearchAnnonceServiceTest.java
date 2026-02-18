package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.service;

import org.junit.jupiter.api.*;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.model.Annonce;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.model.AnnonceStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SearchAnnonceServiceTest extends AnnonceServiceTestBase {

    @Test
    @DisplayName("findPublishedPaginated() délègue au repository avec filtre PUBLISHED")
    void findPublishedPaginated_shouldReturnOnlyPublished() {
        Annonce a = buildAnnonce(1L, AnnonceStatus.PUBLISHED);

        when(annonceRepository.findWithFilters(eq(em), anyMap(), isNull(), isNull(),
                eq("date DESC"), anyString(), eq(0), eq(10)))
                .thenReturn(List.of(a));

        List<Annonce> results = annonceService.findPublishedPaginated(0, 10);

        assertEquals(1, results.size());
        assertEquals(AnnonceStatus.PUBLISHED, results.get(0).getStatus());
    }

    @Test
    @DisplayName("searchByKeywordPaginated() recherche dans titre et description")
    void searchByKeyword_shouldSearchInTitleAndDescription() {
        Annonce a = buildAnnonce(1L, AnnonceStatus.PUBLISHED);
        a.setTitle("Appartement lumineux");

        when(annonceRepository.findWithFilters(eq(em), anyMap(), eq("lumineux"),
                any(String[].class), eq("date DESC"), anyString(), eq(0), eq(10)))
                .thenReturn(List.of(a));

        List<Annonce> results = annonceService.searchByKeywordPaginated("lumineux", 0, 10);

        assertEquals(1, results.size());
    }

    @Test
    @DisplayName("findByAuthorPaginated() filtre par auteur")
    void findByAuthor_shouldReturnOnlyAuthorAnnonces() {
        Annonce a = buildAnnonce(1L, AnnonceStatus.DRAFT);

        when(annonceRepository.findWithFilters(eq(em), anyMap(), isNull(), isNull(),
                eq("date DESC"), anyString(), eq(0), eq(10)))
                .thenReturn(List.of(a));

        List<Annonce> results = annonceService.findByAuthorPaginated(1L, 0, 10);

        assertEquals(1, results.size());
    }

    @Test
    @DisplayName("countPublished() compte uniquement les annonces publiées")
    void countPublished_shouldCountCorrectly() {
        when(annonceRepository.countWithFilters(eq(em), anyMap())).thenReturn(3L);

        assertEquals(3, annonceService.countPublished());
    }
}
