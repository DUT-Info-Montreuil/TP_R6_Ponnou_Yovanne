package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CategoryDTOTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    // ── CategoryDTO ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("CategoryDTO_gettersAndSetters_shouldWork")
    void categoryDTO_gettersAndSetters_shouldWork() {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(1L);
        dto.setLabel("Immobilier");

        assertEquals(1L, dto.getId());
        assertEquals("Immobilier", dto.getLabel());
    }

    // ── CategoryCreateDTO ────────────────────────────────────────────────────

    @Test
    @DisplayName("CategoryCreateDTO_valid_shouldHaveNoViolations")
    void categoryCreateDTO_valid_shouldHaveNoViolations() {
        CategoryCreateDTO dto = new CategoryCreateDTO();
        dto.setLabel("Immobilier");

        Set<ConstraintViolation<CategoryCreateDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
        assertEquals("Immobilier", dto.getLabel());
    }

    @Test
    @DisplayName("CategoryCreateDTO_blankLabel_shouldFailValidation")
    void categoryCreateDTO_blankLabel_shouldFailValidation() {
        CategoryCreateDTO dto = new CategoryCreateDTO();
        dto.setLabel("");

        Set<ConstraintViolation<CategoryCreateDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("label")));
    }

    @Test
    @DisplayName("CategoryCreateDTO_nullLabel_shouldFailValidation")
    void categoryCreateDTO_nullLabel_shouldFailValidation() {
        CategoryCreateDTO dto = new CategoryCreateDTO();

        Set<ConstraintViolation<CategoryCreateDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("CategoryCreateDTO_labelTooLong_shouldFailValidation")
    void categoryCreateDTO_labelTooLong_shouldFailValidation() {
        CategoryCreateDTO dto = new CategoryCreateDTO();
        dto.setLabel("A".repeat(101));

        Set<ConstraintViolation<CategoryCreateDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("label")));
    }

    @Test
    @DisplayName("CategoryCreateDTO_labelMaxLength_shouldBeValid")
    void categoryCreateDTO_labelMaxLength_shouldBeValid() {
        CategoryCreateDTO dto = new CategoryCreateDTO();
        dto.setLabel("A".repeat(100));

        Set<ConstraintViolation<CategoryCreateDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    // ── CategoryUpdateDTO ────────────────────────────────────────────────────

    @Test
    @DisplayName("CategoryUpdateDTO_valid_shouldHaveNoViolations")
    void categoryUpdateDTO_valid_shouldHaveNoViolations() {
        CategoryUpdateDTO dto = new CategoryUpdateDTO();
        dto.setLabel("Electronique");

        Set<ConstraintViolation<CategoryUpdateDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
        assertEquals("Electronique", dto.getLabel());
    }

    @Test
    @DisplayName("CategoryUpdateDTO_blankLabel_shouldFailValidation")
    void categoryUpdateDTO_blankLabel_shouldFailValidation() {
        CategoryUpdateDTO dto = new CategoryUpdateDTO();
        dto.setLabel("   ");

        Set<ConstraintViolation<CategoryUpdateDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("CategoryUpdateDTO_nullLabel_shouldFailValidation")
    void categoryUpdateDTO_nullLabel_shouldFailValidation() {
        CategoryUpdateDTO dto = new CategoryUpdateDTO();

        Set<ConstraintViolation<CategoryUpdateDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("CategoryUpdateDTO_labelTooLong_shouldFailValidation")
    void categoryUpdateDTO_labelTooLong_shouldFailValidation() {
        CategoryUpdateDTO dto = new CategoryUpdateDTO();
        dto.setLabel("B".repeat(101));

        Set<ConstraintViolation<CategoryUpdateDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
    }

    // ── CategoryPatchDTO ─────────────────────────────────────────────────────

    @Test
    @DisplayName("CategoryPatchDTO_valid_shouldHaveNoViolations")
    void categoryPatchDTO_valid_shouldHaveNoViolations() {
        CategoryPatchDTO dto = new CategoryPatchDTO();
        dto.setLabel("Vehicules");

        Set<ConstraintViolation<CategoryPatchDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
        assertEquals("Vehicules", dto.getLabel());
    }

    @Test
    @DisplayName("CategoryPatchDTO_nullLabel_shouldBeValid")
    void categoryPatchDTO_nullLabel_shouldBeValid() {
        CategoryPatchDTO dto = new CategoryPatchDTO();

        Set<ConstraintViolation<CategoryPatchDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
        assertNull(dto.getLabel());
    }

    @Test
    @DisplayName("CategoryPatchDTO_labelTooLong_shouldFailValidation")
    void categoryPatchDTO_labelTooLong_shouldFailValidation() {
        CategoryPatchDTO dto = new CategoryPatchDTO();
        dto.setLabel("C".repeat(101));

        Set<ConstraintViolation<CategoryPatchDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("label")));
    }

    @Test
    @DisplayName("CategoryPatchDTO_labelMaxLength_shouldBeValid")
    void categoryPatchDTO_labelMaxLength_shouldBeValid() {
        CategoryPatchDTO dto = new CategoryPatchDTO();
        dto.setLabel("C".repeat(100));

        Set<ConstraintViolation<CategoryPatchDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }
}
