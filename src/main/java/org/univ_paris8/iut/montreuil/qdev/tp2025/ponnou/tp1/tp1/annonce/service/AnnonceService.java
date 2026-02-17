package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.service;

import lombok.extern.slf4j.Slf4j;
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

@Slf4j
public class AnnonceService {

    private static final String JOINS = "LEFT JOIN FETCH e.author LEFT JOIN FETCH e.category";
    private static final String ORDER_BY = "date DESC";
    private static final String[] KEYWORD_FIELDS = {"title", "description"};

    private final AnnonceRepository annonceRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public AnnonceService() {
        this.annonceRepository = new AnnonceRepository();
        this.userRepository = new UserRepository();
        this.categoryRepository = new CategoryRepository();
    }

    public AnnonceService(AnnonceRepository annonceRepository, UserRepository userRepository,
                          CategoryRepository categoryRepository) {
        this.annonceRepository = annonceRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    public Annonce create(String title, String description, String adress, String mail,
                          Long authenticatedUserId, Long categoryId) {
        log.info("Creating annonce title={} by userId={}", title, authenticatedUserId);
        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            User author = userRepository.findById(em, authenticatedUserId)
                    .orElseThrow(() -> {
                        log.warn("User not found id={}", authenticatedUserId);
                        return new ResourceNotFoundException("Utilisateur non trouvé");
                    });

            Category category = categoryRepository.findById(em, categoryId)
                    .orElseThrow(() -> {
                        log.warn("Category not found id={}", categoryId);
                        return new ResourceNotFoundException("Catégorie non trouvée");
                    });

            Annonce annonce = new Annonce(title, description, adress, mail);
            annonce.setAuthor(author);
            annonce.setCategory(category);
            annonce.setStatus(AnnonceStatus.DRAFT);

            Annonce saved = annonceRepository.save(em, annonce);
            tx.commit();
            log.info("Annonce created id={}", saved.getId());
            return saved;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            log.error("Error creating annonce title={}", title, e);
            throw e;
        } finally {
            em.close();
        }
    }

