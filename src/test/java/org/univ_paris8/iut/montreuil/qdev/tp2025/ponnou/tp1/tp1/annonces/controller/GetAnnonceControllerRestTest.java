package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonces.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GetAnnonceControllerRestTest extends AnnonceControllerRestTestBase {

    @Test
    @DisplayName("GET /annonces -> 200 avec pagination")
    void getAll_shouldReturn200WithPagination() {
        createAnnonce();
        createAnnonce();

        Response response = jerseyTest.target("/annonces")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .request(MediaType.APPLICATION_JSON)
                .get();

        assertEquals(200, response.getStatus());
        Map<?, ?> json = response.readEntity(Map.class);
        List<?> items = (List<?>) json.get("items");
        assertEquals(2, items.size());
        assertEquals(0, json.get("page"));
        assertEquals(10, json.get("size"));
        assertEquals(2, ((Number) json.get("totalItems")).intValue());
        assertEquals(1, ((Number) json.get("totalPages")).intValue());
    }

    @Test
    @DisplayName("GET /annonces -> pagination taille 1 retourne 1 item")
    void getAll_shouldRespectPageSize() {
        createAnnonce();
        createAnnonce();
        createAnnonce();

        Response response = jerseyTest.target("/annonces")
                .queryParam("page", 0)
                .queryParam("size", 1)
                .request(MediaType.APPLICATION_JSON)
                .get();

        assertEquals(200, response.getStatus());
        Map<?, ?> json = response.readEntity(Map.class);
        List<?> items = (List<?>) json.get("items");
        assertEquals(1, items.size());
        assertEquals(3, ((Number) json.get("totalItems")).intValue());
        assertEquals(3, ((Number) json.get("totalPages")).intValue());
    }

    @Test
    @DisplayName("GET /annonces/{id} -> 200 avec payload complet")
    void getById_shouldReturn200() {
        Number id = createAnnonce();

        Response response = jerseyTest.target("/annonces/" + id)
                .request(MediaType.APPLICATION_JSON)
                .get();

        assertEquals(200, response.getStatus());
        Map<?, ?> json = response.readEntity(Map.class);
        assertEquals("Appart F3", json.get("title"));
        assertEquals("DRAFT", json.get("status"));
        assertEquals("author", json.get("authorUsername"));
        assertEquals("Immobilier", json.get("categoryLabel"));
    }

    @Test
    @DisplayName("GET /annonces/{id} -> 404 si inexistant")
    void getById_shouldReturn404_whenNotFound() {
        Response response = jerseyTest.target("/annonces/999")
                .request(MediaType.APPLICATION_JSON)
                .get();

        assertEquals(404, response.getStatus());
        Map<?, ?> error = response.readEntity(Map.class);
        assertEquals("NOT_FOUND", error.get("error"));
    }
}
