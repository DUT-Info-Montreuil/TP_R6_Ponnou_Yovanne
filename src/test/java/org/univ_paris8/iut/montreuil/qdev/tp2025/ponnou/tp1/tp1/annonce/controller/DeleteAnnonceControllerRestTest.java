package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DeleteAnnonceControllerRestTest extends AnnonceControllerRestTestBase {

    @Test
    @DisplayName("DELETE /annonces/{id} -> 204 si annonce archivée")
    void delete_shouldReturn204_whenArchived() {
        Number id = createAnnonce();
        publishAnnonce(id);
        archiveAnnonce(id);

        Response response = jerseyTest.target("/annonces/" + id)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .delete();

        assertEquals(204, response.getStatus());

        Response getResp = jerseyTest.target("/annonces/" + id)
                .request(MediaType.APPLICATION_JSON)
                .get();
        assertEquals(404, getResp.getStatus());
    }

    @Test
    @DisplayName("DELETE /annonces/{id} -> 401 sans token")
    void delete_shouldReturn401_withoutToken() {
        Response response = jerseyTest.target("/annonces/1")
                .request(MediaType.APPLICATION_JSON)
                .delete();

        assertEquals(401, response.getStatus());
    }

    @Test
    @DisplayName("DELETE /annonces/{id} -> 403 si non-auteur")
    void delete_shouldReturn403_whenNotOwner() {
        Number id = createAnnonce();
        publishAnnonce(id);
        archiveAnnonce(id);

        Response response = jerseyTest.target("/annonces/" + id)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + otherToken)
                .delete();

        assertEquals(403, response.getStatus());
    }

    @Test
    @DisplayName("DELETE /annonces/{id} -> 409 si annonce non archivée (DRAFT)")
    void delete_shouldReturn409_whenNotArchived() {
        Number id = createAnnonce();

        Response response = jerseyTest.target("/annonces/" + id)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .delete();

        assertEquals(409, response.getStatus());
        Map<?, ?> error = response.readEntity(Map.class);
        assertEquals("CONFLICT", error.get("error"));
    }

    @Test
    @DisplayName("DELETE /annonces/{id} -> 409 si annonce PUBLISHED (non archivée)")
    void delete_shouldReturn409_whenPublished() {
        Number id = createAnnonce();
        publishAnnonce(id);

        Response response = jerseyTest.target("/annonces/" + id)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .delete();

        assertEquals(409, response.getStatus());
    }
}
