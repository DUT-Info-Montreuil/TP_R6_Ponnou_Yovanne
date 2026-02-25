package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.exception.ResourceNotFoundException;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.model.User;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthFindDeleteUserServiceTest extends UserServiceTestBase {

    @Test
    @DisplayName("authenticate_shouldReturnUser_whenValid")
    void authenticate_shouldReturnUser_whenValid() {
        User user = new User("john", "john@test.com", passwordEncoder.encode("secret"));
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));

        Optional<User> result = userService.authenticate("john", "secret");

        assertTrue(result.isPresent());
    }

    @Test
    @DisplayName("authenticate_shouldReturnEmpty_whenWrongPassword")
    void authenticate_shouldReturnEmpty_whenWrongPassword() {
        User user = new User("john", "john@test.com", passwordEncoder.encode("secret"));
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));

        Optional<User> result = userService.authenticate("john", "wrong");

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("authenticate_shouldReturnEmpty_whenUserNotFound")
    void authenticate_shouldReturnEmpty_whenUserNotFound() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        assertTrue(userService.authenticate("missing", "secret").isEmpty());
    }

    @Test
    @DisplayName("delete_shouldDelete_whenExists")
    void delete_shouldDelete_whenExists() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.delete(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    @DisplayName("delete_shouldThrow_whenNotFound")
    void delete_shouldThrow_whenNotFound() {
        when(userRepository.existsById(404L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> userService.delete(404L));
    }

    @Test
    @DisplayName("findById_shouldReturnUser")
    void findById_shouldReturnUser() {
        User user = new User("john", "john@test.com", "pwd");
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Optional<User> result = userService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals("john", result.get().getUsername());
    }

    @Test
    @DisplayName("findAllPaginated_shouldReturnPage")
    void findAllPaginated_shouldReturnPage() {
        User user = new User("john", "john@test.com", "pwd");
        when(userRepository.findAll(PageRequest.of(0, 10))).thenReturn(new PageImpl<>(List.of(user)));

        var page = userService.findAllPaginated(PageRequest.of(0, 10));

        assertEquals(1, page.getTotalElements());
    }
}
