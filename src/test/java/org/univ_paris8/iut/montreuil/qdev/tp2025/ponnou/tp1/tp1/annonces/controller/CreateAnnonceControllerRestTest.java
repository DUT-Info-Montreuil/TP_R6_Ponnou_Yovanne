package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonces.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreateAnnonceControllerRestTest extends AnnonceControllerRestTestBase {

    @Test
    @DisplayName("POST /annonces -> 201 CREATED avec payload correct")
    void createAnnonce_shouldReturn201() {
        Response response = jerseyTest.target("/annonces")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .post(Entity.json(annonceBody()));

        assertEquals(201, response.getStatus());
        assertTrue(response.getLocation().getPath().contains("/annonces/"));

        Map<?, ?> json = response.readEntity(Map.class);
        assertEquals("Appart F3", json.get("title"));
        assertEquals("Bel appartement lumineux", json.get("description"));
        assertEquals("Paris 10", json.get("adress"));
        assertEquals("contact@test.com", json.get("mail"));
        assertEquals("DRAFT", json.get("status"));
        assertEquals("author", json.get("authorUsername"));
        assertEquals("Immobilier", json.get("categoryLabel"));
        assertNotNull(json.get("id"));
        assertNotNull(json.get("date"));
    }

    @Test
    @DisplayName("POST /annonces -> 401 sans token")
    void createAnnonce_shouldReturn401_withoutToken() {
        Response response = jerseyTest.target("/annonces")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(annonceBody()));

        assertEquals(401, response.getStatus());
        Map<?, ?> error = response.readEntity(Map.class);
        assertEquals("UNAUTHORIZED", error.get("error"));
    }

    @Test
    @DisplayName("POST /annonces -> 400 si titre manquant")
    void createAnnonce_shouldReturn400_whenTitleMissing() {
        Map<String, Object> body = annonceBody();
        body.remove("title");

        Response response = jerseyTest.target("/annonces")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .post(Entity.json(body));

        assertEquals(400, response.getStatus());
        Map<?, ?> error = response.readEntity(Map.class);
        assertEquals("VALIDATION_ERROR", error.get("error"));
    }

    @Test
    @DisplayName("POST /annonces -> 400 si mail invalide")
    void createAnnonce_shouldReturn400_whenMailInvalid() {
        Map<String, Object> body = annonceBody();
        body.put("mail", "not-an-email");

        Response response = jerseyTest.target("/annonces")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .post(Entity.json(body));

        assertEquals(400, response.getStatus());
    }

    @Test
    @DisplayName("POST /annonces -> 400 si categoryId manquant")
    void createAnnonce_shouldReturn400_whenCategoryMissing() {
        Map<String, Object> body = annonceBody();
        body.remove("categoryId");

        Response response = jerseyTest.target("/annonces")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .post(Entity.json(body));

        assertEquals(400, response.getStatus());
    }
}
