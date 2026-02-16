package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.rest;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.junit.jupiter.api.*;
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

import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests d'intégration REST – AuthController (login)
 * Vérifie les payloads JSON et les codes HTTP
 */
class AuthControllerRestTest {

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
                                "org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.controller",
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
    void setUpData() {
        // Créer un utilisateur via le service pour avoir un mot de passe hashé
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

    // ==================== POST /login ====================

    @Test
    @DisplayName("POST /login → 200 avec token si identifiants valides")
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
    @DisplayName("POST /login → 401 si mot de passe incorrect")
    void login_shouldReturn401_whenWrongPassword() {
        Response response = jerseyTest.target("/login")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(Map.of("username", "testuser", "password", "wrongpassword")));

        assertEquals(401, response.getStatus());
        Map<?, ?> error = response.readEntity(Map.class);
        assertEquals("UNAUTHORIZED", error.get("error"));
    }

    @Test
    @DisplayName("POST /login → 401 si utilisateur inexistant")
    void login_shouldReturn401_whenUserNotFound() {
        Response response = jerseyTest.target("/login")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(Map.of("username", "unknown", "password", "password123")));

        assertEquals(401, response.getStatus());
    }

    @Test
    @DisplayName("POST /login → 400 si username manquant")
    void login_shouldReturn400_whenUsernameMissing() {
        Response response = jerseyTest.target("/login")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(Map.of("password", "password123")));

        assertEquals(400, response.getStatus());
        Map<?, ?> error = response.readEntity(Map.class);
        assertEquals("VALIDATION_ERROR", error.get("error"));
    }

    @Test
    @DisplayName("POST /login → 400 si password manquant")
    void login_shouldReturn400_whenPasswordMissing() {
        Response response = jerseyTest.target("/login")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(Map.of("username", "testuser")));

        assertEquals(400, response.getStatus());
    }
}
