package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.dto.AnnonceDTO;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.model.Annonce;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.exception.ResourceNotFoundException;

import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UpdateAnnonceControllerRestTest extends AnnonceControllerRestTestBase {

    @Test
    @DisplayName("update_shouldReturn200")
    void update_shouldReturn200() throws Exception {
        Annonce annonce = new Annonce("Titre", "Description", "Paris", "mail@test.com");
        annonce.setId(1L);
        AnnonceDTO dto = new AnnonceDTO();
        dto.setId(1L);
        dto.setTitle("Titre");

        when(annonceService.update(1L, 1L, "Titre", "Description", "Paris", "mail@test.com", 10L)).thenReturn(annonce);
        when(annonceMapper.toDTO(annonce)).thenReturn(dto);

        mockMvc.perform(put("/api/annonces/{id}", 1)
                         .principal(authenticatedPrincipal(1L, "user", "ROLE_USER"))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "title":"Titre",
                                  "description":"Description",
                                  "adress":"Paris",
                                  "mail":"mail@test.com",
                                  "categoryId":10
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("update_shouldReturn404_whenNotFound")
    void update_shouldReturn404_whenNotFound() throws Exception {
        when(annonceService.update(1L, 1L, "Titre", "Description", "Paris", "mail@test.com", 10L))
                .thenThrow(new ResourceNotFoundException("Annonce non trouvee"));

        mockMvc.perform(put("/api/annonces/{id}", 1)
                         .principal(authenticatedPrincipal(1L, "user", "ROLE_USER"))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "title":"Titre",
                                  "description":"Description",
                                  "adress":"Paris",
                                  "mail":"mail@test.com",
                                  "categoryId":10
                                }
                                """))
                .andExpect(status().isNotFound());
    }
}
