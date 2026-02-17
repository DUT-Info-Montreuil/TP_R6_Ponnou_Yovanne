package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.model.TokenStore;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DeleteUserControllerRestTest extends UserControllerRestTestBase {

    @Test
    @DisplayName("DELETE /users/{id} -> 401 sans token")
    void delete_shouldReturn401_withoutToken() {
        Response response = jerseyTest.target("/users/1")
                .request(MediaType.APPLICATION_JSON)
                .delete();

        assertEquals(401, response.getStatus());
    }

    @Test
    @DisplayName("DELETE /users/{id} -> 204 avec token valide")
    void delete_shouldReturn204_withValidToken() {
        Response createResp = jerseyTest.target("/users").request(MediaType.APPLICATION_JSON)
                .post(Entity.json(Map.of("username", "toDelete", "email", "del@test.com", "password", "password123")));
        Map<?, ?> created = createResp.readEntity(Map.class);
        Number id = (Number) created.get("id");

        String token = TokenStore.generateToken(id.longValue(), "toDelete");

        Response response = jerseyTest.target("/users/" + id)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .delete();

        assertEquals(204, response.getStatus());

        Response getResp = jerseyTest.target("/users/" + id)
                .request(MediaType.APPLICATION_JSON)
                .get();
        assertEquals(404, getResp.getStatus());
    }
}
