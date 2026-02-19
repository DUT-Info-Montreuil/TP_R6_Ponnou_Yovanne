package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.dto.AnnonceDTO;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.model.Annonce;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PatchAnnonceControllerRestTest extends AnnonceControllerRestTestBase {

    @Test
    @DisplayName("patch_shouldReturn200")
    void patch_shouldReturn200() throws Exception {
        Annonce annonce = new Annonce("Titre", "Description", "Paris", "mail@test.com");
        annonce.setId(1L);
        annonce.setTitle("Patched");

        AnnonceDTO dto = new AnnonceDTO();
        dto.setId(1L);
        dto.setTitle("Patched");

        when(annonceService.patch(any(), any(), any())).thenReturn(annonce);
        when(annonceMapper.toDTO(annonce)).thenReturn(dto);

        mockMvc.perform(patch("/api/annonces/{id}", 1)
                         .principal(authenticatedPrincipal(1L, "user", "ROLE_USER"))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "title":"Patched"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Patched"));
    }
}
