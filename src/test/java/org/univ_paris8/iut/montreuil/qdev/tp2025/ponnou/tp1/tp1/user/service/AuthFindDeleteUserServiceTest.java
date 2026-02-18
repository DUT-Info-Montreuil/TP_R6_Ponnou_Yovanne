package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.service;

import org.junit.jupiter.api.*;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.config.PasswordUtils;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.model.User;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthFindDeleteUserServiceTest extends UserServiceTestBase {

    @Test
    @DisplayName("authenticate() retourne l'utilisateur avec les bons identifiants")
    void authenticate_shouldReturnUser_whenCredentialsValid() {
        User dave = new User("dave", "dave@test.com", PasswordUtils.hash("secret123"));
        dave.setId(1L);

        when(userRepository.findOneWithFilters(eq(em), eq(Map.of("username", "dave"))))
                .thenReturn(Optional.of(dave));

        Optional<User> result = userService.authenticate("dave", "secret123");

        assertTrue(result.isPresent());
        assertEquals("dave", result.get().getUsername());
    }

    @Test
    @DisplayName("authenticate() retourne vide avec un mauvais mot de passe")
    void authenticate_shouldReturnEmpty_whenWrongPassword() {
        User dave = new User("dave", "dave@test.com", PasswordUtils.hash("secret123"));
        dave.setId(1L);

        when(userRepository.findOneWithFilters(eq(em), eq(Map.of("username", "dave"))))
                .thenReturn(Optional.of(dave));

        Optional<User> result = userService.authenticate("dave", "wrongpassword");

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("authenticate() retourne vide avec un username inconnu")
    void authenticate_shouldReturnEmpty_whenUnknownUsername() {
        when(userRepository.findOneWithFilters(eq(em), eq(Map.of("username", "inconnu"))))
                .thenReturn(Optional.empty());

        Optional<User> result = userService.authenticate("inconnu", "password");

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("findById() retourne l'utilisateur existant")
    void findById_shouldReturnUser() {
        User frank = new User("frank", "frank@test.com", "password123");
        frank.setId(1L);

        when(userRepository.findById(em, 1L)).thenReturn(Optional.of(frank));

        Optional<User> found = userService.findById(1L);

        assertTrue(found.isPresent());
        assertEquals("frank", found.get().getUsername());
    }

    @Test
    @DisplayName("findByUsername() retourne l'utilisateur par nom")
    void findByUsername_shouldReturnUser() {
        User grace = new User("grace", "grace@test.com", "password123");

        when(userRepository.findOneWithFilters(eq(em), eq(Map.of("username", "grace"))))
                .thenReturn(Optional.of(grace));

        Optional<User> found = userService.findByUsername("grace");

        assertTrue(found.isPresent());
        assertEquals("grace@test.com", found.get().getEmail());
    }

    @Test
    @DisplayName("findAll() retourne tous les utilisateurs")
    void findAll_shouldReturnAllUsers() {
        when(userRepository.findWithFilters(eq(em), isNull(), isNull(), isNull(),
                eq("createdAt DESC"), isNull()))
                .thenReturn(List.of(
                        new User("u1", "u1@t.com", "p"),
                        new User("u2", "u2@t.com", "p"),
                        new User("u3", "u3@t.com", "p")));

        List<User> users = userService.findAll();

        assertEquals(3, users.size());
    }

    @Test
    @DisplayName("delete() supprime l'utilisateur")
    void delete_shouldRemoveUser() {
        when(userRepository.deleteById(em, 1L)).thenReturn(true);

        userService.delete(1L);

        verify(userRepository).deleteById(em, 1L);
        verify(tx).commit();
    }
}
