package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonces.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UpdateAnnonceControllerRestTest extends AnnonceControllerRestTestBase {

    @Test
    @DisplayName("PUT /annonces/{id} -> 200 modifie l'annonce")
    void update_shouldReturn200() {
        Number id = createAnnonce();

        Map<String, Object> body = new HashMap<>();
        body.put("title", "Nouveau titre");
        body.put("description", "Nouvelle description");
        body.put("adress", "Lyon 1");
        body.put("mail", "new@test.com");
        body.put("categoryId", testCategory.getId());

        Response response = jerseyTest.target("/annonces/" + id)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .put(Entity.json(body));

        assertEquals(200, response.getStatus());
        Map<?, ?> json = response.readEntity(Map.class);
        assertEquals("Nouveau titre", json.get("title"));
        assertEquals("Lyon 1", json.get("adress"));
    }

    @Test
    @DisplayName("PUT /annonces/{id} -> 401 sans token")
    void update_shouldReturn401_withoutToken() {
        Response response = jerseyTest.target("/annonces/1")
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.json(annonceBody()));

        assertEquals(401, response.getStatus());
    }

    @Test
    @DisplayName("PUT /annonces/{id} -> 403 si non-auteur")
    void update_shouldReturn403_whenNotOwner() {
        Number id = createAnnonce();

        Map<String, Object> body = new HashMap<>();
        body.put("title", "Nouveau titre");
        body.put("description", "Nouvelle description");
        body.put("adress", "Lyon 1");
        body.put("mail", "new@test.com");
        body.put("categoryId", testCategory.getId());

        Response response = jerseyTest.target("/annonces/" + id)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + otherToken)
                .put(Entity.json(body));

        assertEquals(403, response.getStatus());
        Map<?, ?> error = response.readEntity(Map.class);
        assertEquals("FORBIDDEN", error.get("error"));
    }

    @Test
    @DisplayName("PUT /annonces/{id} -> 409 si annonce PUBLISHED")
    void update_shouldReturn409_whenPublished() {
        Number id = createAnnonce();
        publishAnnonce(id);

        Map<String, Object> body = new HashMap<>();
        body.put("title", "Nouveau titre");
        body.put("description", "Nouvelle description");
        body.put("adress", "Lyon 1");
        body.put("mail", "new@test.com");
        body.put("categoryId", testCategory.getId());

        Response response = jerseyTest.target("/annonces/" + id)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .put(Entity.json(body));

        assertEquals(409, response.getStatus());
        Map<?, ?> error = response.readEntity(Map.class);
        assertEquals("CONFLICT", error.get("error"));
    }
}
