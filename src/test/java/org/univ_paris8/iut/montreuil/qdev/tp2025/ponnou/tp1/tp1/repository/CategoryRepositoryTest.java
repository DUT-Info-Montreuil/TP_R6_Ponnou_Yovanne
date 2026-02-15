package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.repository;

import org.junit.jupiter.api.*;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.model.Category;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.repository.CategoryRepository;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CategoryRepositoryTest {

    private static EntityManagerFactory emf;
    private EntityManager em;
    private CategoryRepository categoryRepository;

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

    @Test
    @DisplayName("save() persiste une categorie")
    void save_shouldPersistCategory() {
        Category cat = new Category("Immobilier");
        em.getTransaction().begin();
        Category saved = categoryRepository.save(em, cat);

        assertNotNull(saved.getId());
        assertEquals("Immobilier", saved.getLabel());
    }

    @Test
    @DisplayName("findById() retourne la categorie existante")
    void findById_shouldReturnCategory() {
        Category cat = new Category("Vehicules");
        em.getTransaction().begin();
        categoryRepository.save(em, cat);

        Optional<Category> found = categoryRepository.findById(em, cat.getId());

        assertTrue(found.isPresent());
        assertEquals("Vehicules", found.get().getLabel());
    }

    @Test
    @DisplayName("update() modifie le label")
    void update_shouldModifyLabel() {
        Category cat = new Category("Ancien label");
        em.getTransaction().begin();
        categoryRepository.save(em, cat);
        em.getTransaction().commit();

        cat.setLabel("Nouveau label");
        em.getTransaction().begin();
        Category updated = categoryRepository.update(em, cat);

        assertEquals("Nouveau label", updated.getLabel());
    }

    @Test
    @DisplayName("deleteById() supprime la categorie")
    void deleteById_shouldRemoveCategory() {
        Category cat = new Category("A supprimer");
        em.getTransaction().begin();
        categoryRepository.save(em, cat);
        Long id = cat.getId();
        em.getTransaction().commit();

        em.getTransaction().begin();
        categoryRepository.deleteById(em, id);

        assertTrue(categoryRepository.findById(em, id).isEmpty());
    }

    @Test
    @DisplayName("findWithFilters() avec tri alphabetique")
    void findWithFilters_shouldReturnSortedByLabel() {
        em.getTransaction().begin();
        em.persist(new Category("Vehicules"));
        em.persist(new Category("Animaux"));
        em.persist(new Category("Immobilier"));
        em.getTransaction().commit();

        List<Category> categories = categoryRepository.findWithFilters(
                em, null, null, null, "label ASC", null);

        assertEquals(3, categories.size());
        assertEquals("Animaux", categories.get(0).getLabel());
        assertEquals("Immobilier", categories.get(1).getLabel());
        assertEquals("Vehicules", categories.get(2).getLabel());
    }

    @Test
    @DisplayName("findOneWithFilters() trouve par label")
    void findOneWithFilters_shouldFindByLabel() {
        em.getTransaction().begin();
        categoryRepository.save(em, new Category("Emploi"));

        Optional<Category> found = categoryRepository.findOneWithFilters(em, Map.of("label", "Emploi"));

        assertTrue(found.isPresent());
        assertEquals("Emploi", found.get().getLabel());
    }
}
