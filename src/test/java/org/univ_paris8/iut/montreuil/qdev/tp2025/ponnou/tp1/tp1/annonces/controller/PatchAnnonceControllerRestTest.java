package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonces.controller;

import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PatchAnnonceControllerRestTest extends AnnonceControllerRestTestBase {

    @Test
    @DisplayName("PATCH /annonces/{id} -> 200 modifie partiellement")
    void patch_shouldReturn200() {
        Number id = createAnnonce();

        Map<String, Object> patchBody = new HashMap<>();
        patchBody.put("title", "Titre partiel");

        Response response = jerseyTest.target("/annonces/" + id)
                .property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .method("PATCH", Entity.json(patchBody));

        assertEquals(200, response.getStatus());
        Map<?, ?> json = response.readEntity(Map.class);
        assertEquals("Titre partiel", json.get("title"));
        assertEquals("Bel appartement lumineux", json.get("description"));
    }

    @Test
    @DisplayName("PATCH /annonces/{id} -> 403 si non-auteur")
    void patch_shouldReturn403_whenNotOwner() {
        Number id = createAnnonce();

        Response response = jerseyTest.target("/annonces/" + id)
                .property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + otherToken)
                .method("PATCH", Entity.json(Map.of("title", "Titre partiel")));

        assertEquals(403, response.getStatus());
    }

    @Test
    @DisplayName("PATCH /annonces/{id} -> 409 si annonce PUBLISHED")
    void patch_shouldReturn409_whenPublished() {
        Number id = createAnnonce();
        publishAnnonce(id);

        Response response = jerseyTest.target("/annonces/" + id)
                .property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .method("PATCH", Entity.json(Map.of("title", "Titre modifie")));

        assertEquals(409, response.getStatus());
    }
}
