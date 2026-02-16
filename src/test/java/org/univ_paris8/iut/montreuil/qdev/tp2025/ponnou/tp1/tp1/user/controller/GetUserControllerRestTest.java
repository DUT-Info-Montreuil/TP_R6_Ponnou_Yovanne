package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GetUserControllerRestTest extends UserControllerRestTestBase {

    @Test
    @DisplayName("GET /users -> 200 avec pagination")
    void getAll_shouldReturn200WithPagination() {
        jerseyTest.target("/users").request(MediaType.APPLICATION_JSON)
                .post(Entity.json(Map.of("username", "user1", "email", "u1@test.com", "password", "password123")));

        Response response = jerseyTest.target("/users")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .request(MediaType.APPLICATION_JSON)
                .get();

        assertEquals(200, response.getStatus());
        Map<?, ?> json = response.readEntity(Map.class);
        assertNotNull(json.get("items"));
        assertEquals(0, json.get("page"));
        assertEquals(10, json.get("size"));
        assertEquals(1, ((Number) json.get("totalItems")).intValue());
    }

    @Test
    @DisplayName("GET /users/{id} -> 200 avec payload correct")
    void getById_shouldReturn200() {
        Response createResp = jerseyTest.target("/users").request(MediaType.APPLICATION_JSON)
                .post(Entity.json(Map.of("username", "bob", "email", "bob@test.com", "password", "password123")));
        Map<?, ?> created = createResp.readEntity(Map.class);
        Number id = (Number) created.get("id");

        Response response = jerseyTest.target("/users/" + id)
                .request(MediaType.APPLICATION_JSON)
                .get();

        assertEquals(200, response.getStatus());
        Map<?, ?> json = response.readEntity(Map.class);
        assertEquals("bob", json.get("username"));
        assertEquals("bob@test.com", json.get("email"));
    }

    @Test
    @DisplayName("GET /users/{id} -> 404 si inexistant")
    void getById_shouldReturn404_whenNotFound() {
        Response response = jerseyTest.target("/users/999")
                .request(MediaType.APPLICATION_JSON)
                .get();

        assertEquals(404, response.getStatus());
        Map<?, ?> error = response.readEntity(Map.class);
        assertEquals("NOT_FOUND", error.get("error"));
    }
}
