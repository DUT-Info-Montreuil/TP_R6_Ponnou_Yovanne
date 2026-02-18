package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.service;

import org.junit.jupiter.api.*;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.config.EntityManagerUtil;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.model.User;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CreateUpdateUserServiceTest extends UserServiceTestBase {

    @Test
    @DisplayName("create() crée un utilisateur avec succès")
    void create_shouldCreateUser() {
        when(userRepository.countWithFilters(eq(em), eq(Map.of("username", "alice")))).thenReturn(0L);
        when(userRepository.countWithFilters(eq(em), eq(Map.of("email", "alice@test.com")))).thenReturn(0L);
        when(userRepository.save(eq(em), any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(1);
            u.setId(1L);
            return u;
        });

        User user = userService.create("alice", "alice@test.com", "password123");

        assertNotNull(user.getId());
        assertEquals("alice", user.getUsername());
        assertEquals("alice@test.com", user.getEmail());
        verify(tx).commit();
    }

    @Test
    @DisplayName("create() échoue si le username existe déjà")
    void create_shouldFail_whenDuplicateUsername() {
        when(userRepository.countWithFilters(eq(em), eq(Map.of("username", "alice")))).thenReturn(1L);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.create("alice", "other@test.com", "password123"));
        assertTrue(ex.getMessage().contains("nom d'utilisateur existe"));
    }

    @Test
    @DisplayName("create() échoue si l'email existe déjà")
    void create_shouldFail_whenDuplicateEmail() {
        when(userRepository.countWithFilters(eq(em), eq(Map.of("username", "bob")))).thenReturn(0L);
        when(userRepository.countWithFilters(eq(em), eq(Map.of("email", "alice@test.com")))).thenReturn(1L);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.create("bob", "alice@test.com", "password123"));
        assertTrue(ex.getMessage().contains("email existe"));
    }

    @Test
    @DisplayName("update() met à jour username et email")
    void update_shouldModifyUser() {
        User existing = new User("charlie", "charlie@test.com", "password123");
        existing.setId(1L);

        when(userRepository.findById(em, 1L)).thenReturn(Optional.of(existing));
        when(userRepository.countWithFilters(eq(em), eq(Map.of("username", "charlie_new")))).thenReturn(0L);
        when(userRepository.countWithFilters(eq(em), eq(Map.of("email", "charlie_new@test.com")))).thenReturn(0L);
        when(userRepository.update(em, existing)).thenReturn(existing);

        User updated = userService.update(1L, "charlie_new", "charlie_new@test.com");

        assertEquals("charlie_new", updated.getUsername());
        assertEquals("charlie_new@test.com", updated.getEmail());
    }

    @Test
    @DisplayName("update() échoue si le nouveau username est déjà pris")
    void update_shouldFail_whenUsernameAlreadyTaken() {
        User bob = new User("bob", "bob@test.com", "password123");
        bob.setId(2L);

        when(userRepository.findById(em, 2L)).thenReturn(Optional.of(bob));
        when(userRepository.countWithFilters(eq(em), eq(Map.of("username", "alice")))).thenReturn(1L);

        assertThrows(IllegalArgumentException.class,
                () -> userService.update(2L, "alice", "bob@test.com"));
    }

    @Test
    @DisplayName("update() échoue pour un ID inexistant")
    void update_shouldFail_whenUserNotFound() {
        when(userRepository.findById(em, 999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> userService.update(999L, "ghost", "ghost@test.com"));
    }

    @Test
    @DisplayName("changePassword() modifie le mot de passe")
    void changePassword_shouldUpdatePassword() {
        User eve = new User("eve", "eve@test.com", "oldpass123");
        eve.setId(1L);

        when(userRepository.findById(em, 1L)).thenReturn(Optional.of(eve));
        when(userRepository.update(em, eve)).thenReturn(eve);

        userService.changePassword(1L, "newpass456");

        assertEquals("newpass456", eve.getPassword());
        verify(userRepository).update(em, eve);
        verify(tx).commit();
    }

    @Test
    @DisplayName("Chaque opération du service appelle EntityManagerUtil.getEntityManager()")
    void service_shouldCallEntityManagerUtil() {
        when(userRepository.countWithFilters(eq(em), any())).thenReturn(0L);
        when(userRepository.save(eq(em), any(User.class))).thenAnswer(inv -> inv.getArgument(1));

        userService.create("verify", "verify@test.com", "password123");

        mockedUtil.verify(EntityManagerUtil::getEntityManager, atLeastOnce());
    }
}
