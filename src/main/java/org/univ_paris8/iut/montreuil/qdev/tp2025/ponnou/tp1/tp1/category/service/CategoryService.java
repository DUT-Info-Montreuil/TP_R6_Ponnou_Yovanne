package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.service;

import lombok.extern.slf4j.Slf4j;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.repository.AnnonceRepository;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.config.EntityManagerUtil;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.model.Category;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.repository.CategoryRepository;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final AnnonceRepository annonceRepository;

    public CategoryService() {
        this.categoryRepository = new CategoryRepository();
        this.annonceRepository = new AnnonceRepository();
    }

    public CategoryService(CategoryRepository categoryRepository, AnnonceRepository annonceRepository) {
        this.categoryRepository = categoryRepository;
        this.annonceRepository = annonceRepository;
    }

    public Category create(String label) {
        log.info("Creating category label={}", label);
        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            if (categoryRepository.countWithFilters(em, Map.of("label", label)) > 0) {
                log.warn("Category already exists label={}", label);
                throw new IllegalArgumentException("Cette catégorie existe déjà");
            }

            Category category = new Category(label);
            Category saved = categoryRepository.save(em, category);
            tx.commit();
            log.info("Category created id={}", saved.getId());
            return saved;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            if (!(e instanceof IllegalArgumentException)) {
                log.error("Error creating category label={}", label, e);
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public Category update(Long id, String label) {
        log.info("Updating category id={} label={}", id, label);
        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Category category = categoryRepository.findById(em, id)
                    .orElseThrow(() -> {
                        log.warn("Category not found id={}", id);
                        return new IllegalArgumentException("Catégorie non trouvée");
                    });

            if (!category.getLabel().equals(label) && categoryRepository.countWithFilters(em, Map.of("label", label)) > 0) {
                log.warn("Category already exists label={}", label);
                throw new IllegalArgumentException("Cette catégorie existe déjà");
            }

            category.setLabel(label);
            Category updated = categoryRepository.update(em, category);
            tx.commit();
            log.info("Category updated id={}", id);
            return updated;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            if (!(e instanceof IllegalArgumentException)) {
                log.error("Error updating category id={}", id, e);
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public Optional<Category> findById(Long id) {
        log.debug("Fetching category by id={}", id);
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            return categoryRepository.findById(em, id);
        } finally {
            em.close();
        }
    }

    public Optional<Category> findByLabel(String label) {
        log.debug("Fetching category by label={}", label);
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            return categoryRepository.findOneWithFilters(em, Map.of("label", label));
        } finally {
            em.close();
        }
    }

    public List<Category> findAll() {
        log.debug("Listing all categories");
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            return categoryRepository.findWithFilters(em, null, null, null, "label ASC", null);
        } finally {
            em.close();
        }
    }

    public List<Category> findAllPaginated(int page, int size) {
        log.debug("Listing categories page={} size={}", page, size);
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
        log.info("Patching category id={}", id);
        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Category category = categoryRepository.findById(em, id)
                    .orElseThrow(() -> {
                        log.warn("Category not found id={}", id);
                        return new IllegalArgumentException("Catégorie non trouvée");
                    });

            if (label != null) {
                if (!category.getLabel().equals(label) && categoryRepository.countWithFilters(em, Map.of("label", label)) > 0) {
                    log.warn("Category already exists label={}", label);
                    throw new IllegalArgumentException("Cette catégorie existe déjà");
                }
                category.setLabel(label);
            }

            Category updated = categoryRepository.update(em, category);
            tx.commit();
            log.info("Category patched id={}", id);
            return updated;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            if (!(e instanceof IllegalArgumentException)) {
                log.error("Error patching category id={}", id, e);
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public void delete(Long id) {
        log.info("Deleting category id={}", id);
        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            if (annonceRepository.countWithFilters(em, Map.of("category.id", id)) > 0) {
                log.warn("Cannot delete category id={}: still has annonces", id);
                throw new IllegalStateException("Impossible de supprimer une catégorie contenant des annonces");
            }
            tx.begin();
            if (!categoryRepository.deleteById(em, id)) {
                log.warn("Category not found id={}", id);
                throw new IllegalArgumentException("Catégorie non trouvée");
            }
            tx.commit();
            log.info("Category deleted id={}", id);
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            if (!(e instanceof IllegalArgumentException || e instanceof IllegalStateException)) {
                log.error("Error deleting category id={}", id, e);
            }
            throw e;
        } finally {
            em.close();
        }
    }
}
