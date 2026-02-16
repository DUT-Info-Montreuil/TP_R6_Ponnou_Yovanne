package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DeleteCategoryControllerRestTest extends CategoryControllerRestTestBase {

    @Test
    @DisplayName("DELETE /categories/{id} -> 204 avec token valide")
    void delete_shouldReturn204() {
        Response createResp = jerseyTest.target("/categories")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .post(Entity.json(Map.of("label", "ASupprimer")));
        Map<?, ?> created = createResp.readEntity(Map.class);
        Number id = (Number) created.get("id");

        Response response = jerseyTest.target("/categories/" + id)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .delete();

        assertEquals(204, response.getStatus());

        Response getResp = jerseyTest.target("/categories/" + id)
                .request(MediaType.APPLICATION_JSON)
                .get();
        assertEquals(404, getResp.getStatus());
    }

    @Test
    @DisplayName("DELETE /categories/{id} -> 401 sans token")
    void delete_shouldReturn401_withoutToken() {
        Response response = jerseyTest.target("/categories/1")
                .request(MediaType.APPLICATION_JSON)
                .delete();

        assertEquals(401, response.getStatus());
    }
}
