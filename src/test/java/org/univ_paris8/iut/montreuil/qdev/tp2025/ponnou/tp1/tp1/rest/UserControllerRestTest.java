package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.rest;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.junit.jupiter.api.*;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.model.TokenStore;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.config.EntityManagerUtil;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests d'intégration REST – UserController
 * Vérifie les payloads JSON et les codes HTTP
 */
class UserControllerRestTest {

    private static EntityManagerFactory emf;
    private static JerseyTest jerseyTest;
    private static MockedStatic<EntityManagerUtil> mockedUtil;

    @BeforeAll
    static void setUp() throws Exception {
        emf = Persistence.createEntityManagerFactory("MasterAnnoncePU");

        mockedUtil = mockStatic(EntityManagerUtil.class);
        mockedUtil.when(EntityManagerUtil::getEntityManager).thenAnswer(inv -> emf.createEntityManager());

        jerseyTest = new JerseyTest() {
            @Override
            protected Application configure() {
                return new ResourceConfig()
                        .packages(
                                "org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.controller",
                                "org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.filter",
                                "org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.exception"
                        )
                        .register(JacksonFeature.class)
                        .register(org.glassfish.jersey.server.validation.ValidationFeature.class)
                        .property(ServerProperties.BV_FEATURE_DISABLE, false);
            }
        };
        jerseyTest.setUp();
    }

    @AfterAll
    static void tearDownAll() throws Exception {
        if (jerseyTest != null) jerseyTest.tearDown();
        if (mockedUtil != null) mockedUtil.close();
        if (emf != null) emf.close();
    }

    @AfterEach
    void cleanDb() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.createQuery("DELETE FROM User").executeUpdate();
        em.getTransaction().commit();
        em.close();
    }

    // ==================== POST /users ====================

    @Test
    @DisplayName("POST /users → 201 CREATED avec payload correct")
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
    @DisplayName("POST /users → 400 si username manquant")
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
    @DisplayName("POST /users → 400 si email invalide")
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
    @DisplayName("POST /users → 400 si password trop court")
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
    @DisplayName("POST /users → 400 si username dupliqué")
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

    // ==================== GET /users ====================

    @Test
    @DisplayName("GET /users → 200 avec pagination")
    void getAll_shouldReturn200WithPagination() {
        // Créer un utilisateur
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

    // ==================== GET /users/{id} ====================

    @Test
    @DisplayName("GET /users/{id} → 200 avec payload correct")
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
    @DisplayName("GET /users/{id} → 404 si inexistant")
    void getById_shouldReturn404_whenNotFound() {
        Response response = jerseyTest.target("/users/999")
                .request(MediaType.APPLICATION_JSON)
                .get();

        assertEquals(404, response.getStatus());
        Map<?, ?> error = response.readEntity(Map.class);
        assertEquals("NOT_FOUND", error.get("error"));
    }

    // ==================== PUT /users/{id} (protégé) ====================

    @Test
    @DisplayName("PUT /users/{id} → 401 sans token")
    void update_shouldReturn401_withoutToken() {
        Response response = jerseyTest.target("/users/1")
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.json(Map.of("username", "updated", "email", "up@test.com")));

        assertEquals(401, response.getStatus());
    }

    @Test
    @DisplayName("PUT /users/{id} → 200 avec token valide")
    void update_shouldReturn200_withValidToken() {
        Response createResp = jerseyTest.target("/users").request(MediaType.APPLICATION_JSON)
                .post(Entity.json(Map.of("username", "charlie", "email", "ch@test.com", "password", "password123")));
        Map<?, ?> created = createResp.readEntity(Map.class);
        Number id = (Number) created.get("id");

        String token = TokenStore.generateToken(id.longValue());

        Response response = jerseyTest.target("/users/" + id)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .put(Entity.json(Map.of("username", "charlie_updated", "email", "ch_new@test.com")));

        assertEquals(200, response.getStatus());
        Map<?, ?> json = response.readEntity(Map.class);
        assertEquals("charlie_updated", json.get("username"));
    }

    // ==================== DELETE /users/{id} (protégé) ====================

    @Test
    @DisplayName("DELETE /users/{id} → 401 sans token")
    void delete_shouldReturn401_withoutToken() {
        Response response = jerseyTest.target("/users/1")
                .request(MediaType.APPLICATION_JSON)
                .delete();

        assertEquals(401, response.getStatus());
    }

    @Test
    @DisplayName("DELETE /users/{id} → 204 avec token valide")
    void delete_shouldReturn204_withValidToken() {
        Response createResp = jerseyTest.target("/users").request(MediaType.APPLICATION_JSON)
                .post(Entity.json(Map.of("username", "toDelete", "email", "del@test.com", "password", "password123")));
        Map<?, ?> created = createResp.readEntity(Map.class);
        Number id = (Number) created.get("id");

        String token = TokenStore.generateToken(id.longValue());

        Response response = jerseyTest.target("/users/" + id)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .delete();

        assertEquals(204, response.getStatus());

        // Vérifier que l'utilisateur n'existe plus
        Response getResp = jerseyTest.target("/users/" + id)
                .request(MediaType.APPLICATION_JSON)
                .get();
        assertEquals(404, getResp.getStatus());
    }
}
