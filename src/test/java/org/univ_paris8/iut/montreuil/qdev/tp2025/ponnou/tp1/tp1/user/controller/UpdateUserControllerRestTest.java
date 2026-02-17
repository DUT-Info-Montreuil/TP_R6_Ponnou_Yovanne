package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.model.TokenStore;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UpdateUserControllerRestTest extends UserControllerRestTestBase {

    @Test
    @DisplayName("PUT /users/{id} -> 401 sans token")
    void update_shouldReturn401_withoutToken() {
        Response response = jerseyTest.target("/users/1")
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.json(Map.of("username", "updated", "email", "up@test.com")));

        assertEquals(401, response.getStatus());
    }

    @Test
    @DisplayName("PUT /users/{id} -> 200 avec token valide")
    void update_shouldReturn200_withValidToken() {
        Response createResp = jerseyTest.target("/users").request(MediaType.APPLICATION_JSON)
                .post(Entity.json(Map.of("username", "charlie", "email", "ch@test.com", "password", "password123")));
        Map<?, ?> created = createResp.readEntity(Map.class);
        Number id = (Number) created.get("id");

        String token = TokenStore.generateToken(id.longValue(), "charlie");

        Response response = jerseyTest.target("/users/" + id)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .put(Entity.json(Map.of("username", "charlie_updated", "email", "ch_new@test.com")));

        assertEquals(200, response.getStatus());
        Map<?, ?> json = response.readEntity(Map.class);
        assertEquals("charlie_updated", json.get("username"));
    }
}
