package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.repository;

import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.model.Annonce;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.model.AnnonceStatus;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.model.Category;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.model.User;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.utils.EntityManagerUtil;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

public class AnnonceRepository {

    public Annonce save(Annonce annonce) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            if (annonce.getId() == null) {
                em.persist(annonce);
            } else {
                annonce = em.merge(annonce);
            }
            em.getTransaction().commit();
            return annonce;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public Optional<Annonce> findById(Long id) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            TypedQuery<Annonce> query = em.createQuery(
                    "SELECT a FROM Annonce a " +
                    "LEFT JOIN FETCH a.author " +
                    "LEFT JOIN FETCH a.category " +
                    "WHERE a.id = :id", Annonce.class);
            query.setParameter("id", id);
            List<Annonce> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } finally {
            em.close();
        }
    }

    public List<Annonce> findAll() {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            TypedQuery<Annonce> query = em.createQuery(
                    "SELECT a FROM Annonce a " +
                    "LEFT JOIN FETCH a.author " +
                    "LEFT JOIN FETCH a.category " +
                    "ORDER BY a.date DESC", Annonce.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Annonce> findAllPaginated(int page, int size) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            TypedQuery<Annonce> query = em.createQuery(
                    "SELECT a FROM Annonce a " +
                    "LEFT JOIN FETCH a.author " +
                    "LEFT JOIN FETCH a.category " +
                    "ORDER BY a.date DESC", Annonce.class);
            query.setFirstResult(page * size);
            query.setMaxResults(size);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Annonce> findByStatus(AnnonceStatus status) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            TypedQuery<Annonce> query = em.createQuery(
                    "SELECT a FROM Annonce a " +
                    "LEFT JOIN FETCH a.author " +
                    "LEFT JOIN FETCH a.category " +
                    "WHERE a.status = :status " +
                    "ORDER BY a.date DESC", Annonce.class);
            query.setParameter("status", status);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Annonce> findByStatusPaginated(AnnonceStatus status, int page, int size) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            TypedQuery<Annonce> query = em.createQuery(
                    "SELECT a FROM Annonce a " +
                    "LEFT JOIN FETCH a.author " +
                    "LEFT JOIN FETCH a.category " +
                    "WHERE a.status = :status " +
                    "ORDER BY a.date DESC", Annonce.class);
            query.setParameter("status", status);
            query.setFirstResult(page * size);
            query.setMaxResults(size);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Annonce> findByCategory(Category category) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            TypedQuery<Annonce> query = em.createQuery(
                    "SELECT a FROM Annonce a " +
                    "LEFT JOIN FETCH a.author " +
                    "LEFT JOIN FETCH a.category " +
                    "WHERE a.category = :category " +
                    "ORDER BY a.date DESC", Annonce.class);
            query.setParameter("category", category);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Annonce> findByCategoryId(Long categoryId) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            TypedQuery<Annonce> query = em.createQuery(
                    "SELECT a FROM Annonce a " +
                    "LEFT JOIN FETCH a.author " +
                    "LEFT JOIN FETCH a.category " +
                    "WHERE a.category.id = :categoryId " +
                    "ORDER BY a.date DESC", Annonce.class);
            query.setParameter("categoryId", categoryId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Annonce> findByAuthor(User author) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            TypedQuery<Annonce> query = em.createQuery(
                    "SELECT a FROM Annonce a " +
                    "LEFT JOIN FETCH a.author " +
                    "LEFT JOIN FETCH a.category " +
                    "WHERE a.author = :author " +
                    "ORDER BY a.date DESC", Annonce.class);
            query.setParameter("author", author);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Annonce> findByAuthorId(Long authorId) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            TypedQuery<Annonce> query = em.createQuery(
                    "SELECT a FROM Annonce a " +
                    "LEFT JOIN FETCH a.author " +
                    "LEFT JOIN FETCH a.category " +
                    "WHERE a.author.id = :authorId " +
                    "ORDER BY a.date DESC", Annonce.class);
            query.setParameter("authorId", authorId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Annonce> searchByKeyword(String keyword) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            TypedQuery<Annonce> query = em.createQuery(
                    "SELECT a FROM Annonce a " +
                    "LEFT JOIN FETCH a.author " +
                    "LEFT JOIN FETCH a.category " +
                    "WHERE LOWER(a.title) LIKE LOWER(:keyword) " +
                    "OR LOWER(a.description) LIKE LOWER(:keyword) " +
                    "ORDER BY a.date DESC", Annonce.class);
            query.setParameter("keyword", "%" + keyword + "%");
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Annonce> searchByKeywordPaginated(String keyword, int page, int size) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            TypedQuery<Annonce> query = em.createQuery(
                    "SELECT a FROM Annonce a " +
                    "LEFT JOIN FETCH a.author " +
                    "LEFT JOIN FETCH a.category " +
                    "WHERE LOWER(a.title) LIKE LOWER(:keyword) " +
                    "OR LOWER(a.description) LIKE LOWER(:keyword) " +
                    "ORDER BY a.date DESC", Annonce.class);
            query.setParameter("keyword", "%" + keyword + "%");
            query.setFirstResult(page * size);
            query.setMaxResults(size);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Annonce> findByCategoryAndStatus(Long categoryId, AnnonceStatus status) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            TypedQuery<Annonce> query = em.createQuery(
                    "SELECT a FROM Annonce a " +
                    "LEFT JOIN FETCH a.author " +
                    "LEFT JOIN FETCH a.category " +
                    "WHERE a.category.id = :categoryId AND a.status = :status " +
                    "ORDER BY a.date DESC", Annonce.class);
            query.setParameter("categoryId", categoryId);
            query.setParameter("status", status);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Annonce> findByCategoryAndStatusPaginated(Long categoryId, AnnonceStatus status, int page, int size) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            TypedQuery<Annonce> query = em.createQuery(
                    "SELECT a FROM Annonce a " +
                    "LEFT JOIN FETCH a.author " +
                    "LEFT JOIN FETCH a.category " +
                    "WHERE a.category.id = :categoryId AND a.status = :status " +
                    "ORDER BY a.date DESC", Annonce.class);
            query.setParameter("categoryId", categoryId);
            query.setParameter("status", status);
            query.setFirstResult(page * size);
            query.setMaxResults(size);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public void delete(Annonce annonce) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Annonce managed = em.find(Annonce.class, annonce.getId());
            if (managed != null) {
                em.remove(managed);
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

    public void deleteById(Long id) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Annonce annonce = em.find(Annonce.class, id);
            if (annonce != null) {
                em.remove(annonce);
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

    public long count() {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(a) FROM Annonce a", Long.class);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }

    public long countByStatus(AnnonceStatus status) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(a) FROM Annonce a WHERE a.status = :status", Long.class);
            query.setParameter("status", status);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }

    public long countByCategory(Long categoryId) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(a) FROM Annonce a WHERE a.category.id = :categoryId", Long.class);
            query.setParameter("categoryId", categoryId);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }

    public long countByKeyword(String keyword) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(a) FROM Annonce a " +
                    "WHERE LOWER(a.title) LIKE LOWER(:keyword) " +
                    "OR LOWER(a.description) LIKE LOWER(:keyword)", Long.class);
            query.setParameter("keyword", "%" + keyword + "%");
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }
}
