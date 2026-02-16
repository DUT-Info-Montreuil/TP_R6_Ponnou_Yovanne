package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreateUserControllerRestTest extends UserControllerRestTestBase {

    @Test
    @DisplayName("POST /users -> 201 CREATED avec payload correct")
    void createUser_shouldReturn201() {
        Map<String, String> body = Map.of(
                "username", "alice",
                "email", "alice@test.com",
                "password", "password123"
        );

        Response response = jerseyTest.target("/users")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(body));

        assertEquals(201, response.getStatus());
        assertTrue(response.getLocation().getPath().contains("/users/"));

        Map<?, ?> json = response.readEntity(Map.class);
        assertEquals("alice", json.get("username"));
        assertEquals("alice@test.com", json.get("email"));
        assertNotNull(json.get("id"));
        assertNull(json.get("password"));
    }

    @Test
    @DisplayName("POST /users -> 400 si username manquant")
    void createUser_shouldReturn400_whenUsernameMissing() {
        Map<String, String> body = Map.of(
                "email", "alice@test.com",
                "password", "password123"
        );

        Response response = jerseyTest.target("/users")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(body));

        assertEquals(400, response.getStatus());
        Map<?, ?> error = response.readEntity(Map.class);
        assertEquals("VALIDATION_ERROR", error.get("error"));
    }

    @Test
    @DisplayName("POST /users -> 400 si email invalide")
    void createUser_shouldReturn400_whenEmailInvalid() {
        Map<String, String> body = Map.of(
                "username", "alice",
                "email", "not-an-email",
                "password", "password123"
        );

        Response response = jerseyTest.target("/users")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(body));

        assertEquals(400, response.getStatus());
    }

    @Test
    @DisplayName("POST /users -> 400 si password trop court")
    void createUser_shouldReturn400_whenPasswordTooShort() {
        Map<String, String> body = Map.of(
                "username", "alice",
                "email", "alice@test.com",
                "password", "abc"
        );

        Response response = jerseyTest.target("/users")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(body));

        assertEquals(400, response.getStatus());
    }

    @Test
    @DisplayName("POST /users -> 400 si username duplique")
    void createUser_shouldReturn400_whenDuplicateUsername() {
        Map<String, String> body = Map.of(
                "username", "alice",
                "email", "alice@test.com",
                "password", "password123"
        );
        jerseyTest.target("/users").request(MediaType.APPLICATION_JSON)
                .post(Entity.json(body));

        Map<String, String> body2 = Map.of(
                "username", "alice",
                "email", "alice2@test.com",
                "password", "password123"
        );

        Response response = jerseyTest.target("/users")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(body2));

        assertEquals(400, response.getStatus());
    }
}
