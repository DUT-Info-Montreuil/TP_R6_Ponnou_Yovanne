package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.controller;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockedStatic;
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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mockStatic;

public abstract class AnnonceControllerRestTestBase {

    protected static EntityManagerFactory emf;
    protected static JerseyTest jerseyTest;
    protected static MockedStatic<EntityManagerUtil> mockedUtil;

    protected User testUser;
    protected User otherUser;
    protected Category testCategory;
    protected String token;
    protected String otherToken;

    @BeforeAll
    static void setUpAll() throws Exception {
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
        Response warmup = jerseyTest.target("/annonces")
                .request(MediaType.APPLICATION_JSON)
                .get();
        warmup.close();
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

        token = TokenStore.generateToken(testUser.getId(), testUser.getUsername());
        otherToken = TokenStore.generateToken(otherUser.getId(), otherUser.getUsername());
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

    protected Map<String, Object> annonceBody() {
        Map<String, Object> body = new HashMap<>();
        body.put("title", "Appart F3");
        body.put("description", "Bel appartement lumineux");
        body.put("adress", "Paris 10");
        body.put("mail", "contact@test.com");
        body.put("categoryId", testCategory.getId());
        return body;
    }

    protected Number createAnnonce() {
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

    protected void publishAnnonce(Number id) {
        jerseyTest.target("/annonces/" + id + "/publish")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .put(Entity.json(""));
    }

    protected void archiveAnnonce(Number id) {
        jerseyTest.target("/annonces/" + id + "/archive")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .put(Entity.json(""));
    }
}
