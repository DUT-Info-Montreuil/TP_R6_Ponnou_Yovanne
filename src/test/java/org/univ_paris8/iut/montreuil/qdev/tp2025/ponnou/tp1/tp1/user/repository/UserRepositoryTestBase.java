package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.repository;

import org.junit.jupiter.api.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

abstract class UserRepositoryTestBase {

    protected static EntityManagerFactory emf;
    protected EntityManager em;
    protected UserRepository userRepository;

    @BeforeAll
    static void setUpFactory() {
        emf = Persistence.createEntityManagerFactory("MasterAnnoncePU");
    }

    @AfterAll
    static void tearDownFactory() {
        if (emf != null) emf.close();
    }

    @BeforeEach
    void setUp() {
        em = emf.createEntityManager();
        userRepository = new UserRepository();
    }

    @AfterEach
    void tearDown() {
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
        em.getTransaction().begin();
        em.createQuery("DELETE FROM Annonce").executeUpdate();
        em.createQuery("DELETE FROM User").executeUpdate();
        em.getTransaction().commit();
        em.close();
    }
}