    public Annonce update(Long id, Long authenticatedUserId, String title, String description,
                          String adress, String mail, Long categoryId) {
        log.info("Updating annonce id={} by userId={}", id, authenticatedUserId);
        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Annonce annonce = annonceRepository.findOneWithFilters(em, Map.of("id", id), JOINS)
                    .orElseThrow(() -> {
                        log.warn("Annonce not found id={}", id);
                        return new ResourceNotFoundException("Annonce non trouvée");
                    });

            checkOwnership(annonce, authenticatedUserId);
            checkNotPublished(annonce);

            Category category = categoryRepository.findById(em, categoryId)
                    .orElseThrow(() -> {
                        log.warn("Category not found id={}", categoryId);
                        return new ResourceNotFoundException("Catégorie non trouvée");
                    });

            annonce.setTitle(title);
            annonce.setDescription(description);
            annonce.setAdress(adress);
            annonce.setMail(mail);
            annonce.setCategory(category);

            Annonce updated = annonceRepository.update(em, annonce);
            tx.commit();
            log.info("Annonce updated id={}", id);
            return updated;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            if (!(e instanceof ResourceNotFoundException || e instanceof ForbiddenOperationException || e instanceof IllegalStateException)) {
                log.error("Error updating annonce id={}", id, e);
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public Annonce publish(Long id, Long authenticatedUserId) {
        log.info("Publishing annonce id={} by userId={}", id, authenticatedUserId);
        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Annonce annonce = annonceRepository.findOneWithFilters(em, Map.of("id", id), JOINS)
                    .orElseThrow(() -> {
                        log.warn("Annonce not found id={}", id);
                        return new ResourceNotFoundException("Annonce non trouvée");
                    });

            checkOwnership(annonce, authenticatedUserId);

            if (annonce.getStatus() == AnnonceStatus.ARCHIVED) {
                log.warn("Cannot publish archived annonce id={}", id);
                throw new IllegalStateException("Impossible de publier une annonce archivée");
            }

            annonce.setStatus(AnnonceStatus.PUBLISHED);
            Annonce updated = annonceRepository.update(em, annonce);
            tx.commit();
            log.info("Annonce published id={}", id);
            return updated;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            if (!(e instanceof ResourceNotFoundException || e instanceof ForbiddenOperationException || e instanceof IllegalStateException)) {
                log.error("Error publishing annonce id={}", id, e);
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public Annonce archive(Long id, Long authenticatedUserId) {
        log.info("Archiving annonce id={} by userId={}", id, authenticatedUserId);
        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Annonce annonce = annonceRepository.findOneWithFilters(em, Map.of("id", id), JOINS)
                    .orElseThrow(() -> {
                        log.warn("Annonce not found id={}", id);
                        return new ResourceNotFoundException("Annonce non trouvée");
                    });

            checkOwnership(annonce, authenticatedUserId);

            annonce.setStatus(AnnonceStatus.ARCHIVED);
            Annonce updated = annonceRepository.update(em, annonce);
            tx.commit();
            log.info("Annonce archived id={}", id);
            return updated;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            if (!(e instanceof ResourceNotFoundException || e instanceof ForbiddenOperationException)) {
                log.error("Error archiving annonce id={}", id, e);
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public Annonce patch(Long id, Long authenticatedUserId, String title, String description,
                         String adress, String mail, Long categoryId) {
        log.info("Patching annonce id={} by userId={}", id, authenticatedUserId);
        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Annonce annonce = annonceRepository.findOneWithFilters(em, Map.of("id", id), JOINS)
                    .orElseThrow(() -> {
                        log.warn("Annonce not found id={}", id);
                        return new ResourceNotFoundException("Annonce non trouvée");
                    });

            checkOwnership(annonce, authenticatedUserId);
            checkNotPublished(annonce);

            if (title != null) annonce.setTitle(title);
            if (description != null) annonce.setDescription(description);
            if (adress != null) annonce.setAdress(adress);
            if (mail != null) annonce.setMail(mail);
            if (categoryId != null) {
                Category category = categoryRepository.findById(em, categoryId)
                        .orElseThrow(() -> {
                            log.warn("Category not found id={}", categoryId);
                            return new ResourceNotFoundException("Catégorie non trouvée");
                        });
                annonce.setCategory(category);
            }

            Annonce updated = annonceRepository.update(em, annonce);
            tx.commit();
            log.info("Annonce patched id={}", id);
            return updated;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            if (!(e instanceof ResourceNotFoundException || e instanceof ForbiddenOperationException || e instanceof IllegalStateException)) {
                log.error("Error patching annonce id={}", id, e);
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public void delete(Long id, Long authenticatedUserId) {
        log.info("Deleting annonce id={} by userId={}", id, authenticatedUserId);
        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Annonce annonce = annonceRepository.findOneWithFilters(em, Map.of("id", id), JOINS)
                    .orElseThrow(() -> {
                        log.warn("Annonce not found id={}", id);
                        return new ResourceNotFoundException("Annonce non trouvée");
                    });

            checkOwnership(annonce, authenticatedUserId);

            if (annonce.getStatus() != AnnonceStatus.ARCHIVED) {
                log.warn("Cannot delete non-archived annonce id={} status={}", id, annonce.getStatus());
                throw new IllegalStateException(
                        "L'annonce doit être archivée avant d'être supprimée. Statut actuel : " + annonce.getStatus());
            }

            annonceRepository.delete(em, annonce);
            tx.commit();
            log.info("Annonce deleted id={}", id);
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            if (!(e instanceof ResourceNotFoundException || e instanceof ForbiddenOperationException || e instanceof IllegalStateException)) {
                log.error("Error deleting annonce id={}", id, e);
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public Optional<Annonce> findById(Long id) {
        log.debug("Fetching annonce by id={}", id);
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            return annonceRepository.findOneWithFilters(em, Map.of("id", id), JOINS);
        } finally {
            em.close();
        }
    }

    public List<Annonce> findAllPaginated(int page, int size) {
        log.debug("Listing all annonces page={} size={}", page, size);
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            return annonceRepository.findWithFilters(em, null, null, null, ORDER_BY, JOINS, page, size);
        } finally {
            em.close();
        }
    }

    public List<Annonce> findPublishedPaginated(int page, int size) {
        log.debug("Listing published annonces page={} size={}", page, size);
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            return annonceRepository.findWithFilters(em, Map.of("status", AnnonceStatus.PUBLISHED), null, null, ORDER_BY, JOINS, page, size);
        } finally {
            em.close();
        }
    }

    public List<Annonce> findByAuthorPaginated(Long authorId, int page, int size) {
        log.debug("Listing annonces by authorId={} page={} size={}", authorId, page, size);
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            return annonceRepository.findWithFilters(em, Map.of("author.id", authorId), null, null, ORDER_BY, JOINS, page, size);
        } finally {
            em.close();
        }
    }

    public List<Annonce> searchByKeywordPaginated(String keyword, int page, int size) {
        log.debug("Searching annonces keyword={} page={} size={}", keyword, page, size);
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            return annonceRepository.findWithFilters(em, Map.of("status", AnnonceStatus.PUBLISHED), keyword, KEYWORD_FIELDS, ORDER_BY, JOINS, page, size);
        } finally {
            em.close();
        }
    }

    public List<Annonce> findByCategoryAndStatusPaginated(Long categoryId, AnnonceStatus status, int page, int size) {
        log.debug("Listing annonces categoryId={} status={} page={} size={}", categoryId, status, page, size);
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
            log.warn("Ownership check failed: userId={} tried to access annonce id={} owned by userId={}",
                    authenticatedUserId, annonce.getId(), annonce.getAuthor().getId());
            throw new ForbiddenOperationException("Seul l'auteur peut modifier ou supprimer cette annonce");
        }
    }

    private void checkNotPublished(Annonce annonce) {
        if (annonce.getStatus() == AnnonceStatus.PUBLISHED) {
            log.warn("Cannot modify published annonce id={}", annonce.getId());
            throw new IllegalStateException("Impossible de modifier une annonce publiée");
        }
    }
}
