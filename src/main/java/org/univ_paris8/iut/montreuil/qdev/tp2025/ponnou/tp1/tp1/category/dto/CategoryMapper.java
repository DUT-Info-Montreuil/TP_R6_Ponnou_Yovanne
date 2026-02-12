package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.dto;

import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.model.Category;

import java.util.List;
import java.util.stream.Collectors;

public final class CategoryMapper {

    private CategoryMapper() {
    }

    public static CategoryDTO toDTO(Category entity) {
        return CategoryDTO.builder()
                .id(entity.getId())
                .label(entity.getLabel())
                .build();
    }

    public static List<CategoryDTO> toDTOList(List<Category> entities) {
        return entities.stream()
                .map(CategoryMapper::toDTO)
                .collect(Collectors.toList());
    }
}
