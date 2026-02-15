package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.service;

import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.model.Annonce;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.model.AnnonceStatus;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.repository.AnnonceRepository;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.model.Category;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.repository.CategoryRepository;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.exception.ForbiddenOperationException;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.exception.ResourceNotFoundException;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.model.User;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.repository.UserRepository;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.config.EntityManagerUtil;

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

    private final AnnonceRepository annonceRepository = new AnnonceRepository();
    private final UserRepository userRepository = new UserRepository();
    private final CategoryRepository categoryRepository = new CategoryRepository();

    public Annonce create(String title, String description, String adress, String mail,
                          Long authenticatedUserId, Long categoryId) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            User author = userRepository.findById(em, authenticatedUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

            Category category = categoryRepository.findById(em, categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Catégorie non trouvée"));

            Annonce annonce = new Annonce(title, description, adress, mail);
            annonce.setAuthor(author);
            annonce.setCategory(category);
            annonce.setStatus(AnnonceStatus.DRAFT);

            Annonce saved = annonceRepository.save(em, annonce);
            tx.commit();
            return saved;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public Annonce update(Long id, Long authenticatedUserId, String title, String description,
                          String adress, String mail, Long categoryId) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Annonce annonce = annonceRepository.findOneWithFilters(em, Map.of("id", id), JOINS)
                    .orElseThrow(() -> new ResourceNotFoundException("Annonce non trouvée"));

            checkOwnership(annonce, authenticatedUserId);
            checkNotPublished(annonce);

            Category category = categoryRepository.findById(em, categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Catégorie non trouvée"));

            annonce.setTitle(title);
            annonce.setDescription(description);
            annonce.setAdress(adress);
            annonce.setMail(mail);
            annonce.setCategory(category);

            Annonce updated = annonceRepository.update(em, annonce);
            tx.commit();
            return updated;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public Annonce publish(Long id, Long authenticatedUserId) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Annonce annonce = annonceRepository.findOneWithFilters(em, Map.of("id", id), JOINS)
                    .orElseThrow(() -> new ResourceNotFoundException("Annonce non trouvée"));

            checkOwnership(annonce, authenticatedUserId);

            if (annonce.getStatus() == AnnonceStatus.ARCHIVED) {
                throw new IllegalStateException("Impossible de publier une annonce archivée");
            }

            annonce.setStatus(AnnonceStatus.PUBLISHED);
            Annonce updated = annonceRepository.update(em, annonce);
            tx.commit();
            return updated;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public Annonce archive(Long id, Long authenticatedUserId) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Annonce annonce = annonceRepository.findOneWithFilters(em, Map.of("id", id), JOINS)
                    .orElseThrow(() -> new ResourceNotFoundException("Annonce non trouvée"));

            checkOwnership(annonce, authenticatedUserId);

            annonce.setStatus(AnnonceStatus.ARCHIVED);
            Annonce updated = annonceRepository.update(em, annonce);
            tx.commit();
            return updated;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public Annonce patch(Long id, Long authenticatedUserId, String title, String description,
                         String adress, String mail, Long categoryId) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Annonce annonce = annonceRepository.findOneWithFilters(em, Map.of("id", id), JOINS)
                    .orElseThrow(() -> new ResourceNotFoundException("Annonce non trouvée"));

            checkOwnership(annonce, authenticatedUserId);
            checkNotPublished(annonce);

            if (title != null) annonce.setTitle(title);
            if (description != null) annonce.setDescription(description);
            if (adress != null) annonce.setAdress(adress);
            if (mail != null) annonce.setMail(mail);
            if (categoryId != null) {
                Category category = categoryRepository.findById(em, categoryId)
                        .orElseThrow(() -> new ResourceNotFoundException("Catégorie non trouvée"));
                annonce.setCategory(category);
            }

            Annonce updated = annonceRepository.update(em, annonce);
            tx.commit();
            return updated;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public void delete(Long id, Long authenticatedUserId) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Annonce annonce = annonceRepository.findOneWithFilters(em, Map.of("id", id), JOINS)
                    .orElseThrow(() -> new ResourceNotFoundException("Annonce non trouvée"));

            checkOwnership(annonce, authenticatedUserId);

            if (annonce.getStatus() != AnnonceStatus.ARCHIVED) {
                throw new IllegalStateException(
                        "L'annonce doit être archivée avant d'être supprimée. Statut actuel : " + annonce.getStatus());
            }

            annonceRepository.delete(em, annonce);
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
            return annonceRepository.findOneWithFilters(em, Map.of("id", id), JOINS);
        } finally {
            em.close();
        }
    }

    public List<Annonce> findAllPaginated(int page, int size) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            return annonceRepository.findWithFilters(em, null, null, null, ORDER_BY, JOINS, page, size);
        } finally {
            em.close();
        }
    }

    public List<Annonce> findPublishedPaginated(int page, int size) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            return annonceRepository.findWithFilters(em, Map.of("status", AnnonceStatus.PUBLISHED), null, null, ORDER_BY, JOINS, page, size);
        } finally {
            em.close();
        }
    }

    public List<Annonce> findByAuthorPaginated(Long authorId, int page, int size) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            return annonceRepository.findWithFilters(em, Map.of("author.id", authorId), null, null, ORDER_BY, JOINS, page, size);
        } finally {
            em.close();
        }
    }

    public List<Annonce> searchByKeywordPaginated(String keyword, int page, int size) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            return annonceRepository.findWithFilters(em, Map.of("status", AnnonceStatus.PUBLISHED), keyword, KEYWORD_FIELDS, ORDER_BY, JOINS, page, size);
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
            return annonceRepository.findWithFilters(em, filters, null, null, ORDER_BY, JOINS, page, size);
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
            return annonceRepository.count(em);
        } finally {
            em.close();
        }
    }

    public long countPublished() {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            return annonceRepository.countWithFilters(em, Map.of("status", AnnonceStatus.PUBLISHED));
        } finally {
            em.close();
        }
    }

    public long countByKeyword(String keyword) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            return annonceRepository.countWithFilters(em, Map.of("status", AnnonceStatus.PUBLISHED), keyword, KEYWORD_FIELDS);
        } finally {
            em.close();
        }
    }

    public long countByAuthor(Long authorId) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            return annonceRepository.countWithFilters(em, Map.of("author.id", authorId));
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
            return annonceRepository.countWithFilters(em, filters);
        } finally {
            em.close();
        }
    }

    private void checkOwnership(Annonce annonce, Long authenticatedUserId) {
        if (!annonce.getAuthor().getId().equals(authenticatedUserId)) {
            throw new ForbiddenOperationException("Seul l'auteur peut modifier ou supprimer cette annonce");
        }
    }

    private void checkNotPublished(Annonce annonce) {
        if (annonce.getStatus() == AnnonceStatus.PUBLISHED) {
            throw new IllegalStateException("Impossible de modifier une annonce publiée");
        }
    }
}
