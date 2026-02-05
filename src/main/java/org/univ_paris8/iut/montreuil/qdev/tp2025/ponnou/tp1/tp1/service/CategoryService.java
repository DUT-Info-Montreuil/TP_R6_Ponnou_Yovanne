package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.service;

import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.model.Category;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.utils.EntityManagerUtil;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

public class CategoryService {

    public Category create(String label) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            if (existsByLabel(em, label)) {
                throw new IllegalArgumentException("Cette catégorie existe déjà");
            }

            Category category = new Category(label);
            em.persist(category);
            em.getTransaction().commit();
            return category;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public Category update(Long id, String label) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            Category category = em.find(Category.class, id);
            if (category == null) {
                throw new IllegalArgumentException("Catégorie non trouvée");
            }

            if (!category.getLabel().equals(label) && existsByLabel(em, label)) {
                throw new IllegalArgumentException("Cette catégorie existe déjà");
            }

            category.setLabel(label);

            em.getTransaction().commit();
            return category;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public Optional<Category> findById(Long id) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            return Optional.ofNullable(em.find(Category.class, id));
        } finally {
            em.close();
        }
    }

    public Optional<Category> findByLabel(String label) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            TypedQuery<Category> query = em.createQuery(
                    "SELECT c FROM Category c WHERE c.label = :label", Category.class);
            query.setParameter("label", label);
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }

    public List<Category> findAll() {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            TypedQuery<Category> query = em.createQuery(
                    "SELECT c FROM Category c ORDER BY c.label ASC", Category.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public void delete(Long id) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            Category category = em.find(Category.class, id);
            if (category != null) {
                TypedQuery<Long> countQuery = em.createQuery(
                        "SELECT COUNT(a) FROM Annonce a WHERE a.category.id = :categoryId", Long.class);
                countQuery.setParameter("categoryId", id);
                if (countQuery.getSingleResult() > 0) {
                    throw new IllegalStateException("Impossible de supprimer une catégorie contenant des annonces");
                }
                em.remove(category);
            }

            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    private boolean existsByLabel(EntityManager em, String label) {
        TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(c) FROM Category c WHERE c.label = :label", Long.class);
        query.setParameter("label", label);
        return query.getSingleResult() > 0;
    }
}
