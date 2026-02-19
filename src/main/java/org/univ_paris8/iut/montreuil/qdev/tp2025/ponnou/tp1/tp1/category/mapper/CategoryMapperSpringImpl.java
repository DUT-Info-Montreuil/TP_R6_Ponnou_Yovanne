package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.mapper;

import org.springframework.stereotype.Component;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.dto.CategoryDTO;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.dto.CategoryPatchDTO;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.model.Category;

import java.util.List;

@Component
public class CategoryMapperSpringImpl implements CategoryMapper {

    @Override
    public CategoryDTO toDTO(Category entity) {
        if (entity == null) {
            return null;
        }
        CategoryDTO dto = new CategoryDTO();
        dto.setId(entity.getId());
        dto.setLabel(entity.getLabel());
        return dto;
    }

    @Override
    public List<CategoryDTO> toDTOList(List<Category> entities) {
        return entities == null ? null : entities.stream().map(this::toDTO).toList();
    }

    @Override
    public void updateCategoryFromPatchDTO(CategoryPatchDTO dto, Category entity) {
        if (dto == null || entity == null) {
            return;
        }
        if (dto.getLabel() != null) {
            entity.setLabel(dto.getLabel());
        }
    }
}
