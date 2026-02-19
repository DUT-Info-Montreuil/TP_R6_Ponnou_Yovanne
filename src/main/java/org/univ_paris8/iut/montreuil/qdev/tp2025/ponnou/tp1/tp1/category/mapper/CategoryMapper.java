package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.dto.CategoryDTO;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.dto.CategoryPatchDTO;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.model.Category;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryDTO toDTO(Category entity);

    List<CategoryDTO> toDTOList(List<Category> entities);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "annonces", ignore = true)
    void updateCategoryFromPatchDTO(CategoryPatchDTO dto, @MappingTarget Category entity);
}
