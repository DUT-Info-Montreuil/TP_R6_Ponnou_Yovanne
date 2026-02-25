package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.model.AnnonceStatus;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.repository.AnnonceRepository;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.dto.CategoryPatchDTO;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.mapper.CategoryMapper;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.model.Category;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.repository.CategoryRepository;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.exception.ResourceNotFoundException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final AnnonceRepository annonceRepository;
    private final CategoryMapper categoryMapper;

    @Transactional
    public Category create(String label) {
        if (categoryRepository.existsByLabel(label)) {
            throw new IllegalArgumentException("Cette categorie existe deja");
        }

        Category category = categoryMapper.toEntityForCreate(label);
        return categoryRepository.save(category);
    }

    @Transactional
    public Category update(Long id, String label) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categorie non trouvee"));

        if (!category.getLabel().equals(label) && categoryRepository.existsByLabel(label)) {
            throw new IllegalArgumentException("Cette categorie existe deja");
        }

        category.setLabel(label);
        return categoryRepository.save(category);
    }

    @Transactional
    public Category patch(Long id, CategoryPatchDTO dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categorie non trouvee"));

        if (dto.getLabel() != null && !category.getLabel().equals(dto.getLabel())
                && categoryRepository.existsByLabel(dto.getLabel())) {
            throw new IllegalArgumentException("Cette categorie existe deja");
        }

        categoryMapper.updateCategoryFromPatchDTO(dto, category);
        return categoryRepository.save(category);
    }

    @Transactional
    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Categorie non trouvee");
        }

        if (annonceRepository.countByCategoryIdAndStatus(id, AnnonceStatus.PUBLISHED) > 0
                || annonceRepository.countByCategoryIdAndStatus(id, AnnonceStatus.DRAFT) > 0) {
            throw new IllegalStateException("Impossible de supprimer une categorie contenant des annonces actives");
        }

        categoryRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Category> findById(Long id) {
        return categoryRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<Category> findAllPaginated(Pageable pageable) {
        return categoryRepository.findAll(pageable);
    }
}
