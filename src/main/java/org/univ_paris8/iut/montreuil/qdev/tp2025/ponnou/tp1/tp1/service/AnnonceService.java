package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.service;

import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.model.Annonce;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.model.AnnonceStatus;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.model.Category;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.model.User;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.utils.EntityManagerUtil;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

public class AnnonceService {

    public Annonce create(String title, String description, String adress, String mail,
                          Long authorId, Long categoryId) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            User author = em.find(User.class, authorId);
            if (author == null) {
                throw new IllegalArgumentException("Utilisateur non trouvé");
            }

            Category category = em.find(Category.class, categoryId);
            if (category == null) {
                throw new IllegalArgumentException("Catégorie non trouvée");
            }

            Annonce annonce = new Annonce(title, description, adress, mail);
            annonce.setAuthor(author);
            annonce.setCategory(category);
            annonce.setStatus(AnnonceStatus.DRAFT);

            em.persist(annonce);
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

    public Annonce update(Long id, String title, String description, String adress, String mail,
                          Long categoryId) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            Annonce annonce = em.find(Annonce.class, id);
            if (annonce == null) {
                throw new IllegalArgumentException("Annonce non trouvée");
            }

            Category category = em.find(Category.class, categoryId);
            if (category == null) {
                throw new IllegalArgumentException("Catégorie non trouvée");
            }

            annonce.setTitle(title);
            annonce.setDescription(description);
            annonce.setAdress(adress);
            annonce.setMail(mail);
            annonce.setCategory(category);

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

    public Annonce publish(Long id) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            Annonce annonce = em.find(Annonce.class, id);
            if (annonce == null) {
                throw new IllegalArgumentException("Annonce non trouvée");
            }

            if (annonce.getStatus() == AnnonceStatus.ARCHIVED) {
                throw new IllegalStateException("Impossible de publier une annonce archivée");
            }

            annonce.setStatus(AnnonceStatus.PUBLISHED);

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

    public Annonce archive(Long id) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            Annonce annonce = em.find(Annonce.class, id);
            if (annonce == null) {
                throw new IllegalArgumentException("Annonce non trouvée");
            }

            annonce.setStatus(AnnonceStatus.ARCHIVED);

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

    public void delete(Long id) {
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

    public List<Annonce> findPublishedPaginated(int page, int size) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            TypedQuery<Annonce> query = em.createQuery(
                    "SELECT a FROM Annonce a " +
                    "LEFT JOIN FETCH a.author " +
                    "LEFT JOIN FETCH a.category " +
                    "WHERE a.status = :status " +
                    "ORDER BY a.date DESC", Annonce.class);
            query.setParameter("status", AnnonceStatus.PUBLISHED);
            query.setFirstResult(page * size);
            query.setMaxResults(size);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Annonce> findByAuthorPaginated(Long authorId, int page, int size) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            TypedQuery<Annonce> query = em.createQuery(
                    "SELECT a FROM Annonce a " +
                    "LEFT JOIN FETCH a.author " +
                    "LEFT JOIN FETCH a.category " +
                    "WHERE a.author.id = :authorId " +
                    "ORDER BY a.date DESC", Annonce.class);
            query.setParameter("authorId", authorId);
            query.setFirstResult(page * size);
            query.setMaxResults(size);
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
                    "WHERE a.status = :status " +
                    "AND (LOWER(a.title) LIKE LOWER(:keyword) " +
                    "OR LOWER(a.description) LIKE LOWER(:keyword)) " +
                    "ORDER BY a.date DESC", Annonce.class);
            query.setParameter("status", AnnonceStatus.PUBLISHED);
            query.setParameter("keyword", "%" + keyword + "%");
            query.setFirstResult(page * size);
            query.setMaxResults(size);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Annonce> findByCategoryAndStatusPaginated(Long categoryId, AnnonceStatus status,
                                                           int page, int size) {
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

    public List<Annonce> findByCategoryPaginated(Long categoryId, int page, int size) {
        return findByCategoryAndStatusPaginated(categoryId, AnnonceStatus.PUBLISHED, page, size);
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

    public long countPublished() {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(a) FROM Annonce a WHERE a.status = :status", Long.class);
            query.setParameter("status", AnnonceStatus.PUBLISHED);
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
                    "WHERE a.status = :status " +
                    "AND (LOWER(a.title) LIKE LOWER(:keyword) " +
                    "OR LOWER(a.description) LIKE LOWER(:keyword))", Long.class);
            query.setParameter("status", AnnonceStatus.PUBLISHED);
            query.setParameter("keyword", "%" + keyword + "%");
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }

    public long countByAuthor(Long authorId) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(a) FROM Annonce a WHERE a.author.id = :authorId", Long.class);
            query.setParameter("authorId", authorId);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }

    public long countByCategory(Long categoryId) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(a) FROM Annonce a " +
                    "WHERE a.category.id = :categoryId AND a.status = :status", Long.class);
            query.setParameter("categoryId", categoryId);
            query.setParameter("status", AnnonceStatus.PUBLISHED);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }
}
