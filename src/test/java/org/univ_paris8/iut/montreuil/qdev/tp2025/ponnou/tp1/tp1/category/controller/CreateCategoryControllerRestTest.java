package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreateCategoryControllerRestTest extends CategoryControllerRestTestBase {

    @Test
    @DisplayName("POST /categories -> 201 CREATED avec payload correct")
    void createCategory_shouldReturn201() {
        Response response = jerseyTest.target("/categories")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .post(Entity.json(Map.of("label", "Immobilier")));

        assertEquals(201, response.getStatus());
        assertTrue(response.getLocation().getPath().contains("/categories/"));

        Map<?, ?> json = response.readEntity(Map.class);
        assertEquals("Immobilier", json.get("label"));
        assertNotNull(json.get("id"));
    }

    @Test
    @DisplayName("POST /categories -> 401 sans token")
    void createCategory_shouldReturn401_withoutToken() {
        Response response = jerseyTest.target("/categories")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(Map.of("label", "Immobilier")));

        assertEquals(401, response.getStatus());
    }

    @Test
    @DisplayName("POST /categories -> 400 si label manquant")
    void createCategory_shouldReturn400_whenLabelMissing() {
        Response response = jerseyTest.target("/categories")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .post(Entity.json(Map.of()));

        assertEquals(400, response.getStatus());
        Map<?, ?> error = response.readEntity(Map.class);
        assertEquals("VALIDATION_ERROR", error.get("error"));
    }

    @Test
    @DisplayName("POST /categories -> 400 si label duplique")
    void createCategory_shouldReturn400_whenDuplicateLabel() {
        jerseyTest.target("/categories")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .post(Entity.json(Map.of("label", "Immobilier")));

        Response response = jerseyTest.target("/categories")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .post(Entity.json(Map.of("label", "Immobilier")));

        assertEquals(400, response.getStatus());
    }
}
