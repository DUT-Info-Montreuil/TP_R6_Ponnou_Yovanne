package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserDTOTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    // ── UserDTO ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("UserDTO_gettersAndSetters_shouldWork")
    void userDTO_gettersAndSetters_shouldWork() {
        Timestamp ts = new Timestamp(1_000_000L);
        UserDTO dto = new UserDTO();
        dto.setId(7L);
        dto.setUsername("yovanne");
        dto.setEmail("yovanne@example.com");
        dto.setRole("ROLE_USER");
        dto.setCreatedAt(ts);

        assertEquals(7L, dto.getId());
        assertEquals("yovanne", dto.getUsername());
        assertEquals("yovanne@example.com", dto.getEmail());
        assertEquals("ROLE_USER", dto.getRole());
        assertEquals(ts, dto.getCreatedAt());
    }

    // ── UserCreateDTO ────────────────────────────────────────────────────────

    @Test
    @DisplayName("UserCreateDTO_valid_shouldHaveNoViolations")
    void userCreateDTO_valid_shouldHaveNoViolations() {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setUsername("alice");
        dto.setEmail("alice@test.com");
        dto.setPassword("password123");

        Set<ConstraintViolation<UserCreateDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
        assertEquals("alice", dto.getUsername());
        assertEquals("alice@test.com", dto.getEmail());
        assertEquals("password123", dto.getPassword());
    }

    @Test
    @DisplayName("UserCreateDTO_blankUsername_shouldFailValidation")
    void userCreateDTO_blankUsername_shouldFailValidation() {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setUsername("");
        dto.setEmail("alice@test.com");
        dto.setPassword("password123");

        Set<ConstraintViolation<UserCreateDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("username")));
    }

    @Test
    @DisplayName("UserCreateDTO_usernameTooShort_shouldFailValidation")
    void userCreateDTO_usernameTooShort_shouldFailValidation() {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setUsername("ab");
        dto.setEmail("alice@test.com");
        dto.setPassword("password123");

        Set<ConstraintViolation<UserCreateDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("username")));
    }

    @Test
    @DisplayName("UserCreateDTO_usernameTooLong_shouldFailValidation")
    void userCreateDTO_usernameTooLong_shouldFailValidation() {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setUsername("a".repeat(51));
        dto.setEmail("alice@test.com");
        dto.setPassword("password123");

        Set<ConstraintViolation<UserCreateDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("UserCreateDTO_invalidEmail_shouldFailValidation")
    void userCreateDTO_invalidEmail_shouldFailValidation() {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setUsername("alice");
        dto.setEmail("not-an-email");
        dto.setPassword("password123");

        Set<ConstraintViolation<UserCreateDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    @DisplayName("UserCreateDTO_blankEmail_shouldFailValidation")
    void userCreateDTO_blankEmail_shouldFailValidation() {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setUsername("alice");
        dto.setEmail("");
        dto.setPassword("password123");

        Set<ConstraintViolation<UserCreateDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("UserCreateDTO_shortPassword_shouldFailValidation")
    void userCreateDTO_shortPassword_shouldFailValidation() {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setUsername("alice");
        dto.setEmail("alice@test.com");
        dto.setPassword("abc");

        Set<ConstraintViolation<UserCreateDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("password")));
    }

    @Test
    @DisplayName("UserCreateDTO_nullFields_shouldFailValidation")
    void userCreateDTO_nullFields_shouldFailValidation() {
        UserCreateDTO dto = new UserCreateDTO();

        Set<ConstraintViolation<UserCreateDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertEquals(3, violations.size());
    }

    // ── UserUpdateDTO ────────────────────────────────────────────────────────

    @Test
    @DisplayName("UserUpdateDTO_valid_shouldHaveNoViolations")
    void userUpdateDTO_valid_shouldHaveNoViolations() {
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setUsername("alice_updated");
        dto.setEmail("alice_updated@test.com");

        Set<ConstraintViolation<UserUpdateDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
        assertEquals("alice_updated", dto.getUsername());
        assertEquals("alice_updated@test.com", dto.getEmail());
    }

    @Test
    @DisplayName("UserUpdateDTO_blankUsername_shouldFailValidation")
    void userUpdateDTO_blankUsername_shouldFailValidation() {
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setUsername("   ");
        dto.setEmail("alice@test.com");

        Set<ConstraintViolation<UserUpdateDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("UserUpdateDTO_invalidEmail_shouldFailValidation")
    void userUpdateDTO_invalidEmail_shouldFailValidation() {
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setUsername("alice");
        dto.setEmail("bad-email");

        Set<ConstraintViolation<UserUpdateDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    @DisplayName("UserUpdateDTO_nullFields_shouldFailValidation")
    void userUpdateDTO_nullFields_shouldFailValidation() {
        UserUpdateDTO dto = new UserUpdateDTO();

        Set<ConstraintViolation<UserUpdateDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertEquals(2, violations.size());
    }

    // ── UserPatchDTO ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("UserPatchDTO_valid_shouldHaveNoViolations")
    void userPatchDTO_valid_shouldHaveNoViolations() {
        UserPatchDTO dto = new UserPatchDTO();
        dto.setUsername("alice_patch");
        dto.setEmail("alice_patch@test.com");
        dto.setPassword("newpassword");

        Set<ConstraintViolation<UserPatchDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
        assertEquals("alice_patch", dto.getUsername());
        assertEquals("alice_patch@test.com", dto.getEmail());
        assertEquals("newpassword", dto.getPassword());
    }

    @Test
    @DisplayName("UserPatchDTO_allNull_shouldBeValid")
    void userPatchDTO_allNull_shouldBeValid() {
        UserPatchDTO dto = new UserPatchDTO();

        Set<ConstraintViolation<UserPatchDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
        assertNull(dto.getUsername());
        assertNull(dto.getEmail());
        assertNull(dto.getPassword());
    }

    @Test
    @DisplayName("UserPatchDTO_usernameTooShort_shouldFailValidation")
    void userPatchDTO_usernameTooShort_shouldFailValidation() {
        UserPatchDTO dto = new UserPatchDTO();
        dto.setUsername("ab");

        Set<ConstraintViolation<UserPatchDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("username")));
    }

    @Test
    @DisplayName("UserPatchDTO_invalidEmail_shouldFailValidation")
    void userPatchDTO_invalidEmail_shouldFailValidation() {
        UserPatchDTO dto = new UserPatchDTO();
        dto.setEmail("not-an-email");

        Set<ConstraintViolation<UserPatchDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    @DisplayName("UserPatchDTO_passwordTooShort_shouldFailValidation")
    void userPatchDTO_passwordTooShort_shouldFailValidation() {
        UserPatchDTO dto = new UserPatchDTO();
        dto.setPassword("abc");

        Set<ConstraintViolation<UserPatchDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("password")));
    }
}
