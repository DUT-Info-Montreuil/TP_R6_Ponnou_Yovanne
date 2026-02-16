package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.controller;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.mockito.MockedStatic;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.config.EntityManagerUtil;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.ws.rs.core.Application;

import static org.mockito.Mockito.mockStatic;

public abstract class UserControllerRestTestBase {

    protected static EntityManagerFactory emf;
    protected static JerseyTest jerseyTest;
    protected static MockedStatic<EntityManagerUtil> mockedUtil;

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
}
