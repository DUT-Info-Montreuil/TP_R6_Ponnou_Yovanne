package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.model.User;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LoginAuthControllerRestTest extends AuthControllerRestTestBase {

    @Test
    @DisplayName("login_shouldReturn200WithToken")
    void login_shouldReturn200WithToken() throws Exception {
        User user = new User("testuser", "test@test.com", "hashed-password");
        user.setId(1L);
        user.setRole("ROLE_ADMIN");

        when(userService.authenticate(eq("testuser"), eq("password123"))).thenReturn(Optional.of(user));
        when(jwtService.generateToken(1L, "testuser", "ROLE_ADMIN")).thenReturn("jwt-token");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "testuser",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @DisplayName("login_shouldReturn401_whenWrongPassword")
    void login_shouldReturn401_whenWrongPassword() throws Exception {
        when(userService.authenticate(eq("testuser"), eq("wrongpassword"))).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "testuser",
                                  "password": "wrongpassword"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("login_shouldReturn401_whenUserNotFound")
    void login_shouldReturn401_whenUserNotFound() throws Exception {
        when(userService.authenticate(eq("unknown"), eq("password123"))).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "unknown",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("login_shouldReturn400_whenUsernameMissing")
    void login_shouldReturn400_whenUsernameMissing() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("login_shouldReturn400_whenPasswordMissing")
    void login_shouldReturn400_whenPasswordMissing() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "testuser"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }
}
