package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.repository;

import org.junit.jupiter.api.*;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.model.User;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CrudUserRepositoryTest extends UserRepositoryTestBase {

    @Test
    @DisplayName("save() persiste un utilisateur et lui attribue un ID")
    void save_shouldPersistUser() {
        User user = new User("alice", "alice@test.com", "password123");

        em.getTransaction().begin();
        User saved = userRepository.save(em, user);

        assertNotNull(saved.getId());
        assertEquals("alice", saved.getUsername());
        assertEquals("alice@test.com", saved.getEmail());
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    @DisplayName("findById() retourne l'utilisateur existant")
    void findById_shouldReturnUser_whenExists() {
        User user = new User("bob", "bob@test.com", "password123");
        em.getTransaction().begin();
        userRepository.save(em, user);

        Optional<User> found = userRepository.findById(em, user.getId());

        assertTrue(found.isPresent());
        assertEquals("bob", found.get().getUsername());
    }

    @Test
    @DisplayName("findById() retourne vide pour un ID inexistant")
    void findById_shouldReturnEmpty_whenNotExists() {
        Optional<User> found = userRepository.findById(em, 999L);
        assertTrue(found.isEmpty());
    }

    @Test
    @DisplayName("update() met a jour les champs de l'utilisateur")
    void update_shouldModifyUser() {
        User user = new User("charlie", "charlie@test.com", "password123");
        em.getTransaction().begin();
        userRepository.save(em, user);
        em.getTransaction().commit();

        user.setUsername("charlie_updated");
        user.setEmail("charlie_new@test.com");
        em.getTransaction().begin();
        User updated = userRepository.update(em, user);

        assertEquals("charlie_updated", updated.getUsername());
        assertEquals("charlie_new@test.com", updated.getEmail());
    }

    @Test
    @DisplayName("deleteById() supprime l'utilisateur")
    void deleteById_shouldRemoveUser() {
        User user = new User("dave", "dave@test.com", "password123");
        em.getTransaction().begin();
        userRepository.save(em, user);
        Long id = user.getId();
        em.getTransaction().commit();

        em.getTransaction().begin();
        userRepository.deleteById(em, id);

        Optional<User> found = userRepository.findById(em, id);
        assertTrue(found.isEmpty());
    }

    @Test
    @DisplayName("delete() avec entite supprime l'utilisateur")
    void delete_shouldRemoveUser() {
        User user = new User("eve", "eve@test.com", "password123");
        em.getTransaction().begin();
        userRepository.save(em, user);
        Long id = user.getId();
        em.getTransaction().commit();

        em.getTransaction().begin();
        userRepository.delete(em, user);

        Optional<User> found = userRepository.findById(em, id);
        assertTrue(found.isEmpty());
    }
}
