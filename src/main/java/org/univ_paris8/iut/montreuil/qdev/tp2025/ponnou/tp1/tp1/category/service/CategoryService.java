package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.service;

import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.repository.AnnonceRepository;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.config.EntityManagerUtil;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.model.Category;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.repository.CategoryRepository;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CategoryService {

    private final CategoryRepository categoryRepository = new CategoryRepository();
    private final AnnonceRepository annonceRepository = new AnnonceRepository();

    public Category create(String label) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            if (categoryRepository.countWithFilters(em, Map.of("label", label)) > 0) {
                throw new IllegalArgumentException("Cette catégorie existe déjà");
            }

            Category category = new Category(label);
            Category saved = categoryRepository.save(em, category);
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

            Category category = categoryRepository.findById(em, id)
                    .orElseThrow(() -> new IllegalArgumentException("Catégorie non trouvée"));

            if (!category.getLabel().equals(label) && categoryRepository.countWithFilters(em, Map.of("label", label)) > 0) {
                throw new IllegalArgumentException("Cette catégorie existe déjà");
            }

            category.setLabel(label);
            Category updated = categoryRepository.update(em, category);
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
            return categoryRepository.findById(em, id);
        } finally {
            em.close();
        }
    }

    public Optional<Category> findByLabel(String label) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            return categoryRepository.findOneWithFilters(em, Map.of("label", label));
        } finally {
            em.close();
        }
    }

    public List<Category> findAll() {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            return categoryRepository.findWithFilters(em, null, null, null, "label ASC", null);
        } finally {
            em.close();
        }
    }

    public List<Category> findAllPaginated(int page, int size) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            return categoryRepository.findWithFilters(em, null, null, null, "label ASC", null, page, size);
        } finally {
            em.close();
        }
    }

    public long count() {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            return categoryRepository.count(em);
        } finally {
            em.close();
        }
    }

    public Category patch(Long id, String label) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Category category = categoryRepository.findById(em, id)
                    .orElseThrow(() -> new IllegalArgumentException("Catégorie non trouvée"));

            if (label != null) {
                if (!category.getLabel().equals(label) && categoryRepository.countWithFilters(em, Map.of("label", label)) > 0) {
                    throw new IllegalArgumentException("Cette catégorie existe déjà");
                }
                category.setLabel(label);
            }

            Category updated = categoryRepository.update(em, category);
            tx.commit();
            return updated;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public void delete(Long id) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            if (annonceRepository.countWithFilters(em, Map.of("category.id", id)) > 0) {
                throw new IllegalStateException("Impossible de supprimer une catégorie contenant des annonces");
            }
            tx.begin();
            if (!categoryRepository.deleteById(em, id)) {
                throw new IllegalArgumentException("Catégorie non trouvée");
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}
