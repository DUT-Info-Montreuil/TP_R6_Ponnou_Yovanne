package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.repository;

import org.junit.jupiter.api.*;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.model.Annonce;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.model.AnnonceStatus;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CrudAnnonceRepositoryTest extends AnnonceRepositoryTestBase {

    @Test
    @DisplayName("save() persiste une annonce avec ses relations")
    void save_shouldPersistAnnonce() {
        Annonce annonce = createAnnonce("Appartement F3", "Bel appart lumineux", AnnonceStatus.DRAFT);

        em.getTransaction().begin();
        Annonce saved = annonceRepository.save(em, annonce);

        assertNotNull(saved.getId());
        assertEquals("Appartement F3", saved.getTitle());
        assertEquals(AnnonceStatus.DRAFT, saved.getStatus());
        assertNotNull(saved.getDate());
        assertEquals(testUser.getId(), saved.getAuthor().getId());
        assertEquals(testCategory.getId(), saved.getCategory().getId());
    }

    @Test
    @DisplayName("findById() retourne l'annonce existante")
    void findById_shouldReturnAnnonce() {
        Annonce annonce = createAnnonce("Maison", "Grande maison", AnnonceStatus.DRAFT);
        em.getTransaction().begin();
        annonceRepository.save(em, annonce);

        Optional<Annonce> found = annonceRepository.findById(em, annonce.getId());

        assertTrue(found.isPresent());
        assertEquals("Maison", found.get().getTitle());
    }

    @Test
    @DisplayName("update() met a jour le titre et le statut")
    void update_shouldModifyAnnonce() {
        Annonce annonce = createAnnonce("Ancien titre", "Description", AnnonceStatus.DRAFT);
        em.getTransaction().begin();
        annonceRepository.save(em, annonce);
        em.getTransaction().commit();

        annonce.setTitle("Nouveau titre");
        annonce.setStatus(AnnonceStatus.PUBLISHED);
        em.getTransaction().begin();
        Annonce updated = annonceRepository.update(em, annonce);

        assertEquals("Nouveau titre", updated.getTitle());
        assertEquals(AnnonceStatus.PUBLISHED, updated.getStatus());
    }

    @Test
    @DisplayName("deleteById() supprime l'annonce")
    void deleteById_shouldRemoveAnnonce() {
        Annonce annonce = createAnnonce("A supprimer", "Desc", AnnonceStatus.DRAFT);
        em.getTransaction().begin();
        annonceRepository.save(em, annonce);
        Long id = annonce.getId();
        em.getTransaction().commit();

        em.getTransaction().begin();
        annonceRepository.deleteById(em, id);

        assertTrue(annonceRepository.findById(em, id).isEmpty());
    }
}
