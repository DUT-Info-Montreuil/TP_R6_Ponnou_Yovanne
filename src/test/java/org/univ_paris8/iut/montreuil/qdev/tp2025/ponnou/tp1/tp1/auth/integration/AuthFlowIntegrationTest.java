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
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.model.Annonce;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.model.AnnonceStatus;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.repository.AnnonceRepository;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.model.Category;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.repository.CategoryRepository;
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

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private AnnonceRepository annonceRepository;

    private User persistedUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        annonceRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
        User user = new User("testuser", "test@test.com", PasswordUtils.hash("password123"));
        persistedUser = userRepository.save(user);
        User admin = new User("admin", "admin@test.com", PasswordUtils.hash("password123"));
        admin.setRole("ROLE_ADMIN");
        adminUser = userRepository.save(admin);
    }

    @Test
    @DisplayName("fullFlow_login_thenAccessSecuredEndpoint")
    void fullFlow_login_thenAccessSecuredEndpoint() throws Exception {
        String token = loginAndGetToken("testuser", "password123");

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

    @Test
    @DisplayName("categoryCreate_shouldReturn403_withRoleUserToken")
    void categoryCreate_shouldReturn403_withRoleUserToken() throws Exception {
        String token = loginAndGetToken("testuser", "password123");

        mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "label": "Immobilier"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("categoryCreate_shouldReturn201_withRoleAdminToken")
    void categoryCreate_shouldReturn201_withRoleAdminToken() throws Exception {
        String token = loginAndGetToken("admin", "password123");

        mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "label": "Vehicules"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.label").value("Vehicules"));
    }

    @Test
    @DisplayName("archive_shouldReturn403_withRoleUserToken")
    void archive_shouldReturn403_withRoleUserToken() throws Exception {
        String token = loginAndGetToken("testuser", "password123");

        Category category = categoryRepository.save(new Category("Immobilier"));
        Annonce annonce = new Annonce("Titre", "Description", "Paris", "mail@test.com");
        annonce.setAuthor(persistedUser);
        annonce.setCategory(category);
        annonce.setStatus(AnnonceStatus.DRAFT);
        annonce = annonceRepository.save(annonce);

        mockMvc.perform(put("/api/annonces/{id}/archive", annonce.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("archive_shouldReturn200_withRoleAdminToken")
    void archive_shouldReturn200_withRoleAdminToken() throws Exception {
        String token = loginAndGetToken("admin", "password123");

        Category category = categoryRepository.save(new Category("Vehicules"));
        Annonce annonce = new Annonce("Titre", "Description", "Paris", "mail@test.com");
        annonce.setAuthor(adminUser);
        annonce.setCategory(category);
        annonce.setStatus(AnnonceStatus.DRAFT);
        annonce = annonceRepository.save(annonce);

        mockMvc.perform(put("/api/annonces/{id}/archive", annonce.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(annonce.getId()))
                .andExpect(jsonPath("$.status").value("ARCHIVED"));
    }

    private String loginAndGetToken(String username, String password) throws Exception {
        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "%s"
                                }
                                """.formatted(username, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode loginJson = objectMapper.readTree(loginResponse);
        return loginJson.get("token").asText();
    }
}
