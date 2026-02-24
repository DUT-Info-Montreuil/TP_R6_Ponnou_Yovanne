package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.security.JwtAuthenticationFilter;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.security.JwtService;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.security.SecurityConfig;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.exception.GlobalExceptionHandler;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.exception.ResourceNotFoundException;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.logging.CorrelationIdFilter;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.dto.UserDTO;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.mapper.UserMapper;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.model.User;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.service.UserService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, CorrelationIdFilter.class, GlobalExceptionHandler.class})
class UserControllerRestTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserMapper userMapper;

    @MockitoBean
    private JwtService jwtService;

    // ── GET /api/users ───────────────────────────────────────────────────────

    @Test
    @DisplayName("getAll_shouldReturn200WithPaginatedUsers")
    @WithMockUser
    void getAll_shouldReturn200WithPaginatedUsers() throws Exception {
        User user = new User("alice", "alice@test.com", "pwd");
        user.setId(1L);
        UserDTO dto = new UserDTO();
        dto.setId(1L);
        dto.setUsername("alice");
        dto.setEmail("alice@test.com");

        when(userService.findAllPaginated(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(user)));
        when(userMapper.toDTO(user)).thenReturn(dto);

        mockMvc.perform(get("/api/users").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(1))
                .andExpect(jsonPath("$.items[0].username").value("alice"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalItems").value(1));
    }

    @Test
    @DisplayName("getAll_withoutAuth_shouldReturn200")
    void getAll_withoutAuth_shouldReturn200() throws Exception {
        when(userService.findAllPaginated(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk());
    }

    // ── GET /api/users/{id} ──────────────────────────────────────────────────

    @Test
    @DisplayName("getById_shouldReturn200_whenFound")
    @WithMockUser
    void getById_shouldReturn200_whenFound() throws Exception {
        User user = new User("bob", "bob@test.com", "pwd");
        user.setId(2L);
        UserDTO dto = new UserDTO();
        dto.setId(2L);
        dto.setUsername("bob");

        when(userService.findById(2L)).thenReturn(Optional.of(user));
        when(userMapper.toDTO(user)).thenReturn(dto);

        mockMvc.perform(get("/api/users/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.username").value("bob"));
    }

    @Test
    @DisplayName("getById_shouldReturn404_whenNotFound")
    @WithMockUser
    void getById_shouldReturn404_whenNotFound() throws Exception {
        when(userService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    // ── POST /api/users ──────────────────────────────────────────────────────

    @Test
    @DisplayName("create_shouldReturn201_withValidBody")
    void create_shouldReturn201_withValidBody() throws Exception {
        User created = new User("charlie", "charlie@test.com", "hashed");
        created.setId(3L);
        UserDTO dto = new UserDTO();
        dto.setId(3L);
        dto.setUsername("charlie");

        when(userService.create("charlie", "charlie@test.com", "password123")).thenReturn(created);
        when(userMapper.toDTO(created)).thenReturn(dto);

        mockMvc.perform(post("/api/users")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "charlie",
                                "email", "charlie@test.com",
                                "password", "password123"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.username").value("charlie"));
    }

    @Test
    @DisplayName("create_withBlankUsername_shouldReturn400")
    void create_withBlankUsername_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "",
                                "email", "valid@test.com",
                                "password", "password123"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("create_withInvalidEmail_shouldReturn400")
    void create_withInvalidEmail_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "validuser",
                                "email", "not-an-email",
                                "password", "password123"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("create_withShortPassword_shouldReturn400")
    void create_withShortPassword_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "validuser",
                                "email", "valid@test.com",
                                "password", "abc"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("create_withDuplicateUsername_shouldReturn400")
    void create_withDuplicateUsername_shouldReturn400() throws Exception {
        when(userService.create(any(), any(), any()))
                .thenThrow(new IllegalArgumentException("Ce nom d'utilisateur existe deja"));

        mockMvc.perform(post("/api/users")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "existing",
                                "email", "existing@test.com",
                                "password", "password123"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"));
    }

    // ── PUT /api/users/{id} ──────────────────────────────────────────────────

    @Test
    @DisplayName("update_shouldReturn200_whenAuthenticated")
    @WithMockUser
    void update_shouldReturn200_whenAuthenticated() throws Exception {
        User updated = new User("alice_new", "alice_new@test.com", "pwd");
        updated.setId(1L);
        UserDTO dto = new UserDTO();
        dto.setId(1L);
        dto.setUsername("alice_new");

        when(userService.update(eq(1L), eq("alice_new"), eq("alice_new@test.com"))).thenReturn(updated);
        when(userMapper.toDTO(updated)).thenReturn(dto);

        mockMvc.perform(put("/api/users/1")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "alice_new",
                                "email", "alice_new@test.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice_new"));
    }

    @Test
    @DisplayName("update_whenNotFound_shouldReturn404")
    @WithMockUser
    void update_whenNotFound_shouldReturn404() throws Exception {
        when(userService.update(eq(99L), any(), any()))
                .thenThrow(new ResourceNotFoundException("Utilisateur non trouve"));

        mockMvc.perform(put("/api/users/99")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "someone",
                                "email", "someone@test.com"))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("update_withBlankUsername_shouldReturn400")
    @WithMockUser
    void update_withBlankUsername_shouldReturn400() throws Exception {
        mockMvc.perform(put("/api/users/1")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "",
                                "email", "valid@test.com"))))
                .andExpect(status().isBadRequest());
    }

    // ── DELETE /api/users/{id} ───────────────────────────────────────────────

    @Test
    @DisplayName("delete_asAdmin_shouldReturn204")
    @WithMockUser(roles = "ADMIN")
    void delete_asAdmin_shouldReturn204() throws Exception {
        doNothing().when(userService).delete(1L);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("delete_asUser_shouldReturn403")
    @WithMockUser(roles = "USER")
    void delete_asUser_shouldReturn403() throws Exception {
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("delete_whenNotFound_shouldReturn404")
    @WithMockUser(roles = "ADMIN")
    void delete_whenNotFound_shouldReturn404() throws Exception {
        doThrow(new ResourceNotFoundException("Utilisateur non trouve"))
                .when(userService).delete(99L);

        mockMvc.perform(delete("/api/users/99"))
                .andExpect(status().isNotFound());
    }

    // ── PATCH /api/users/{id} ────────────────────────────────────────────────

    @Test
    @DisplayName("patch_shouldReturn200_whenAuthenticated")
    @WithMockUser
    void patch_shouldReturn200_whenAuthenticated() throws Exception {
        User patched = new User("alice_patched", "alice@test.com", "pwd");
        patched.setId(1L);
        UserDTO dto = new UserDTO();
        dto.setId(1L);
        dto.setUsername("alice_patched");

        when(userService.patch(eq(1L), any())).thenReturn(patched);
        when(userMapper.toDTO(patched)).thenReturn(dto);

        mockMvc.perform(patch("/api/users/1")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("username", "alice_patched"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice_patched"));
    }

    @Test
    @DisplayName("patch_withoutAuth_shouldReturn403")
    void patch_withoutAuth_shouldReturn403() throws Exception {
        mockMvc.perform(patch("/api/users/1")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("username", "alice_patched"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("patch_whenNotFound_shouldReturn404")
    @WithMockUser
    void patch_whenNotFound_shouldReturn404() throws Exception {
        doThrow(new ResourceNotFoundException("Utilisateur non trouve"))
                .when(userService).patch(eq(99L), any());

        mockMvc.perform(patch("/api/users/99")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("username", "someone"))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("patch_withInvalidEmail_shouldReturn400")
    @WithMockUser
    void patch_withInvalidEmail_shouldReturn400() throws Exception {
        mockMvc.perform(patch("/api/users/1")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", "not-an-email"))))
                .andExpect(status().isBadRequest());
    }
}
