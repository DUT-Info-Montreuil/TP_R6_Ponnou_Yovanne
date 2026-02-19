package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.integration;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AnnonceWorkflowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AnnonceRepository annonceRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    private User user1;
    private User user2;
    private User admin;
    private Category category;

    @BeforeEach
    void setUp() {
        annonceRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        user1 = userRepository.save(new User("user1", "user1@test.com", PasswordUtils.hash("password123")));
        user2 = userRepository.save(new User("user2", "user2@test.com", PasswordUtils.hash("password123")));
        admin = new User("admin", "admin@test.com", PasswordUtils.hash("password123"));
        admin.setRole("ROLE_ADMIN");
        admin = userRepository.save(admin);

        category = categoryRepository.save(new Category("Immobilier"));
    }

    @Test
    @DisplayName("fullCrudWorkflow")
    void fullCrudWorkflow() throws Exception {
        String adminToken = loginAndGetToken("admin", "password123");

        String createResponse = mockMvc.perform(post("/api/annonces")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title":"Appartement",
                                  "description":"Bel appartement",
                                  "adress":"Paris",
                                  "mail":"contact@test.com",
                                  "categoryId":%d
                                }
                                """.formatted(category.getId())))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long annonceId = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(get("/api/annonces/{id}", annonceId))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/annonces/{id}", annonceId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title":"Appartement MAJ",
                                  "description":"Desc MAJ",
                                  "adress":"Lyon",
                                  "mail":"updated@test.com",
                                  "categoryId":%d
                                }
                                """.formatted(category.getId())))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/annonces/{id}/publish", annonceId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"));

        mockMvc.perform(put("/api/annonces/{id}", annonceId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title":"Interdit",
                                  "description":"Interdit",
                                  "adress":"Paris",
                                  "mail":"forbidden@test.com",
                                  "categoryId":%d
                                }
                                """.formatted(category.getId())))
                .andExpect(status().isConflict());

        mockMvc.perform(put("/api/annonces/{id}/archive", annonceId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ARCHIVED"));

        mockMvc.perform(delete("/api/annonces/{id}", annonceId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("create_shouldReturn403_withoutToken")
    void create_shouldReturn403_withoutToken() throws Exception {
        mockMvc.perform(post("/api/annonces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title":"Appartement",
                                  "description":"Bel appartement",
                                  "adress":"Paris",
                                  "mail":"contact@test.com",
                                  "categoryId":1
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("update_shouldReturn403_whenNotOwner")
    void update_shouldReturn403_whenNotOwner() throws Exception {
        Annonce annonce = new Annonce("Titre", "Desc", "Paris", "mail@test.com");
        annonce.setStatus(AnnonceStatus.DRAFT);
        annonce.setAuthor(user1);
        annonce.setCategory(category);
        annonce = annonceRepository.save(annonce);

        String tokenUser2 = loginAndGetToken("user2", "password123");

        mockMvc.perform(put("/api/annonces/{id}", annonce.getId())
                        .header("Authorization", "Bearer " + tokenUser2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title":"Hack",
                                  "description":"Hack",
                                  "adress":"Paris",
                                  "mail":"hack@test.com",
                                  "categoryId":%d
                                }
                                """.formatted(category.getId())))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("update_shouldReturn409_whenPublished")
    void update_shouldReturn409_whenPublished() throws Exception {
        String tokenUser1 = loginAndGetToken("user1", "password123");

        String createResponse = mockMvc.perform(post("/api/annonces")
                        .header("Authorization", "Bearer " + tokenUser1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title":"Appartement",
                                  "description":"Bel appartement",
                                  "adress":"Paris",
                                  "mail":"contact@test.com",
                                  "categoryId":%d
                                }
                                """.formatted(category.getId())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long annonceId = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(put("/api/annonces/{id}/publish", annonceId)
                        .header("Authorization", "Bearer " + tokenUser1))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/annonces/{id}", annonceId)
                        .header("Authorization", "Bearer " + tokenUser1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title":"Interdit",
                                  "description":"Interdit",
                                  "adress":"Paris",
                                  "mail":"forbidden@test.com",
                                  "categoryId":%d
                                }
                                """.formatted(category.getId())))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("delete_shouldReturn409_whenNotArchived")
    void delete_shouldReturn409_whenNotArchived() throws Exception {
        Annonce annonce = new Annonce("Titre", "Desc", "Paris", "mail@test.com");
        annonce.setStatus(AnnonceStatus.DRAFT);
        annonce.setAuthor(user1);
        annonce.setCategory(category);
        annonce = annonceRepository.save(annonce);

        String tokenUser1 = loginAndGetToken("user1", "password123");

        mockMvc.perform(delete("/api/annonces/{id}", annonce.getId())
                        .header("Authorization", "Bearer " + tokenUser1))
                .andExpect(status().isConflict());
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
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(loginResponse);
        return jsonNode.get("token").asText();
    }
}
