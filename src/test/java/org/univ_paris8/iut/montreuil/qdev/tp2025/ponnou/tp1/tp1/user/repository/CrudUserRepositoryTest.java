package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.model.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CrudUserRepositoryTest extends UserRepositoryTestBase {

    @Test
    @DisplayName("save_shouldPersistUser")
    void save_shouldPersistUser() {
        User user = new User("bob", "bob@test.com", "password123");

        User saved = userRepository.save(user);

        assertTrue(saved.getId() != null);
        assertEquals("bob", saved.getUsername());
    }

    @Test
    @DisplayName("findById_shouldReturnUser")
    void findById_shouldReturnUser() {
        User found = userRepository.findById(existing.getId()).orElseThrow();

        assertEquals(existing.getUsername(), found.getUsername());
    }

    @Test
    @DisplayName("delete_shouldRemoveUser")
    void delete_shouldRemoveUser() {
        userRepository.deleteById(existing.getId());

        assertTrue(userRepository.findById(existing.getId()).isEmpty());
    }
}
