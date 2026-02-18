package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.repository;

import org.junit.jupiter.api.*;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.model.User;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SearchUserRepositoryTest extends UserRepositoryTestBase {

    @Test
    @DisplayName("findOneWithFilters() trouve un utilisateur par username")
    void findOneWithFilters_shouldFindByUsername() {
        User user = new User("frank", "frank@test.com", "password123");
        em.getTransaction().begin();
        userRepository.save(em, user);

        Optional<User> found = userRepository.findOneWithFilters(em, Map.of("username", "frank"));

        assertTrue(found.isPresent());
        assertEquals("frank@test.com", found.get().getEmail());
    }

    @Test
    @DisplayName("findOneWithFilters() retourne vide si aucun match")
    void findOneWithFilters_shouldReturnEmpty_whenNoMatch() {
        Optional<User> found = userRepository.findOneWithFilters(em, Map.of("username", "inexistant"));
        assertTrue(found.isEmpty());
    }

    @Test
    @DisplayName("countWithFilters() compte correctement les utilisateurs")
    void countWithFilters_shouldReturnCorrectCount() {
        em.getTransaction().begin();
        userRepository.save(em, new User("user1", "user1@test.com", "password123"));
        userRepository.save(em, new User("user2", "user2@test.com", "password123"));
        userRepository.save(em, new User("user3", "user3@test.com", "password123"));
        em.getTransaction().commit();

        long total = userRepository.count(em);
        assertEquals(3, total);

        long filtered = userRepository.countWithFilters(em, Map.of("username", "user1"));
        assertEquals(1, filtered);
    }

    @Test
    @DisplayName("findWithFilters() avec tri retourne les resultats ordonnes")
    void findWithFilters_shouldReturnOrdered() {
        em.getTransaction().begin();
        userRepository.save(em, new User("zara", "zara@test.com", "password123"));
        userRepository.save(em, new User("adam", "adam@test.com", "password123"));
        userRepository.save(em, new User("mia", "mia@test.com", "password123"));
        em.getTransaction().commit();

        List<User> users = userRepository.findWithFilters(em, null, null, null, "username ASC", null);

        assertEquals(3, users.size());
        assertEquals("adam", users.get(0).getUsername());
        assertEquals("mia", users.get(1).getUsername());
        assertEquals("zara", users.get(2).getUsername());
    }
}
