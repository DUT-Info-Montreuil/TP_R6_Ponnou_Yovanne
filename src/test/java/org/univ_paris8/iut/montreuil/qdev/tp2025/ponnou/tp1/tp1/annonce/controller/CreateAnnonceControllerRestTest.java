package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.dto.AnnonceDTO;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.model.Annonce;

import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CreateAnnonceControllerRestTest extends AnnonceControllerRestTestBase {

    @Test
    @DisplayName("create_shouldReturn201")
    void create_shouldReturn201() throws Exception {
        Annonce created = new Annonce("Titre", "Description", "Paris", "mail@test.com");
        created.setId(1L);
        AnnonceDTO dto = new AnnonceDTO();
        dto.setId(1L);
        dto.setTitle("Titre");

        when(annonceService.create("Titre", "Description", "Paris", "mail@test.com", 1L, 10L)).thenReturn(created);
        when(annonceMapper.toDTO(created)).thenReturn(dto);

        mockMvc.perform(post("/api/annonces")
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
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/api/annonces/1")))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("create_shouldReturn400_whenValidationFails")
    void create_shouldReturn400_whenValidationFails() throws Exception {
        mockMvc.perform(post("/api/annonces")
                         .principal(authenticatedPrincipal(1L, "user", "ROLE_USER"))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "description":"Description",
                                  "adress":"Paris",
                                  "mail":"mail@test.com",
                                  "categoryId":10
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }
}
