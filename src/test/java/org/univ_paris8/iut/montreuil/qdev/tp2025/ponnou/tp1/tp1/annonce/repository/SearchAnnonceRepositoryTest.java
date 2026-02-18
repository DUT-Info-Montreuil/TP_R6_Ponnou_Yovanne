package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.repository;

import org.junit.jupiter.api.*;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.model.Annonce;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.model.AnnonceStatus;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.model.Category;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.model.User;

import javax.persistence.EntityManager;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class SearchAnnonceRepositoryTest extends AnnonceRepositoryTestBase {

    @Test
    @DisplayName("findWithFilters() filtre par statut PUBLISHED")
    void findWithFilters_shouldFilterByStatus() {
        em.getTransaction().begin();
        em.persist(createAnnonce("Draft 1", "Desc", AnnonceStatus.DRAFT));
        em.persist(createAnnonce("Published 1", "Desc", AnnonceStatus.PUBLISHED));
        em.persist(createAnnonce("Published 2", "Desc", AnnonceStatus.PUBLISHED));
        em.getTransaction().commit();

        List<Annonce> published = annonceRepository.findWithFilters(em,
                Map.of("status", AnnonceStatus.PUBLISHED),
                null, null, "date DESC", null);

        assertEquals(2, published.size());
        assertTrue(published.stream().allMatch(a -> a.getStatus() == AnnonceStatus.PUBLISHED));
    }

    @Test
    @DisplayName("findWithFilters() recherche par mot-cle dans titre et description")
    void findWithFilters_shouldSearchByKeyword() {
        em.getTransaction().begin();
        em.persist(createAnnonce("Appartement lumineux", "Centre ville", AnnonceStatus.PUBLISHED));
        em.persist(createAnnonce("Maison de campagne", "Jardin lumineux", AnnonceStatus.PUBLISHED));
        em.persist(createAnnonce("Studio", "Petit studio", AnnonceStatus.PUBLISHED));
        em.getTransaction().commit();

        List<Annonce> results = annonceRepository.findWithFilters(em,
                Map.of("status", AnnonceStatus.PUBLISHED),
                "lumineux",
                new String[]{"title", "description"},
                "date DESC", null);

        assertEquals(2, results.size());
    }

    @Test
    @DisplayName("findWithFilters() pagine correctement les resultats")
    void findWithFilters_shouldPaginateResults() {
        em.getTransaction().begin();
        for (int i = 0; i < 15; i++) {
            em.persist(createAnnonce("Annonce " + i, "Description " + i, AnnonceStatus.PUBLISHED));
        }
        em.getTransaction().commit();

        List<Annonce> page0 = annonceRepository.findWithFilters(em,
                Map.of("status", AnnonceStatus.PUBLISHED),
                null, null, "title ASC", null, 0, 5);
        assertEquals(5, page0.size());

        List<Annonce> page1 = annonceRepository.findWithFilters(em,
                Map.of("status", AnnonceStatus.PUBLISHED),
                null, null, "title ASC", null, 1, 5);
        assertEquals(5, page1.size());

        List<Annonce> page2 = annonceRepository.findWithFilters(em,
                Map.of("status", AnnonceStatus.PUBLISHED),
                null, null, "title ASC", null, 2, 5);
        assertEquals(5, page2.size());

        assertNotEquals(page0.get(0).getId(), page1.get(0).getId());
    }

    @Test
    @DisplayName("countWithFilters() compte le bon nombre d'annonces publiees")
    void countWithFilters_shouldCountCorrectly() {
        em.getTransaction().begin();
        em.persist(createAnnonce("A1", "D1", AnnonceStatus.PUBLISHED));
        em.persist(createAnnonce("A2", "D2", AnnonceStatus.PUBLISHED));
        em.persist(createAnnonce("A3", "D3", AnnonceStatus.DRAFT));
        em.persist(createAnnonce("A4", "D4", AnnonceStatus.ARCHIVED));
        em.getTransaction().commit();

        long totalCount = annonceRepository.count(em);
        assertEquals(4, totalCount);

        long publishedCount = annonceRepository.countWithFilters(em, Map.of("status", AnnonceStatus.PUBLISHED));
        assertEquals(2, publishedCount);
    }

    @Test
    @DisplayName("findWithFilters() filtre par auteur")
    void findWithFilters_shouldFilterByAuthor() {
        User otherUser = new User("other", "other@test.com", "password123");
        em.getTransaction().begin();
        em.persist(otherUser);
        em.getTransaction().commit();

        Annonce annonceUser1 = createAnnonce("Par testuser", "Desc", AnnonceStatus.PUBLISHED);
        Annonce annonceUser2 = createAnnonce("Par other", "Desc", AnnonceStatus.PUBLISHED);
        annonceUser2.setAuthor(otherUser);

        em.getTransaction().begin();
        em.persist(annonceUser1);
        em.persist(annonceUser2);
        em.getTransaction().commit();

        List<Annonce> byAuthor = annonceRepository.findWithFilters(em,
                Map.of("author.id", testUser.getId()),
                null, null, "date DESC",
                "LEFT JOIN FETCH e.author LEFT JOIN FETCH e.category");

        assertEquals(1, byAuthor.size());
        assertEquals("Par testuser", byAuthor.get(0).getTitle());
    }

    @Test
    @DisplayName("findWithFilters() filtre par categorie")
    void findWithFilters_shouldFilterByCategory() {
        Category otherCategory = new Category("Vehicules");
        em.getTransaction().begin();
        em.persist(otherCategory);
        em.getTransaction().commit();

        Annonce a1 = createAnnonce("Immobilier annonce", "Desc", AnnonceStatus.PUBLISHED);
        Annonce a2 = createAnnonce("Vehicule annonce", "Desc", AnnonceStatus.PUBLISHED);
        a2.setCategory(otherCategory);

        em.getTransaction().begin();
        em.persist(a1);
        em.persist(a2);
        em.getTransaction().commit();

        Map<String, Object> filters = new HashMap<>();
        filters.put("category.id", testCategory.getId());
        filters.put("status", AnnonceStatus.PUBLISHED);

        List<Annonce> byCat = annonceRepository.findWithFilters(em, filters,
                null, null, "date DESC",
                "LEFT JOIN FETCH e.author LEFT JOIN FETCH e.category");

        assertEquals(1, byCat.size());
        assertEquals("Immobilier annonce", byCat.get(0).getTitle());
    }

    @Test
    @DisplayName("findOneWithFilters() avec JOIN FETCH charge les relations")
    void findOneWithFilters_withJoinFetch_shouldLoadRelations() {
        Annonce annonce = createAnnonce("Test join", "Desc", AnnonceStatus.PUBLISHED);
        em.getTransaction().begin();
        em.persist(annonce);
        em.getTransaction().commit();

        EntityManager em2 = emf.createEntityManager();
        try {
            Optional<Annonce> found = annonceRepository.findOneWithFilters(em2,
                    Map.of("id", annonce.getId()),
                    "LEFT JOIN FETCH e.author LEFT JOIN FETCH e.category");

            assertTrue(found.isPresent());
            assertEquals("testuser", found.get().getAuthor().getUsername());
            assertEquals("Immobilier", found.get().getCategory().getLabel());
        } finally {
            em2.close();
        }
    }
}
