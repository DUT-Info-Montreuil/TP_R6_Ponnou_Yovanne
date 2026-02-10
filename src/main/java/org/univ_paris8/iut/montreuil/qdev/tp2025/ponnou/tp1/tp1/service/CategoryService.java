package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.service;

import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.dao.AnnonceDAO;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.dao.CategoryDAO;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.model.Category;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.utils.EntityManagerUtil;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CategoryService {

    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final AnnonceDAO annonceDAO = new AnnonceDAO();

    public Category create(String label) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            if (categoryDAO.countWithFilters(em, Map.of("label", label)) > 0) {
                throw new IllegalArgumentException("Cette catégorie existe déjà");
            }

            Category category = new Category(label);
            Category saved = categoryDAO.save(em, category);
            tx.commit();
            return saved;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public Category update(Long id, String label) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Category category = categoryDAO.findById(em, id)
                    .orElseThrow(() -> new IllegalArgumentException("Catégorie non trouvée"));

            if (!category.getLabel().equals(label) && categoryDAO.countWithFilters(em, Map.of("label", label)) > 0) {
                throw new IllegalArgumentException("Cette catégorie existe déjà");
            }

            category.setLabel(label);
            Category updated = categoryDAO.update(em, category);
            tx.commit();
            return updated;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public Optional<Category> findById(Long id) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            return categoryDAO.findById(em, id);
        } finally {
            em.close();
        }
    }

    public Optional<Category> findByLabel(String label) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            return categoryDAO.findOneWithFilters(em, Map.of("label", label));
        } finally {
            em.close();
        }
    }

    public List<Category> findAll() {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            return categoryDAO.findWithFilters(em, null, null, null, "label ASC", null);
        } finally {
            em.close();
        }
    }

    public void delete(Long id) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            if (annonceDAO.countWithFilters(em, Map.of("category.id", id)) > 0) {
                throw new IllegalStateException("Impossible de supprimer une catégorie contenant des annonces");
            }
            tx.begin();
            categoryDAO.deleteById(em, id);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}
