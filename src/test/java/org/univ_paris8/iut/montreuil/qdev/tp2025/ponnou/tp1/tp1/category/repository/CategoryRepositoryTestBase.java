package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.repository;

import org.junit.jupiter.api.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

abstract class CategoryRepositoryTestBase {

    protected static EntityManagerFactory emf;
    protected EntityManager em;
    protected CategoryRepository categoryRepository;

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
        categoryRepository = new CategoryRepository();
    }

    @AfterEach
    void tearDown() {
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
        em.getTransaction().begin();
        em.createQuery("DELETE FROM Annonce").executeUpdate();
        em.createQuery("DELETE FROM Category").executeUpdate();
        em.getTransaction().commit();
        em.close();
    }
}
