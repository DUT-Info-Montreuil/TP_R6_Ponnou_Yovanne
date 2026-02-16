package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.rest;

import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.junit.jupiter.api.*;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.model.TokenStore;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.model.Category;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.config.EntityManagerUtil;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.model.User;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests d'intégration REST – AnnonceController
 * Vérifie les payloads JSON, codes HTTP, et règles métier via l'API
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AnnonceControllerRestTest {

    private static EntityManagerFactory emf;
    private static JerseyTest jerseyTest;
    private static MockedStatic<EntityManagerUtil> mockedUtil;

    private User testUser;
    private User otherUser;
    private Category testCategory;
    private String token;
    private String otherToken;

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
                                "org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.controller",
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
    void setUpData() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        testUser = new User("author", "author@test.com", "password123");
        otherUser = new User("other", "other@test.com", "password123");
        testCategory = new Category("Immobilier");
        em.persist(testUser);
        em.persist(otherUser);
        em.persist(testCategory);
        em.getTransaction().commit();
        em.close();

        token = TokenStore.generateToken(testUser.getId());
        otherToken = TokenStore.generateToken(otherUser.getId());
    }

    @AfterEach
    void cleanDb() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.createQuery("DELETE FROM Annonce").executeUpdate();
        em.createQuery("DELETE FROM Category").executeUpdate();
        em.createQuery("DELETE FROM User").executeUpdate();
        em.getTransaction().commit();
        em.close();
    }

    // ==================== Helpers ====================

    private Map<String, Object> annonceBody() {
        Map<String, Object> body = new HashMap<>();
        body.put("title", "Appart F3");
        body.put("description", "Bel appartement lumineux");
        body.put("adress", "Paris 10");
        body.put("mail", "contact@test.com");
        body.put("categoryId", testCategory.getId());
        return body;
    }

    private Number createAnnonce() {
        Response resp = jerseyTest.target("/annonces")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .post(Entity.json(annonceBody()));
        if (resp.getStatus() != 201) {
            fail("createAnnonce helper failed with status " + resp.getStatus()
                    + ", body: " + resp.readEntity(String.class));
        }
        return (Number) resp.readEntity(Map.class).get("id");
    }

    private void publishAnnonce(Number id) {
        jerseyTest.target("/annonces/" + id + "/publish")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .put(Entity.json(""));
    }

    private void archiveAnnonce(Number id) {
        jerseyTest.target("/annonces/" + id + "/archive")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .put(Entity.json(""));
    }

    // ==================== POST /annonces ====================

    @Test
    @DisplayName("POST /annonces → 201 CREATED avec payload correct")
    void createAnnonce_shouldReturn201() {
        Response response = jerseyTest.target("/annonces")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .post(Entity.json(annonceBody()));

        assertEquals(201, response.getStatus());
        assertTrue(response.getLocation().getPath().contains("/annonces/"));

        Map<?, ?> json = response.readEntity(Map.class);
        assertEquals("Appart F3", json.get("title"));
        assertEquals("Bel appartement lumineux", json.get("description"));
        assertEquals("Paris 10", json.get("adress"));
        assertEquals("contact@test.com", json.get("mail"));
        assertEquals("DRAFT", json.get("status"));
        assertEquals("author", json.get("authorUsername"));
        assertEquals("Immobilier", json.get("categoryLabel"));
        assertNotNull(json.get("id"));
        assertNotNull(json.get("date"));
    }

    @Test
    @DisplayName("POST /annonces → 401 sans token")
    void createAnnonce_shouldReturn401_withoutToken() {
        Response response = jerseyTest.target("/annonces")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(annonceBody()));

        assertEquals(401, response.getStatus());
        Map<?, ?> error = response.readEntity(Map.class);
        assertEquals("UNAUTHORIZED", error.get("error"));
    }

    @Test
    @DisplayName("POST /annonces → 400 si titre manquant")
    void createAnnonce_shouldReturn400_whenTitleMissing() {
        Map<String, Object> body = annonceBody();
        body.remove("title");

        Response response = jerseyTest.target("/annonces")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .post(Entity.json(body));

        assertEquals(400, response.getStatus());
        Map<?, ?> error = response.readEntity(Map.class);
        assertEquals("VALIDATION_ERROR", error.get("error"));
    }

    @Test
    @DisplayName("POST /annonces → 400 si mail invalide")
    void createAnnonce_shouldReturn400_whenMailInvalid() {
        Map<String, Object> body = annonceBody();
        body.put("mail", "not-an-email");

        Response response = jerseyTest.target("/annonces")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .post(Entity.json(body));

        assertEquals(400, response.getStatus());
    }

    @Test
    @DisplayName("POST /annonces → 400 si categoryId manquant")
    void createAnnonce_shouldReturn400_whenCategoryMissing() {
        Map<String, Object> body = annonceBody();
        body.remove("categoryId");

        Response response = jerseyTest.target("/annonces")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .post(Entity.json(body));

        assertEquals(400, response.getStatus());
    }

    // ==================== GET /annonces ====================

    @Test
    @DisplayName("GET /annonces → 200 avec pagination")
    void getAll_shouldReturn200WithPagination() {
        createAnnonce();
        createAnnonce();

        Response response = jerseyTest.target("/annonces")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .request(MediaType.APPLICATION_JSON)
                .get();

        assertEquals(200, response.getStatus());
        Map<?, ?> json = response.readEntity(Map.class);
        List<?> items = (List<?>) json.get("items");
        assertEquals(2, items.size());
        assertEquals(0, json.get("page"));
        assertEquals(10, json.get("size"));
        assertEquals(2, ((Number) json.get("totalItems")).intValue());
        assertEquals(1, ((Number) json.get("totalPages")).intValue());
    }

    @Test
    @DisplayName("GET /annonces → pagination taille 1 retourne 1 item")
    void getAll_shouldRespectPageSize() {
        createAnnonce();
        createAnnonce();
        createAnnonce();

        Response response = jerseyTest.target("/annonces")
                .queryParam("page", 0)
                .queryParam("size", 1)
                .request(MediaType.APPLICATION_JSON)
                .get();

        assertEquals(200, response.getStatus());
        Map<?, ?> json = response.readEntity(Map.class);
        List<?> items = (List<?>) json.get("items");
        assertEquals(1, items.size());
        assertEquals(3, ((Number) json.get("totalItems")).intValue());
        assertEquals(3, ((Number) json.get("totalPages")).intValue());
    }

    // ==================== GET /annonces/{id} ====================

    @Test
    @DisplayName("GET /annonces/{id} → 200 avec payload complet")
    void getById_shouldReturn200() {
        Number id = createAnnonce();

        Response response = jerseyTest.target("/annonces/" + id)
                .request(MediaType.APPLICATION_JSON)
                .get();

        assertEquals(200, response.getStatus());
        Map<?, ?> json = response.readEntity(Map.class);
        assertEquals("Appart F3", json.get("title"));
        assertEquals("DRAFT", json.get("status"));
        assertEquals("author", json.get("authorUsername"));
        assertEquals("Immobilier", json.get("categoryLabel"));
    }

    @Test
    @DisplayName("GET /annonces/{id} → 404 si inexistant")
    void getById_shouldReturn404_whenNotFound() {
        Response response = jerseyTest.target("/annonces/999")
                .request(MediaType.APPLICATION_JSON)
                .get();

        assertEquals(404, response.getStatus());
        Map<?, ?> error = response.readEntity(Map.class);
        assertEquals("NOT_FOUND", error.get("error"));
    }

    // ==================== PUT /annonces/{id} ====================

    @Test
    @DisplayName("PUT /annonces/{id} → 200 modifie l'annonce")
    void update_shouldReturn200() {
        Number id = createAnnonce();

        Map<String, Object> body = new HashMap<>();
        body.put("title", "Nouveau titre");
        body.put("description", "Nouvelle description");
        body.put("adress", "Lyon 1");
        body.put("mail", "new@test.com");
        body.put("categoryId", testCategory.getId());

        Response response = jerseyTest.target("/annonces/" + id)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .put(Entity.json(body));

        assertEquals(200, response.getStatus());
        Map<?, ?> json = response.readEntity(Map.class);
        assertEquals("Nouveau titre", json.get("title"));
        assertEquals("Lyon 1", json.get("adress"));
    }

    @Test
    @DisplayName("PUT /annonces/{id} → 401 sans token")
    void update_shouldReturn401_withoutToken() {
        Response response = jerseyTest.target("/annonces/1")
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.json(annonceBody()));

        assertEquals(401, response.getStatus());
    }

    @Test
    @DisplayName("PUT /annonces/{id} → 403 si non-auteur")
    void update_shouldReturn403_whenNotOwner() {
        Number id = createAnnonce();

        Map<String, Object> body = new HashMap<>();
        body.put("title", "Nouveau titre");
        body.put("description", "Nouvelle description");
        body.put("adress", "Lyon 1");
        body.put("mail", "new@test.com");
        body.put("categoryId", testCategory.getId());

        Response response = jerseyTest.target("/annonces/" + id)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + otherToken)
                .put(Entity.json(body));

        assertEquals(403, response.getStatus());
        Map<?, ?> error = response.readEntity(Map.class);
        assertEquals("FORBIDDEN", error.get("error"));
    }

    @Test
    @DisplayName("PUT /annonces/{id} → 409 si annonce PUBLISHED")
    void update_shouldReturn409_whenPublished() {
        Number id = createAnnonce();
        publishAnnonce(id);

        Map<String, Object> body = new HashMap<>();
        body.put("title", "Nouveau titre");
        body.put("description", "Nouvelle description");
        body.put("adress", "Lyon 1");
        body.put("mail", "new@test.com");
        body.put("categoryId", testCategory.getId());

        Response response = jerseyTest.target("/annonces/" + id)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .put(Entity.json(body));

        assertEquals(409, response.getStatus());
        Map<?, ?> error = response.readEntity(Map.class);
        assertEquals("CONFLICT", error.get("error"));
    }

    // ==================== PUT /annonces/{id}/publish ====================

    @Test
    @DisplayName("PUT /annonces/{id}/publish → 200 passe en PUBLISHED")
    void publish_shouldReturn200() {
        Number id = createAnnonce();

        Response response = jerseyTest.target("/annonces/" + id + "/publish")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .put(Entity.json(""));

        assertEquals(200, response.getStatus());
        Map<?, ?> json = response.readEntity(Map.class);
        assertEquals("PUBLISHED", json.get("status"));
    }

    @Test
    @DisplayName("PUT /annonces/{id}/publish → 401 sans token")
    void publish_shouldReturn401_withoutToken() {
        Response response = jerseyTest.target("/annonces/1/publish")
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.json(""));

        assertEquals(401, response.getStatus());
    }

    @Test
    @DisplayName("PUT /annonces/{id}/publish → 403 si non-auteur")
    void publish_shouldReturn403_whenNotOwner() {
        Number id = createAnnonce();

        Response response = jerseyTest.target("/annonces/" + id + "/publish")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + otherToken)
                .put(Entity.json(""));

        assertEquals(403, response.getStatus());
    }

    @Test
    @DisplayName("PUT /annonces/{id}/publish → 409 si déjà archivée")
    void publish_shouldReturn409_whenArchived() {
        Number id = createAnnonce();
        publishAnnonce(id);
        archiveAnnonce(id);

        Response response = jerseyTest.target("/annonces/" + id + "/publish")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .put(Entity.json(""));

        assertEquals(409, response.getStatus());
        Map<?, ?> error = response.readEntity(Map.class);
        assertEquals("CONFLICT", error.get("error"));
    }

    // ==================== PUT /annonces/{id}/archive ====================

    @Test
    @DisplayName("PUT /annonces/{id}/archive → 200 passe en ARCHIVED")
    void archive_shouldReturn200() {
        Number id = createAnnonce();
        publishAnnonce(id);

        Response response = jerseyTest.target("/annonces/" + id + "/archive")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .put(Entity.json(""));

        assertEquals(200, response.getStatus());
        Map<?, ?> json = response.readEntity(Map.class);
        assertEquals("ARCHIVED", json.get("status"));
    }

    @Test
    @DisplayName("PUT /annonces/{id}/archive → 403 si non-auteur")
    void archive_shouldReturn403_whenNotOwner() {
        Number id = createAnnonce();
        publishAnnonce(id);

        Response response = jerseyTest.target("/annonces/" + id + "/archive")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + otherToken)
                .put(Entity.json(""));

        assertEquals(403, response.getStatus());
    }

    // ==================== DELETE /annonces/{id} ====================

    @Test
    @DisplayName("DELETE /annonces/{id} → 204 si annonce archivée")
    void delete_shouldReturn204_whenArchived() {
        Number id = createAnnonce();
        publishAnnonce(id);
        archiveAnnonce(id);

        Response response = jerseyTest.target("/annonces/" + id)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .delete();

        assertEquals(204, response.getStatus());

        // Vérifier suppression
        Response getResp = jerseyTest.target("/annonces/" + id)
                .request(MediaType.APPLICATION_JSON)
                .get();
        assertEquals(404, getResp.getStatus());
    }

    @Test
    @DisplayName("DELETE /annonces/{id} → 401 sans token")
    void delete_shouldReturn401_withoutToken() {
        Response response = jerseyTest.target("/annonces/1")
                .request(MediaType.APPLICATION_JSON)
                .delete();

        assertEquals(401, response.getStatus());
    }

    @Test
    @DisplayName("DELETE /annonces/{id} → 403 si non-auteur")
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
    @DisplayName("DELETE /annonces/{id} → 409 si annonce non archivée (DRAFT)")
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
    @DisplayName("DELETE /annonces/{id} → 409 si annonce PUBLISHED (non archivée)")
    void delete_shouldReturn409_whenPublished() {
        Number id = createAnnonce();
        publishAnnonce(id);

        Response response = jerseyTest.target("/annonces/" + id)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .delete();

        assertEquals(409, response.getStatus());
    }

    // ==================== PATCH /annonces/{id} ====================

    @Test
    @Order(100)
    @DisplayName("PATCH /annonces/{id} → 200 modifie partiellement")
    void patch_shouldReturn200() {
        Number id = createAnnonce();

        Map<String, Object> patchBody = new HashMap<>();
        patchBody.put("title", "Titre partiel");

        Response response = jerseyTest.target("/annonces/" + id)
                .property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .method("PATCH", Entity.json(patchBody));

        assertEquals(200, response.getStatus());
        Map<?, ?> json = response.readEntity(Map.class);
        assertEquals("Titre partiel", json.get("title"));
        // Les autres champs restent inchangés
        assertEquals("Bel appartement lumineux", json.get("description"));
    }

    @Test
    @Order(101)
    @DisplayName("PATCH /annonces/{id} → 403 si non-auteur")
    void patch_shouldReturn403_whenNotOwner() {
        Number id = createAnnonce();

        Response response = jerseyTest.target("/annonces/" + id)
                .property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + otherToken)
                .method("PATCH", Entity.json(Map.of("title", "Titre partiel")));

        assertEquals(403, response.getStatus());
    }

    @Test
    @Order(102)
    @DisplayName("PATCH /annonces/{id} → 409 si annonce PUBLISHED")
    void patch_shouldReturn409_whenPublished() {
        Number id = createAnnonce();
        publishAnnonce(id);

        Response response = jerseyTest.target("/annonces/" + id)
                .property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .method("PATCH", Entity.json(Map.of("title", "Titre modifié")));

        assertEquals(409, response.getStatus());
    }
}
