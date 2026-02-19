package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.dto.UserDTO;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.model.User;

import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    private final UserMapper mapper = new UserMapperSpringImpl();

    @Test
    @DisplayName("toDTO() mappe tous les champs correctement")
    void toDTO_shouldMapAllFields() {
        User user = new User("alice", "alice@test.com", "password123");
        user.setId(1L);
        user.setCreatedAt(new Timestamp(1000000L));

        UserDTO dto = mapper.toDTO(user);

        assertEquals(1L, dto.getId());
        assertEquals("alice", dto.getUsername());
        assertEquals("alice@test.com", dto.getEmail());
        assertEquals(new Timestamp(1000000L), dto.getCreatedAt());
    }

    @Test
    @DisplayName("toDTO() mappe aussi le role")
    void toDTO_shouldMapRole() {
        User user = new User("alice", "alice@test.com", "secret123");
        user.setRole("ROLE_ADMIN");

        UserDTO dto = mapper.toDTO(user);

        assertEquals("ROLE_ADMIN", dto.getRole());
    }

    @Test
    @DisplayName("toDTOList() mappe une liste d'utilisateurs")
    void toDTOList_shouldMapAllElements() {
        User u1 = new User("alice", "alice@test.com", "pass1");
        u1.setId(1L);
        User u2 = new User("bob", "bob@test.com", "pass2");
        u2.setId(2L);

        List<UserDTO> dtos = mapper.toDTOList(List.of(u1, u2));

        assertEquals(2, dtos.size());
        assertEquals("alice", dtos.get(0).getUsername());
        assertEquals("bob", dtos.get(1).getUsername());
    }

    @Test
    @DisplayName("toDTOList() retourne une liste vide pour une entree vide")
    void toDTOList_shouldReturnEmptyList() {
        List<UserDTO> dtos = mapper.toDTOList(List.of());
        assertTrue(dtos.isEmpty());
    }
}
