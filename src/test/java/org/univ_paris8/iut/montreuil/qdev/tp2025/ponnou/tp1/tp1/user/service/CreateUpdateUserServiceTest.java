package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.exception.ResourceNotFoundException;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.model.User;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class CreateUpdateUserServiceTest extends UserServiceTestBase {

    @Test
    @DisplayName("create_shouldHashPasswordAndSave")
    void create_shouldHashPasswordAndSave() {
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.existsByEmail("alice@test.com")).thenReturn(false);
        when(userMapper.toEntityForCreate(any(String.class), any(String.class), any(String.class)))
                .thenAnswer(i -> new User(i.getArgument(0), i.getArgument(1), i.getArgument(2)));
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User user = i.getArgument(0);
            user.setId(1L);
            return user;
        });

        User result = userService.create("alice", "alice@test.com", "password123");

        assertEquals(1L, result.getId());
        assertTrue(passwordEncoder.matches("password123", result.getPassword()));
    }

    @Test
    @DisplayName("create_shouldThrow_whenUsernameExists")
    void create_shouldThrow_whenUsernameExists() {
        when(userRepository.existsByUsername("alice")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> userService.create("alice", "alice@test.com", "password123"));
    }

    @Test
    @DisplayName("create_shouldThrow_whenEmailExists")
    void create_shouldThrow_whenEmailExists() {
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.existsByEmail("alice@test.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> userService.create("alice", "alice@test.com", "password123"));
    }

    @Test
    @DisplayName("update_shouldUpdateFields")
    void update_shouldUpdateFields() {
        User existing = new User("old", "old@test.com", "pwd");
        existing.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByUsername("new")).thenReturn(false);
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = userService.update(1L, "new", "new@test.com");

        assertEquals("new", result.getUsername());
        assertEquals("new@test.com", result.getEmail());
    }

    @Test
    @DisplayName("update_shouldThrow_whenNotFound")
    void update_shouldThrow_whenNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.update(999L, "new", "new@test.com"));
    }

    @Test
    @DisplayName("update_shouldThrow_whenNewUsernameExists")
    void update_shouldThrow_whenNewUsernameExists() {
        User existing = new User("old", "old@test.com", "pwd");
        existing.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByUsername("taken")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> userService.update(1L, "taken", "old@test.com"));
    }
}
