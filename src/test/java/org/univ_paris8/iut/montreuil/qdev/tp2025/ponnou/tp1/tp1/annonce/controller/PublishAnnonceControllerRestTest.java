package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.dto.AnnonceDTO;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.model.Annonce;

import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PublishAnnonceControllerRestTest extends AnnonceControllerRestTestBase {

    @Test
    @DisplayName("publish_shouldReturn200")
    void publish_shouldReturn200() throws Exception {
        Annonce annonce = new Annonce("Titre", "Description", "Paris", "mail@test.com");
        annonce.setId(1L);
        AnnonceDTO dto = new AnnonceDTO();
        dto.setId(1L);

        when(annonceService.publish(1L, 1L)).thenReturn(annonce);
        when(annonceMapper.toDTO(annonce)).thenReturn(dto);

        mockMvc.perform(put("/api/annonces/{id}/publish", 1).principal(authenticatedPrincipal(1L, "user", "ROLE_USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }
}
