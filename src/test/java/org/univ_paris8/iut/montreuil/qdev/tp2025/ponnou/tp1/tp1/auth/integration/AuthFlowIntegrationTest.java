package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.config.PasswordUtils;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.model.User;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.repository.UserRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private User persistedUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        User user = new User("testuser", "test@test.com", PasswordUtils.hash("password123"));
        persistedUser = userRepository.save(user);
    }

    @Test
    @DisplayName("fullFlow_login_thenAccessSecuredEndpoint")
    void fullFlow_login_thenAccessSecuredEndpoint() throws Exception {
        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "testuser",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode loginJson = objectMapper.readTree(loginResponse);
        String token = loginJson.get("token").asText();

        mockMvc.perform(put("/api/users/{id}", persistedUser.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "testuser_updated",
                                  "email": "updated@test.com"
                                }
                                """))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("securedEndpoint_shouldReturn403_withoutToken")
    void securedEndpoint_shouldReturn403_withoutToken() throws Exception {
        mockMvc.perform(put("/api/users/{id}", persistedUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "hacker",
                                  "email": "hack@test.com"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("securedEndpoint_shouldReturn403_withInvalidToken")
    void securedEndpoint_shouldReturn403_withInvalidToken() throws Exception {
        mockMvc.perform(put("/api/users/{id}", persistedUser.getId())
                        .header("Authorization", "Bearer invalid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "hacker",
                                  "email": "hack@test.com"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("publicEndpoint_shouldReturn200_withoutToken")
    void publicEndpoint_shouldReturn200_withoutToken() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk());
    }
}
