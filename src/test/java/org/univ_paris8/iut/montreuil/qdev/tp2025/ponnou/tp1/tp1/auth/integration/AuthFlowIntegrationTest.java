package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.integration;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.jaas.JaasConfig;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.config.EntityManagerUtil;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.service.UserService;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

class AuthFlowIntegrationTest {

    private static EntityManagerFactory emf;
    private static JerseyTest jerseyTest;
    private static MockedStatic<EntityManagerUtil> mockedUtil;

    @BeforeAll
    static void setUpAll() throws Exception {
        emf = Persistence.createEntityManagerFactory("MasterAnnoncePU");

        mockedUtil = mockStatic(EntityManagerUtil.class);
        mockedUtil.when(EntityManagerUtil::getEntityManager).thenAnswer(inv -> emf.createEntityManager());

        JaasConfig.install();

        jerseyTest = new JerseyTest() {
            @Override
            protected Application configure() {
                return new ResourceConfig()
                        .packages(
                                "org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.controller",
                                "org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.filter",
                                "org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.controller",
                                "org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.exception")
                        .register(JacksonFeature.class)
                        .register(org.glassfish.jersey.server.validation.ValidationFeature.class)
                        .property(ServerProperties.BV_FEATURE_DISABLE, false);
            }
        };
        jerseyTest.setUp();
    }

    @AfterAll
    static void tearDownAll() throws Exception {
        if (jerseyTest != null)
            jerseyTest.tearDown();
        if (mockedUtil != null)
            mockedUtil.close();
        if (emf != null)
            emf.close();
    }

    @BeforeEach
    void setUpData() {
        UserService userService = new UserService();
        userService.create("testuser", "test@test.com", "password123");
    }

    @AfterEach
    void cleanDb() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.createQuery("DELETE FROM User").executeUpdate();
        em.getTransaction().commit();
        em.close();
    }

    @Test
    @DisplayName("login → récupérer token → appeler endpoint protégé avec token → 200")
    void fullFlow_login_thenAccessSecuredEndpoint() {
        // 1. Login pour obtenir un token
        Response loginResponse = jerseyTest.target("/login")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(Map.of("username", "testuser", "password", "password123")));

        assertEquals(200, loginResponse.getStatus());
        Map<?, ?> loginJson = loginResponse.readEntity(Map.class);
        String token = (String) loginJson.get("token");
        assertNotNull(token);
        assertEquals("testuser", loginJson.get("username"));

        // 2. Récupérer l'ID du user créé
        Response getUsersResponse = jerseyTest.target("/users")
                .request(MediaType.APPLICATION_JSON)
                .get();
        assertEquals(200, getUsersResponse.getStatus());
        Map<?, ?> usersJson = getUsersResponse.readEntity(Map.class);
        java.util.List<?> items = (java.util.List<?>) usersJson.get("items");
        Map<?, ?> userMap = (Map<?, ?>) items.get(0);
        Number userId = (Number) userMap.get("id");

        // 3. Appeler endpoint protégé (PUT /users/{id}) avec le token → 200
        Response securedResponse = jerseyTest.target("/users/" + userId)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .put(Entity.json(Map.of("username", "testuser_updated", "email", "updated@test.com")));

        assertEquals(200, securedResponse.getStatus());
    }

    @Test
    @DisplayName("appeler endpoint protégé sans token → 401")
    void securedEndpoint_shouldReturn401_withoutToken() {
        Response response = jerseyTest.target("/users/1")
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.json(Map.of("username", "hacker", "email", "hack@test.com")));

        assertEquals(401, response.getStatus());
        Map<?, ?> error = response.readEntity(Map.class);
        assertEquals("UNAUTHORIZED", error.get("error"));
    }

    @Test
    @DisplayName("appeler endpoint protégé avec token invalide → 401")
    void securedEndpoint_shouldReturn401_withInvalidToken() {
        Response response = jerseyTest.target("/users/1")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer invalid-token-xyz")
                .put(Entity.json(Map.of("username", "hacker", "email", "hack@test.com")));

        assertEquals(401, response.getStatus());
        Map<?, ?> error = response.readEntity(Map.class);
        assertEquals("UNAUTHORIZED", error.get("error"));
    }

    @Test
    @DisplayName("appeler endpoint protégé avec token → 200")
    void securedEndpoint_shouldReturn200_withValidToken() {
        // Créer un second user pour le DELETE
        Response createResp = jerseyTest.target("/users")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(Map.of("username", "toDelete", "email", "del@test.com", "password", "password123")));
        assertEquals(201, createResp.getStatus());
        Map<?, ?> created = createResp.readEntity(Map.class);
        Number id = (Number) created.get("id");

        // Login pour obtenir un token
        Response loginResponse = jerseyTest.target("/login")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(Map.of("username", "toDelete", "password", "password123")));
        assertEquals(200, loginResponse.getStatus());
        String token = (String) loginResponse.readEntity(Map.class).get("token");

        // Appeler DELETE protégé avec le token → 204
        Response deleteResponse = jerseyTest.target("/users/" + id)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .delete();

        assertEquals(204, deleteResponse.getStatus());
    }

    @Test
    @DisplayName("endpoint non protégé accessible sans token → 200")
    void publicEndpoint_shouldReturn200_withoutToken() {
        Response response = jerseyTest.target("/users")
                .request(MediaType.APPLICATION_JSON)
                .get();

        assertEquals(200, response.getStatus());
    }
}
