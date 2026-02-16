package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.dto.CategoryDTO;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.model.Category;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CategoryMapperTest {

    @Test
    @DisplayName("toDTO() mappe tous les champs correctement")
    void toDTO_shouldMapAllFields() {
        Category category = new Category("Immobilier");
        category.setId(1L);

        CategoryDTO dto = CategoryMapper.toDTO(category);

        assertEquals(1L, dto.getId());
        assertEquals("Immobilier", dto.getLabel());
    }

    @Test
    @DisplayName("toDTOList() mappe une liste de catégories")
    void toDTOList_shouldMapAllElements() {
        Category c1 = new Category("Immobilier");
        c1.setId(1L);
        Category c2 = new Category("Véhicules");
        c2.setId(2L);

        List<CategoryDTO> dtos = CategoryMapper.toDTOList(List.of(c1, c2));

        assertEquals(2, dtos.size());
        assertEquals("Immobilier", dtos.get(0).getLabel());
        assertEquals("Véhicules", dtos.get(1).getLabel());
    }

    @Test
    @DisplayName("toDTOList() retourne une liste vide pour une entrée vide")
    void toDTOList_shouldReturnEmptyList() {
        List<CategoryDTO> dtos = CategoryMapper.toDTOList(List.of());
        assertTrue(dtos.isEmpty());
    }
}
