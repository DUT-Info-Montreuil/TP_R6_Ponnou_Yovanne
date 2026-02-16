package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ArchiveAnnonceControllerRestTest extends AnnonceControllerRestTestBase {

    @Test
    @DisplayName("PUT /annonces/{id}/archive -> 200 passe en ARCHIVED")
    void archive_shouldReturn200() {
        Number id = createAnnonce();
        publishAnnonce(id);

        Response response = jerseyTest.target("/annonces/" + id + "/archive")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .put(Entity.json(""));

        assertEquals(200, response.getStatus());
        Map<?, ?> json = response.readEntity(Map.class);
        assertEquals("ARCHIVED", json.get("status"));
    }

    @Test
    @DisplayName("PUT /annonces/{id}/archive -> 403 si non-auteur")
    void archive_shouldReturn403_whenNotOwner() {
        Number id = createAnnonce();
        publishAnnonce(id);

        Response response = jerseyTest.target("/annonces/" + id + "/archive")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + otherToken)
                .put(Entity.json(""));

        assertEquals(403, response.getStatus());
    }
}
