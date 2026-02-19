package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.dto.AnnoncePatchDTO;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.mappers.AnnonceMapper;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.model.Annonce;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.model.AnnonceStatus;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.repository.AnnonceRepository;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.repository.AnnonceSpecifications;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.model.Category;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.repository.CategoryRepository;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.exception.ForbiddenOperationException;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.exception.ResourceNotFoundException;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.model.User;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.repository.UserRepository;

import java.sql.Timestamp;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnnonceService {

    private final AnnonceRepository annonceRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final AnnonceMapper annonceMapper;

    @Transactional
    public Annonce create(String title, String description, String adress, String mail,
                          Long authenticatedUserId, Long categoryId) {
        log.info("Creating annonce title={} by userId={}", title, authenticatedUserId);

        User author = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouve"));

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Categorie non trouvee"));

        Annonce annonce = new Annonce(title, description, adress, mail);
        annonce.setAuthor(author);
        annonce.setCategory(category);
        annonce.setStatus(AnnonceStatus.DRAFT);

        Annonce saved = annonceRepository.save(annonce);
        log.info("Annonce created id={}", saved.getId());
        return saved;
    }

    @Transactional
    public Annonce update(Long id, Long authenticatedUserId, String title, String description,
                          String adress, String mail, Long categoryId) {
        log.info("Updating annonce id={} by userId={}", id, authenticatedUserId);

        Annonce annonce = annonceRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new ResourceNotFoundException("Annonce non trouvee"));

        checkOwnership(annonce, authenticatedUserId);
        checkNotPublished(annonce);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Categorie non trouvee"));

        annonce.setTitle(title);
        annonce.setDescription(description);
        annonce.setAdress(adress);
        annonce.setMail(mail);
        annonce.setCategory(category);

        Annonce updated = annonceRepository.save(annonce);
        log.info("Annonce updated id={}", id);
        return updated;
    }

    @Transactional
    public Annonce publish(Long id, Long authenticatedUserId) {
        log.info("Publishing annonce id={} by userId={}", id, authenticatedUserId);

        Annonce annonce = annonceRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new ResourceNotFoundException("Annonce non trouvee"));

        checkOwnership(annonce, authenticatedUserId);

        if (annonce.getStatus() == AnnonceStatus.ARCHIVED) {
            throw new IllegalStateException("Impossible de publier une annonce archivee");
        }

        annonce.setStatus(AnnonceStatus.PUBLISHED);
        Annonce updated = annonceRepository.save(annonce);
        log.info("Annonce published id={}", id);
        return updated;
    }

    @Transactional
    public Annonce archive(Long id, Long authenticatedUserId) {
        log.info("Archiving annonce id={} by userId={}", id, authenticatedUserId);

        Annonce annonce = annonceRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new ResourceNotFoundException("Annonce non trouvee"));

        checkOwnership(annonce, authenticatedUserId);

        annonce.setStatus(AnnonceStatus.ARCHIVED);
        Annonce updated = annonceRepository.save(annonce);
        log.info("Annonce archived id={}", id);
        return updated;
    }

    @Transactional
    public Annonce patch(Long id, Long authenticatedUserId, AnnoncePatchDTO dto) {
        log.info("Patching annonce id={} by userId={}", id, authenticatedUserId);

        Annonce annonce = annonceRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new ResourceNotFoundException("Annonce non trouvee"));

        checkOwnership(annonce, authenticatedUserId);
        checkNotPublished(annonce);

        annonceMapper.updateAnnonceFromPatchDTO(dto, annonce);
        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categorie non trouvee"));
            annonce.setCategory(category);
        }

        Annonce updated = annonceRepository.save(annonce);
        log.info("Annonce patched id={}", id);
        return updated;
    }

    @Transactional
    public void delete(Long id, Long authenticatedUserId) {
        log.info("Deleting annonce id={} by userId={}", id, authenticatedUserId);

        Annonce annonce = annonceRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new ResourceNotFoundException("Annonce non trouvee"));

        checkOwnership(annonce, authenticatedUserId);

        if (annonce.getStatus() != AnnonceStatus.ARCHIVED) {
            throw new IllegalStateException(
                    "L'annonce doit etre archivee avant d'etre supprimee. Statut actuel : " + annonce.getStatus());
        }

        annonceRepository.delete(annonce);
        log.info("Annonce deleted id={}", id);
    }

    @Transactional(readOnly = true)
    public Optional<Annonce> findById(Long id) {
        return annonceRepository.findByIdWithRelations(id);
    }

    @Transactional(readOnly = true)
    public Page<Annonce> findAllPaginated(Pageable pageable) {
        return annonceRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Annonce> findPublishedPaginated(Pageable pageable) {
        return annonceRepository.findByStatusWithRelations(AnnonceStatus.PUBLISHED, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Annonce> findByAuthorPaginated(Long authorId, Pageable pageable) {
        return annonceRepository.findByAuthorIdWithRelations(authorId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Annonce> searchByKeywordPaginated(String keyword, Pageable pageable) {
        return annonceRepository.searchByKeyword(keyword, AnnonceStatus.PUBLISHED, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Annonce> findByCategoryPaginated(Long categoryId, Pageable pageable) {
        return annonceRepository.findByCategoryIdAndStatus(categoryId, AnnonceStatus.PUBLISHED, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Annonce> searchWithFilters(String keyword,
                                           Long categoryId,
                                           Long authorId,
                                           AnnonceStatus status,
                                           Timestamp fromDate,
                                           Timestamp toDate,
                                           Pageable pageable) {
        Specification<Annonce> specification = Specification.allOf(
                AnnonceSpecifications.hasKeyword(keyword),
                AnnonceSpecifications.hasCategoryId(categoryId),
                AnnonceSpecifications.hasAuthorId(authorId),
                AnnonceSpecifications.hasStatus(status),
                AnnonceSpecifications.dateAfter(fromDate),
                AnnonceSpecifications.dateBefore(toDate)
        );
        return annonceRepository.findAll(specification, pageable);
    }

    private void checkOwnership(Annonce annonce, Long authenticatedUserId) {
        if (!annonce.getAuthor().getId().equals(authenticatedUserId)) {
            throw new ForbiddenOperationException("Seul l'auteur peut modifier ou supprimer cette annonce");
        }
    }

    private void checkNotPublished(Annonce annonce) {
        if (annonce.getStatus() == AnnonceStatus.PUBLISHED) {
            throw new IllegalStateException("Impossible de modifier une annonce publiee");
        }
    }
}
