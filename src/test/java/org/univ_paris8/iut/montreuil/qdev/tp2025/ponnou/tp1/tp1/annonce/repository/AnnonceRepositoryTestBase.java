package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.repository;

import org.junit.jupiter.api.*;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.model.Annonce;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.model.AnnonceStatus;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.model.Category;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.model.User;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

abstract class AnnonceRepositoryTestBase {

    protected static EntityManagerFactory emf;
    protected EntityManager em;
    protected AnnonceRepository annonceRepository;

    protected User testUser;
    protected Category testCategory;

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
        annonceRepository = new AnnonceRepository();

        testUser = new User("testuser", "testuser@test.com", "password123");
        testCategory = new Category("Immobilier");

        em.getTransaction().begin();
        em.persist(testUser);
        em.persist(testCategory);
        em.getTransaction().commit();
    }

    @AfterEach
    void tearDown() {
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
        em.getTransaction().begin();
        em.createQuery("DELETE FROM Annonce").executeUpdate();
        em.createQuery("DELETE FROM Category").executeUpdate();
        em.createQuery("DELETE FROM User").executeUpdate();
        em.getTransaction().commit();
        em.close();
    }

    protected Annonce createAnnonce(String title, String description, AnnonceStatus status) {
        Annonce annonce = new Annonce(title, description, "Paris", "contact@test.com");
        annonce.setAuthor(testUser);
        annonce.setCategory(testCategory);
        annonce.setStatus(status);
        return annonce;
    }
}
