package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.model.Annonce;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.model.AnnonceStatus;

import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SearchAnnonceServiceTest extends AnnonceServiceTestBase {

    @Test
    @DisplayName("searchWithFilters_shouldDelegateToRepository")
    void searchWithFilters_shouldDelegateToRepository() {
        Pageable pageable = PageRequest.of(0, 10);
        Timestamp fromDate = Timestamp.valueOf("2025-01-01 00:00:00");
        Timestamp toDate = Timestamp.valueOf("2025-12-31 23:59:59");
        Page<Annonce> expected = new PageImpl<>(List.of(new Annonce()), pageable, 1);

        when(annonceRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), eq(pageable)))
                .thenReturn(expected);

        Page<Annonce> actual = annonceService.searchWithFilters(
                "appart", 1L, 2L, AnnonceStatus.PUBLISHED, fromDate, toDate, pageable);

        assertSame(expected, actual);
        verify(annonceRepository).findAll(any(org.springframework.data.jpa.domain.Specification.class), eq(pageable));
    }
}
