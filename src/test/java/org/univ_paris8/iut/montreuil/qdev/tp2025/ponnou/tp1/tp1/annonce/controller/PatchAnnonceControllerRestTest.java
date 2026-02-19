package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.dto.AnnonceDTO;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.model.Annonce;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.security.AuthenticatedUser;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PatchAnnonceControllerRestTest extends AnnonceControllerRestTestBase {

    @Test
    @DisplayName("patch_shouldReturn200")
    void patch_shouldReturn200() throws Exception {
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(new AuthenticatedUser(1L, "user", "ROLE_USER"));

        Annonce annonce = new Annonce("Titre", "Description", "Paris", "mail@test.com");
        annonce.setId(1L);
        annonce.setTitle("Patched");

        AnnonceDTO dto = new AnnonceDTO();
        dto.setId(1L);
        dto.setTitle("Patched");

        when(annonceService.patch(any(), any(), any())).thenReturn(annonce);
        when(annonceMapper.toDTO(annonce)).thenReturn(dto);

        mockMvc.perform(patch("/api/annonces/{id}", 1)
                         .principal(auth)
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
