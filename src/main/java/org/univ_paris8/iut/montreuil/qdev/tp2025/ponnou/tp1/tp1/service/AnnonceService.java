package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.service;

import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.dao.AnnonceDAO;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.dao.CategoryDAO;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.dao.UserDAO;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.model.Annonce;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.model.AnnonceStatus;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.model.Category;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.model.User;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.utils.EntityManagerUtil;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AnnonceService {

    private static final String JOINS = "LEFT JOIN FETCH e.author LEFT JOIN FETCH e.category";
    private static final String ORDER_BY = "date DESC";
    private static final String[] KEYWORD_FIELDS = {"title", "description"};

    private final AnnonceDAO annonceDAO = new AnnonceDAO();
    private final UserDAO userDAO = new UserDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    public Annonce create(String title, String description, String adress, String mail,
                          Long authorId, Long categoryId) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            User author = userDAO.findById(em, authorId)
                    .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));

            Category category = categoryDAO.findById(em, categoryId)
                    .orElseThrow(() -> new IllegalArgumentException("Catégorie non trouvée"));

            Annonce annonce = new Annonce(title, description, adress, mail);
            annonce.setAuthor(author);
            annonce.setCategory(category);
            annonce.setStatus(AnnonceStatus.DRAFT);

            Annonce saved = annonceDAO.save(em, annonce);
            tx.commit();
            return saved;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public Annonce update(Long id, String title, String description, String adress, String mail,
                          Long categoryId) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Annonce annonce = annonceDAO.findOneWithFilters(em, Map.of("id", id), JOINS)
                    .orElseThrow(() -> new IllegalArgumentException("Annonce non trouvée"));

            Category category = categoryDAO.findById(em, categoryId)
                    .orElseThrow(() -> new IllegalArgumentException("Catégorie non trouvée"));

            annonce.setTitle(title);
            annonce.setDescription(description);
            annonce.setAdress(adress);
            annonce.setMail(mail);
            annonce.setCategory(category);

            Annonce updated = annonceDAO.update(em, annonce);
            tx.commit();
            return updated;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public Annonce publish(Long id) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Annonce annonce = annonceDAO.findOneWithFilters(em, Map.of("id", id), JOINS)
                    .orElseThrow(() -> new IllegalArgumentException("Annonce non trouvée"));

            if (annonce.getStatus() == AnnonceStatus.ARCHIVED) {
                throw new IllegalStateException("Impossible de publier une annonce archivée");
            }

            annonce.setStatus(AnnonceStatus.PUBLISHED);
            Annonce updated = annonceDAO.update(em, annonce);
            tx.commit();
            return updated;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public Annonce archive(Long id) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Annonce annonce = annonceDAO.findOneWithFilters(em, Map.of("id", id), JOINS)
                    .orElseThrow(() -> new IllegalArgumentException("Annonce non trouvée"));

            annonce.setStatus(AnnonceStatus.ARCHIVED);
            Annonce updated = annonceDAO.update(em, annonce);
            tx.commit();
            return updated;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Mise a jour partielle : seuls les champs non-null sont appliques.
     */
    public Annonce patch(Long id, String title, String description, String adress, String mail,
                         Long categoryId) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Annonce annonce = annonceDAO.findOneWithFilters(em, Map.of("id", id), JOINS)
                    .orElseThrow(() -> new IllegalArgumentException("Annonce non trouvée"));

            if (title != null) annonce.setTitle(title);
            if (description != null) annonce.setDescription(description);
            if (adress != null) annonce.setAdress(adress);
            if (mail != null) annonce.setMail(mail);
            if (categoryId != null) {
                Category category = categoryDAO.findById(em, categoryId)
                        .orElseThrow(() -> new IllegalArgumentException("Catégorie non trouvée"));
                annonce.setCategory(category);
            }

            Annonce updated = annonceDAO.update(em, annonce);
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
            tx.begin();
            if (!annonceDAO.deleteById(em, id)) {
                throw new IllegalArgumentException("Annonce non trouvée");
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public Optional<Annonce> findById(Long id) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            return annonceDAO.findOneWithFilters(em, Map.of("id", id), JOINS);
        } finally {
            em.close();
        }
    }

    public List<Annonce> findAllPaginated(int page, int size) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            return annonceDAO.findWithFilters(em, null, null, null, ORDER_BY, JOINS, page, size);
        } finally {
            em.close();
        }
    }

    public List<Annonce> findPublishedPaginated(int page, int size) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            return annonceDAO.findWithFilters(em, Map.of("status", AnnonceStatus.PUBLISHED), null, null, ORDER_BY, JOINS, page, size);
        } finally {
            em.close();
        }
    }

    public List<Annonce> findByAuthorPaginated(Long authorId, int page, int size) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            return annonceDAO.findWithFilters(em, Map.of("author.id", authorId), null, null, ORDER_BY, JOINS, page, size);
        } finally {
            em.close();
        }
    }

    public List<Annonce> searchByKeywordPaginated(String keyword, int page, int size) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            return annonceDAO.findWithFilters(em, Map.of("status", AnnonceStatus.PUBLISHED), keyword, KEYWORD_FIELDS, ORDER_BY, JOINS, page, size);
        } finally {
            em.close();
        }
    }

    public List<Annonce> findByCategoryAndStatusPaginated(Long categoryId, AnnonceStatus status, int page, int size) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            Map<String, Object> filters = new HashMap<>();
            filters.put("category.id", categoryId);
            filters.put("status", status);
            return annonceDAO.findWithFilters(em, filters, null, null, ORDER_BY, JOINS, page, size);
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
            return annonceDAO.count(em);
        } finally {
            em.close();
        }
    }

    public long countPublished() {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            return annonceDAO.countWithFilters(em, Map.of("status", AnnonceStatus.PUBLISHED));
        } finally {
            em.close();
        }
    }

    public long countByKeyword(String keyword) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            return annonceDAO.countWithFilters(em, Map.of("status", AnnonceStatus.PUBLISHED), keyword, KEYWORD_FIELDS);
        } finally {
            em.close();
        }
    }

    public long countByAuthor(Long authorId) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            return annonceDAO.countWithFilters(em, Map.of("author.id", authorId));
        } finally {
            em.close();
        }
    }

    public long countByCategory(Long categoryId) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            Map<String, Object> filters = new HashMap<>();
            filters.put("category.id", categoryId);
            filters.put("status", AnnonceStatus.PUBLISHED);
            return annonceDAO.countWithFilters(em, filters);
        } finally {
            em.close();
        }
    }
}
