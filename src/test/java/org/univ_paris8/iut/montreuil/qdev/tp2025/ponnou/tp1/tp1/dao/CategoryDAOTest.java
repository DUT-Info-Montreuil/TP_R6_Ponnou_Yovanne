package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.dao;

import org.junit.jupiter.api.*;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.model.Category;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Niveau 1 – Tests DAO Category : CRUD + recherche
 */
class CategoryDAOTest {

    private static EntityManagerFactory emf;
    private EntityManager em;
    private CategoryDAO categoryDAO;

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
        categoryDAO = new CategoryDAO();
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

    @Test
    @DisplayName("save() persiste une catégorie")
    void save_shouldPersistCategory() {
        Category cat = new Category("Immobilier");
        em.getTransaction().begin();
        Category saved = categoryDAO.save(em, cat);

        assertNotNull(saved.getId());
        assertEquals("Immobilier", saved.getLabel());
    }

    @Test
    @DisplayName("findById() retourne la catégorie existante")
    void findById_shouldReturnCategory() {
        Category cat = new Category("Véhicules");
        em.getTransaction().begin();
        categoryDAO.save(em, cat);

        Optional<Category> found = categoryDAO.findById(em, cat.getId());

        assertTrue(found.isPresent());
        assertEquals("Véhicules", found.get().getLabel());
    }

    @Test
    @DisplayName("update() modifie le label")
    void update_shouldModifyLabel() {
        Category cat = new Category("Ancien label");
        em.getTransaction().begin();
        categoryDAO.save(em, cat);

        cat.setLabel("Nouveau label");
        em.getTransaction().begin();
        Category updated = categoryDAO.update(em, cat);

        assertEquals("Nouveau label", updated.getLabel());
    }

    @Test
    @DisplayName("deleteById() supprime la catégorie")
    void deleteById_shouldRemoveCategory() {
        Category cat = new Category("A supprimer");
        em.getTransaction().begin();
        categoryDAO.save(em, cat);
        Long id = cat.getId();

        em.getTransaction().begin();
        categoryDAO.deleteById(em, id);

        assertTrue(categoryDAO.findById(em, id).isEmpty());
    }

    @Test
    @DisplayName("findWithFilters() avec tri alphabétique")
    void findWithFilters_shouldReturnSortedByLabel() {
        em.getTransaction().begin();
        em.persist(new Category("Véhicules"));
        em.persist(new Category("Animaux"));
        em.persist(new Category("Immobilier"));
        em.getTransaction().commit();

        List<Category> categories = categoryDAO.findWithFilters(
                em, null, null, null, "label ASC", null);

        assertEquals(3, categories.size());
        assertEquals("Animaux", categories.get(0).getLabel());
        assertEquals("Immobilier", categories.get(1).getLabel());
        assertEquals("Véhicules", categories.get(2).getLabel());
    }

    @Test
    @DisplayName("findOneWithFilters() trouve par label")
    void findOneWithFilters_shouldFindByLabel() {
        em.getTransaction().begin();
        categoryDAO.save(em, new Category("Emploi"));

        Optional<Category> found = categoryDAO.findOneWithFilters(em, Map.of("label", "Emploi"));

        assertTrue(found.isPresent());
        assertEquals("Emploi", found.get().getLabel());
    }
}
