package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.security.AuthenticatedUser;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.exception.ResourceNotFoundException;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DeleteAnnonceControllerRestTest extends AnnonceControllerRestTestBase {

    @Test
    @DisplayName("delete_shouldReturn204")
    void delete_shouldReturn204() throws Exception {
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(new AuthenticatedUser(1L, "user", "ROLE_USER"));
        doNothing().when(annonceService).delete(1L, 1L);

        mockMvc.perform(delete("/api/annonces/{id}", 1) .principal(auth))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("delete_shouldReturn404_whenNotFound")
    void delete_shouldReturn404_whenNotFound() throws Exception {
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(new AuthenticatedUser(1L, "user", "ROLE_USER"));
        doThrow(new ResourceNotFoundException("Annonce non trouvee")).when(annonceService).delete(1L, 1L);

        mockMvc.perform(delete("/api/annonces/{id}", 1) .principal(auth))
                .andExpect(status().isNotFound());
    }
}
