package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.mappers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.dto.AnnonceDTO;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.model.Annonce;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.model.AnnonceStatus;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.model.Category;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.model.User;

import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AnnonceMapperTest {

    @Test
    @DisplayName("toDTO() mappe tous les champs correctement")
    void toDTO_shouldMapAllFields() {
        Annonce annonce = new Annonce("Appart F3", "Bel appartement", "Paris 10", "contact@test.com");
        annonce.setId(1L);
        annonce.setDate(new Timestamp(1000000L));
        annonce.setStatus(AnnonceStatus.PUBLISHED);

        User author = new User("jean", "jean@test.com", "password123");
        author.setId(10L);
        annonce.setAuthor(author);

        Category category = new Category("Immobilier");
        category.setId(5L);
        annonce.setCategory(category);

        AnnonceDTO dto = AnnonceMapper.toDTO(annonce);

        assertEquals(1L, dto.getId());
        assertEquals("Appart F3", dto.getTitle());
        assertEquals("Bel appartement", dto.getDescription());
        assertEquals("Paris 10", dto.getAdress());
        assertEquals("contact@test.com", dto.getMail());
        assertEquals(new Timestamp(1000000L), dto.getDate());
        assertEquals("PUBLISHED", dto.getStatus());
        assertEquals("jean", dto.getAuthorUsername());
        assertEquals("Immobilier", dto.getCategoryLabel());
    }

    @Test
    @DisplayName("toDTO() gère author null")
    void toDTO_shouldHandleNullAuthor() {
        Annonce annonce = new Annonce("Titre", "Desc", "Addr", "m@t.com");
        annonce.setId(1L);
        annonce.setStatus(AnnonceStatus.DRAFT);
        annonce.setCategory(new Category("Cat"));

        AnnonceDTO dto = AnnonceMapper.toDTO(annonce);

        assertNull(dto.getAuthorUsername());
    }

    @Test
    @DisplayName("toDTO() gère category null")
    void toDTO_shouldHandleNullCategory() {
        Annonce annonce = new Annonce("Titre", "Desc", "Addr", "m@t.com");
        annonce.setId(1L);
        annonce.setStatus(AnnonceStatus.DRAFT);
        annonce.setAuthor(new User("jean", "j@t.com", "pass123"));

        AnnonceDTO dto = AnnonceMapper.toDTO(annonce);

        assertNull(dto.getCategoryLabel());
    }

    @Test
    @DisplayName("toDTO() gère status null")
    void toDTO_shouldHandleNullStatus() {
        Annonce annonce = new Annonce("Titre", "Desc", "Addr", "m@t.com");
        annonce.setId(1L);
        annonce.setStatus(null);

        AnnonceDTO dto = AnnonceMapper.toDTO(annonce);

        assertNull(dto.getStatus());
    }

    @Test
    @DisplayName("toDTOList() mappe une liste d'annonces")
    void toDTOList_shouldMapAllElements() {
        Annonce a1 = new Annonce("T1", "D1", "A1", "m1@t.com");
        a1.setId(1L);
        a1.setStatus(AnnonceStatus.DRAFT);

        Annonce a2 = new Annonce("T2", "D2", "A2", "m2@t.com");
        a2.setId(2L);
        a2.setStatus(AnnonceStatus.PUBLISHED);

        List<AnnonceDTO> dtos = AnnonceMapper.toDTOList(List.of(a1, a2));

        assertEquals(2, dtos.size());
        assertEquals("T1", dtos.get(0).getTitle());
        assertEquals("T2", dtos.get(1).getTitle());
    }

    @Test
    @DisplayName("toDTOList() retourne une liste vide pour une entrée vide")
    void toDTOList_shouldReturnEmptyList() {
        List<AnnonceDTO> dtos = AnnonceMapper.toDTOList(List.of());
        assertTrue(dtos.isEmpty());
    }
}
