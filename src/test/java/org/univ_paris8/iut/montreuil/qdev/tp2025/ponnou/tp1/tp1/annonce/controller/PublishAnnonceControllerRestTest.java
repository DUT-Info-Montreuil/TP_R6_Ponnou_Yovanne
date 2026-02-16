package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PublishAnnonceControllerRestTest extends AnnonceControllerRestTestBase {

    @Test
    @DisplayName("PUT /annonces/{id}/publish -> 200 passe en PUBLISHED")
    void publish_shouldReturn200() {
        Number id = createAnnonce();

        Response response = jerseyTest.target("/annonces/" + id + "/publish")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .put(Entity.json(""));

        assertEquals(200, response.getStatus());
        Map<?, ?> json = response.readEntity(Map.class);
        assertEquals("PUBLISHED", json.get("status"));
    }

    @Test
    @DisplayName("PUT /annonces/{id}/publish -> 401 sans token")
    void publish_shouldReturn401_withoutToken() {
        Response response = jerseyTest.target("/annonces/1/publish")
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.json(""));

        assertEquals(401, response.getStatus());
    }

    @Test
    @DisplayName("PUT /annonces/{id}/publish -> 403 si non-auteur")
    void publish_shouldReturn403_whenNotOwner() {
        Number id = createAnnonce();

        Response response = jerseyTest.target("/annonces/" + id + "/publish")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + otherToken)
                .put(Entity.json(""));

        assertEquals(403, response.getStatus());
    }

    @Test
    @DisplayName("PUT /annonces/{id}/publish -> 409 si déjà archivée")
    void publish_shouldReturn409_whenArchived() {
        Number id = createAnnonce();
        publishAnnonce(id);
        archiveAnnonce(id);

        Response response = jerseyTest.target("/annonces/" + id + "/publish")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .put(Entity.json(""));

        assertEquals(409, response.getStatus());
        Map<?, ?> error = response.readEntity(Map.class);
        assertEquals("CONFLICT", error.get("error"));
    }
}
