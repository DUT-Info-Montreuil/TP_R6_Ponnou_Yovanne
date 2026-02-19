package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.repository;

import org.hibernate.Hibernate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.model.Annonce;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.model.AnnonceStatus;

import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CrudAnnonceRepositoryTest extends AnnonceRepositoryTestBase {

    @Test
    @DisplayName("save_shouldPersistAnnonce")
    void save_shouldPersistAnnonce() {
        Annonce annonce = new Annonce("Titre", "Description", "Paris", "mail@test.com");
        annonce.setStatus(AnnonceStatus.DRAFT);
        annonce.setAuthor(testUser);
        annonce.setCategory(testCategory);

        Annonce saved = annonceRepository.save(annonce);

        assertTrue(saved.getId() != null);
        assertEquals("Titre", saved.getTitle());
    }

    @Test
    @DisplayName("findByIdWithRelations_shouldLoadRelations")
    void findByIdWithRelations_shouldLoadRelations() {
        Annonce saved = persistAnnonce(
                "Titre",
                "Description",
                AnnonceStatus.PUBLISHED,
                Timestamp.valueOf("2024-01-01 10:00:00"),
                testUser,
                testCategory
        );

        Annonce found = annonceRepository.findByIdWithRelations(saved.getId()).orElseThrow();

        assertTrue(Hibernate.isInitialized(found.getAuthor()));
        assertTrue(Hibernate.isInitialized(found.getCategory()));
    }

    @Test
    @DisplayName("delete_shouldRemoveAnnonce")
    void delete_shouldRemoveAnnonce() {
        Annonce saved = persistAnnonce(
                "Titre",
                "Description",
                AnnonceStatus.DRAFT,
                Timestamp.valueOf("2024-01-01 10:00:00"),
                testUser,
                testCategory
        );

        annonceRepository.deleteById(saved.getId());

        assertTrue(annonceRepository.findById(saved.getId()).isEmpty());
    }
}
