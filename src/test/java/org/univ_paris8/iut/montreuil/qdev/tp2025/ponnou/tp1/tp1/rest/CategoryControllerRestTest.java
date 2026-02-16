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
import java.util.List;
import java.util.Map;

import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests d'intégration REST – CategoryController
 * Vérifie les payloads JSON et les codes HTTP
 */
class CategoryControllerRestTest {

    private static EntityManagerFactory emf;
    private static JerseyTest jerseyTest;
    private static MockedStatic<EntityManagerUtil> mockedUtil;
    private String token;

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
                                "org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.controller",
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

    @BeforeEach
    void setUpToken() {
        token = TokenStore.generateToken(1L);
    }

    @AfterEach
    void cleanDb() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.createQuery("DELETE FROM Annonce").executeUpdate();
        em.createQuery("DELETE FROM Category").executeUpdate();
        em.getTransaction().commit();
        em.close();
    }

    // ==================== POST /categories ====================

    @Test
    @DisplayName("POST /categories → 201 CREATED avec payload correct")
    void createCategory_shouldReturn201() {
        Response response = jerseyTest.target("/categories")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .post(Entity.json(Map.of("label", "Immobilier")));

        assertEquals(201, response.getStatus());
        assertTrue(response.getLocation().getPath().contains("/categories/"));

        Map<?, ?> json = response.readEntity(Map.class);
        assertEquals("Immobilier", json.get("label"));
        assertNotNull(json.get("id"));
    }

    @Test
    @DisplayName("POST /categories → 401 sans token")
    void createCategory_shouldReturn401_withoutToken() {
        Response response = jerseyTest.target("/categories")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(Map.of("label", "Immobilier")));

        assertEquals(401, response.getStatus());
    }

    @Test
    @DisplayName("POST /categories → 400 si label manquant")
    void createCategory_shouldReturn400_whenLabelMissing() {
        Response response = jerseyTest.target("/categories")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .post(Entity.json(Map.of()));

        assertEquals(400, response.getStatus());
        Map<?, ?> error = response.readEntity(Map.class);
        assertEquals("VALIDATION_ERROR", error.get("error"));
    }

    @Test
    @DisplayName("POST /categories → 400 si label dupliqué")
    void createCategory_shouldReturn400_whenDuplicateLabel() {
        jerseyTest.target("/categories")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .post(Entity.json(Map.of("label", "Immobilier")));

        Response response = jerseyTest.target("/categories")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .post(Entity.json(Map.of("label", "Immobilier")));

        assertEquals(400, response.getStatus());
    }

    // ==================== GET /categories ====================

    @Test
    @DisplayName("GET /categories → 200 avec pagination")
    void getAll_shouldReturn200WithPagination() {
        jerseyTest.target("/categories")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .post(Entity.json(Map.of("label", "Cat1")));
        jerseyTest.target("/categories")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .post(Entity.json(Map.of("label", "Cat2")));

        Response response = jerseyTest.target("/categories")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .request(MediaType.APPLICATION_JSON)
                .get();

        assertEquals(200, response.getStatus());
        Map<?, ?> json = response.readEntity(Map.class);
        List<?> items = (List<?>) json.get("items");
        assertEquals(2, items.size());
        assertEquals(2, ((Number) json.get("totalItems")).intValue());
    }

    // ==================== GET /categories/{id} ====================

    @Test
    @DisplayName("GET /categories/{id} → 200 avec payload correct")
    void getById_shouldReturn200() {
        Response createResp = jerseyTest.target("/categories")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .post(Entity.json(Map.of("label", "Services")));
        Map<?, ?> created = createResp.readEntity(Map.class);
        Number id = (Number) created.get("id");

        Response response = jerseyTest.target("/categories/" + id)
                .request(MediaType.APPLICATION_JSON)
                .get();

        assertEquals(200, response.getStatus());
        Map<?, ?> json = response.readEntity(Map.class);
        assertEquals("Services", json.get("label"));
    }

    @Test
    @DisplayName("GET /categories/{id} → 404 si inexistant")
    void getById_shouldReturn404_whenNotFound() {
        Response response = jerseyTest.target("/categories/999")
                .request(MediaType.APPLICATION_JSON)
                .get();

        assertEquals(404, response.getStatus());
        Map<?, ?> error = response.readEntity(Map.class);
        assertEquals("NOT_FOUND", error.get("error"));
    }

    // ==================== PUT /categories/{id} ====================

    @Test
    @DisplayName("PUT /categories/{id} → 200 avec token valide")
    void update_shouldReturn200() {
        Response createResp = jerseyTest.target("/categories")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .post(Entity.json(Map.of("label", "Ancien")));
        Map<?, ?> created = createResp.readEntity(Map.class);
        Number id = (Number) created.get("id");

        Response response = jerseyTest.target("/categories/" + id)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .put(Entity.json(Map.of("label", "Nouveau")));

        assertEquals(200, response.getStatus());
        Map<?, ?> json = response.readEntity(Map.class);
        assertEquals("Nouveau", json.get("label"));
    }

    @Test
    @DisplayName("PUT /categories/{id} → 401 sans token")
    void update_shouldReturn401_withoutToken() {
        Response response = jerseyTest.target("/categories/1")
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.json(Map.of("label", "Nouveau")));

        assertEquals(401, response.getStatus());
    }

    // ==================== DELETE /categories/{id} ====================

    @Test
    @DisplayName("DELETE /categories/{id} → 204 avec token valide")
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

        // Vérifier suppression
        Response getResp = jerseyTest.target("/categories/" + id)
                .request(MediaType.APPLICATION_JSON)
                .get();
        assertEquals(404, getResp.getStatus());
    }

    @Test
    @DisplayName("DELETE /categories/{id} → 401 sans token")
    void delete_shouldReturn401_withoutToken() {
        Response response = jerseyTest.target("/categories/1")
                .request(MediaType.APPLICATION_JSON)
                .delete();

        assertEquals(401, response.getStatus());
    }
}
