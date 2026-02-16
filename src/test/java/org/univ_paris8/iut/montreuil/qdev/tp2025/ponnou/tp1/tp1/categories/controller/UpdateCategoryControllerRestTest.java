package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.categories.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UpdateCategoryControllerRestTest extends CategoryControllerRestTestBase {

    @Test
    @DisplayName("PUT /categories/{id} -> 200 avec token valide")
    void update_shouldReturn200() {
        Response createResp = jerseyTest.target("/categories")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .post(Entity.json(Map.of("label", "Ancien")));
        Map<?, ?> created = createResp.readEntity(Map.class);
        Number id = (Number) created.get("id");

        Response response = jerseyTest.target("/categories/" + id)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .put(Entity.json(Map.of("label", "Nouveau")));

        assertEquals(200, response.getStatus());
        Map<?, ?> json = response.readEntity(Map.class);
        assertEquals("Nouveau", json.get("label"));
    }

    @Test
    @DisplayName("PUT /categories/{id} -> 401 sans token")
    void update_shouldReturn401_withoutToken() {
        Response response = jerseyTest.target("/categories/1")
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.json(Map.of("label", "Nouveau")));

        assertEquals(401, response.getStatus());
    }
}
