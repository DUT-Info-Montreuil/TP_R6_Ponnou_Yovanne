package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.categories.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GetCategoryControllerRestTest extends CategoryControllerRestTestBase {

    @Test
    @DisplayName("GET /categories -> 200 avec pagination")
    void getAll_shouldReturn200WithPagination() {
        jerseyTest.target("/categories")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .post(Entity.json(Map.of("label", "Cat1")));
        jerseyTest.target("/categories")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .post(Entity.json(Map.of("label", "Cat2")));

        Response response = jerseyTest.target("/categories")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .request(MediaType.APPLICATION_JSON)
                .get();

        assertEquals(200, response.getStatus());
        Map<?, ?> json = response.readEntity(Map.class);
        List<?> items = (List<?>) json.get("items");
        assertEquals(2, items.size());
        assertEquals(2, ((Number) json.get("totalItems")).intValue());
    }

    @Test
    @DisplayName("GET /categories/{id} -> 200 avec payload correct")
    void getById_shouldReturn200() {
        Response createResp = jerseyTest.target("/categories")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .post(Entity.json(Map.of("label", "Services")));
        Map<?, ?> created = createResp.readEntity(Map.class);
        Number id = (Number) created.get("id");

        Response response = jerseyTest.target("/categories/" + id)
                .request(MediaType.APPLICATION_JSON)
                .get();

        assertEquals(200, response.getStatus());
        Map<?, ?> json = response.readEntity(Map.class);
        assertEquals("Services", json.get("label"));
    }

    @Test
    @DisplayName("GET /categories/{id} -> 404 si inexistant")
    void getById_shouldReturn404_whenNotFound() {
        Response response = jerseyTest.target("/categories/999")
                .request(MediaType.APPLICATION_JSON)
                .get();

        assertEquals(404, response.getStatus());
        Map<?, ?> error = response.readEntity(Map.class);
        assertEquals("NOT_FOUND", error.get("error"));
    }
}
