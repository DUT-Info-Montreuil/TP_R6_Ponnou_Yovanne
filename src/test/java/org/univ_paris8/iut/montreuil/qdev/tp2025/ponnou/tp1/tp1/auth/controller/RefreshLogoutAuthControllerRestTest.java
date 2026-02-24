package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.model.RefreshToken;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.security.JwtAuthenticationFilter;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.security.JwtService;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.service.RefreshTokenService;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.exception.GlobalExceptionHandler;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.model.User;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.service.UserService;

import java.time.Instant;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class RefreshLogoutAuthControllerRestTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private RefreshTokenService refreshTokenService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    // --- /refresh ---

    @Test
    @DisplayName("refresh_shouldReturn200_whenTokenIsValid")
    void refresh_shouldReturn200_whenTokenIsValid() throws Exception {
        User user = new User("alice", "alice@test.com", "hash");
        user.setId(1L);
        user.setRole("ROLE_USER");

        RefreshToken existing = new RefreshToken("old-token", user, Instant.now().plusSeconds(3600));
        RefreshToken newToken = new RefreshToken("new-refresh-token", user, Instant.now().plusSeconds(3600));

        when(refreshTokenService.findByToken("old-token")).thenReturn(Optional.of(existing));
        when(jwtService.generateToken(1L, "alice", "ROLE_USER")).thenReturn("new-jwt");
        when(refreshTokenService.createRefreshToken(user)).thenReturn(newToken);

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken": "old-token"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("new-jwt"))
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"));
    }

    @Test
    @DisplayName("refresh_shouldReturn401_whenTokenNotFound")
    void refresh_shouldReturn401_whenTokenNotFound() throws Exception {
        when(refreshTokenService.findByToken("unknown-token")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken": "unknown-token"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("refresh_shouldReturn401_whenTokenRevoked")
    void refresh_shouldReturn401_whenTokenRevoked() throws Exception {
        User user = new User("alice", "alice@test.com", "hash");
        user.setId(1L);
        RefreshToken revoked = new RefreshToken("rev-token", user, Instant.now().plusSeconds(3600));
        revoked.setRevoked(true);

        when(refreshTokenService.findByToken("rev-token")).thenReturn(Optional.of(revoked));

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken": "rev-token"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("refresh_shouldReturn401_whenTokenExpired")
    void refresh_shouldReturn401_whenTokenExpired() throws Exception {
        User user = new User("alice", "alice@test.com", "hash");
        user.setId(1L);
        RefreshToken expired = new RefreshToken("exp-token", user, Instant.now().minusSeconds(1));

        when(refreshTokenService.findByToken("exp-token")).thenReturn(Optional.of(expired));

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken": "exp-token"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("refresh_shouldReturn400_whenBodyMissing")
    void refresh_shouldReturn400_whenBodyMissing() throws Exception {
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    // --- /logout ---

    @Test
    @DisplayName("logout_shouldReturn204_whenTokenIsValid")
    void logout_shouldReturn204_whenTokenIsValid() throws Exception {
        User user = new User("alice", "alice@test.com", "hash");
        user.setId(1L);
        RefreshToken rt = new RefreshToken("valid-token", user, Instant.now().plusSeconds(3600));

        when(refreshTokenService.findByToken("valid-token")).thenReturn(Optional.of(rt));
        doNothing().when(refreshTokenService).revokeAllByUser(1L);

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken": "valid-token"}
                                """))
                .andExpect(status().isNoContent());

        verify(refreshTokenService).revokeAllByUser(1L);
    }

    @Test
    @DisplayName("logout_shouldReturn401_whenTokenNotFound")
    void logout_shouldReturn401_whenTokenNotFound() throws Exception {
        when(refreshTokenService.findByToken("bad-token")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken": "bad-token"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("logout_shouldReturn400_whenBodyMissing")
    void logout_shouldReturn400_whenBodyMissing() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }
}
