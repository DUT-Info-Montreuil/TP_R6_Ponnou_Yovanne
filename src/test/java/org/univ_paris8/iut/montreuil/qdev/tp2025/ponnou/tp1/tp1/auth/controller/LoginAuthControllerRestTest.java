package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class LoginAuthControllerRestTest extends AuthControllerRestTestBase {

    @Test
    @DisplayName("POST /login -> 200 avec token si identifiants valides")
    void login_shouldReturn200WithToken() {
        Response response = jerseyTest.target("/login")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(Map.of("username", "testuser", "password", "password123")));

        assertEquals(200, response.getStatus());
        Map<?, ?> json = response.readEntity(Map.class);
        assertNotNull(json.get("token"));
        assertEquals("testuser", json.get("username"));
    }

    @Test
    @DisplayName("POST /login -> 401 si mot de passe incorrect")
    void login_shouldReturn401_whenWrongPassword() {
        Response response = jerseyTest.target("/login")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(Map.of("username", "testuser", "password", "wrongpassword")));

        assertEquals(401, response.getStatus());
        Map<?, ?> error = response.readEntity(Map.class);
        assertEquals("UNAUTHORIZED", error.get("error"));
    }

    @Test
    @DisplayName("POST /login -> 401 si utilisateur inexistant")
    void login_shouldReturn401_whenUserNotFound() {
        Response response = jerseyTest.target("/login")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(Map.of("username", "unknown", "password", "password123")));

        assertEquals(401, response.getStatus());
    }

    @Test
    @DisplayName("POST /login -> 400 si username manquant")
    void login_shouldReturn400_whenUsernameMissing() {
        Response response = jerseyTest.target("/login")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(Map.of("password", "password123")));

        assertEquals(400, response.getStatus());
        Map<?, ?> error = response.readEntity(Map.class);
        assertEquals("VALIDATION_ERROR", error.get("error"));
    }

    @Test
    @DisplayName("POST /login -> 400 si password manquant")
    void login_shouldReturn400_whenPasswordMissing() {
        Response response = jerseyTest.target("/login")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(Map.of("username", "testuser")));

        assertEquals(400, response.getStatus());
    }
}
