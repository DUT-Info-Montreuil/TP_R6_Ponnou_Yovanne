package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.model.Annonce;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.model.AnnonceStatus;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SearchAnnonceRepositoryTest extends AnnonceRepositoryTestBase {

    @Test
    @DisplayName("searchWithFilters_shouldFilterByStatus")
    void searchWithFilters_shouldFilterByStatus() {
        persistAnnonce("A1", "Desc", AnnonceStatus.DRAFT, ts("2025-01-01T10:00:00"), testUser, testCategory);
        persistAnnonce("A2", "Desc", AnnonceStatus.PUBLISHED, ts("2025-01-02T10:00:00"), testUser, testCategory);

        Specification<Annonce> spec = Specification.allOf(AnnonceSpecifications.hasStatus(AnnonceStatus.PUBLISHED));
        Page<Annonce> page = annonceRepository.findAll(spec, PageRequest.of(0, 10));

        assertEquals(1, page.getTotalElements());
        assertTrue(page.getContent().stream().allMatch(a -> a.getStatus() == AnnonceStatus.PUBLISHED));
    }

    @Test
    @DisplayName("searchWithFilters_shouldFilterByKeyword")
    void searchWithFilters_shouldFilterByKeyword() {
        persistAnnonce("Appartement lumineux", "Centre ville", AnnonceStatus.PUBLISHED, ts("2025-01-01T10:00:00"), testUser, testCategory);
        persistAnnonce("Maison", "Avec jardin", AnnonceStatus.PUBLISHED, ts("2025-01-02T10:00:00"), testUser, testCategory);

        Specification<Annonce> spec = Specification.allOf(AnnonceSpecifications.hasKeyword("lumineux"));
        Page<Annonce> page = annonceRepository.findAll(spec, PageRequest.of(0, 10));

        assertEquals(1, page.getTotalElements());
        assertEquals("Appartement lumineux", page.getContent().get(0).getTitle());
    }

    @Test
    @DisplayName("searchWithFilters_shouldFilterByDateRange")
    void searchWithFilters_shouldFilterByDateRange() {
        persistAnnonce("Old", "Desc", AnnonceStatus.PUBLISHED, ts("2024-01-01T00:00:00"), testUser, testCategory);
        persistAnnonce("InRange", "Desc", AnnonceStatus.PUBLISHED, ts("2025-06-15T12:00:00"), testUser, testCategory);
        persistAnnonce("New", "Desc", AnnonceStatus.PUBLISHED, ts("2026-01-01T00:00:00"), testUser, testCategory);

        Specification<Annonce> spec = Specification.allOf(
                AnnonceSpecifications.dateAfter(ts("2025-01-01T00:00:00")),
                AnnonceSpecifications.dateBefore(ts("2025-12-31T23:59:59"))
        );
        Page<Annonce> page = annonceRepository.findAll(spec, PageRequest.of(0, 10));

        assertEquals(1, page.getTotalElements());
        assertEquals("InRange", page.getContent().get(0).getTitle());
    }

    @Test
    @DisplayName("searchWithFilters_shouldPaginate")
    void searchWithFilters_shouldPaginate() {
        for (int i = 0; i < 12; i++) {
            persistAnnonce("Annonce " + i, "Desc", AnnonceStatus.PUBLISHED,
                    ts("2025-01-01T10:00:" + String.format("%02d", i)), testUser, testCategory);
        }

        Specification<Annonce> spec = Specification.allOf(AnnonceSpecifications.hasStatus(AnnonceStatus.PUBLISHED));
        Page<Annonce> page = annonceRepository.findAll(spec, PageRequest.of(1, 5));

        assertEquals(12, page.getTotalElements());
        assertEquals(5, page.getContent().size());
        assertEquals(3, page.getTotalPages());
    }

    @Test
    @DisplayName("searchWithFilters_shouldCombineMultipleFilters")
    void searchWithFilters_shouldCombineMultipleFilters() {
        persistAnnonce("Appartement central", "Desc", AnnonceStatus.PUBLISHED, ts("2025-06-01T10:00:00"), testUser, testCategory);
        persistAnnonce("Appartement central", "Desc", AnnonceStatus.PUBLISHED, ts("2025-06-01T10:00:00"), otherUser, testCategory);
        persistAnnonce("Appartement central", "Desc", AnnonceStatus.DRAFT, ts("2025-06-01T10:00:00"), testUser, testCategory);
        persistAnnonce("Studio", "Desc", AnnonceStatus.PUBLISHED, ts("2025-06-01T10:00:00"), testUser, testCategory);
        persistAnnonce("Appartement central", "Desc", AnnonceStatus.PUBLISHED, ts("2025-06-01T10:00:00"), testUser, otherCategory);

        Specification<Annonce> spec = Specification.allOf(
                AnnonceSpecifications.hasKeyword("appartement"),
                AnnonceSpecifications.hasStatus(AnnonceStatus.PUBLISHED),
                AnnonceSpecifications.hasAuthorId(testUser.getId()),
                AnnonceSpecifications.hasCategoryId(testCategory.getId()),
                AnnonceSpecifications.dateAfter(ts("2025-01-01T00:00:00")),
                AnnonceSpecifications.dateBefore(ts("2025-12-31T23:59:59"))
        );
        Page<Annonce> page = annonceRepository.findAll(spec, PageRequest.of(0, 10));

        assertEquals(1, page.getTotalElements());
        assertEquals(testUser.getId(), page.getContent().get(0).getAuthor().getId());
        assertEquals(testCategory.getId(), page.getContent().get(0).getCategory().getId());
    }

    private Timestamp ts(String isoLocalDateTime) {
        return Timestamp.valueOf(LocalDateTime.parse(isoLocalDateTime));
    }
}
