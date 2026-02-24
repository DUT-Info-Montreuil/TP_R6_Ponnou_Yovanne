package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.ratelimit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.repository.RefreshTokenRepository;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.config.PasswordUtils;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.model.User;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.repository.UserRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "rate-limit.login.capacity=3",
        "rate-limit.login.refill-seconds=60"
})
class LoginRateLimitFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private LoginRateLimitFilter loginRateLimitFilter;

    @BeforeEach
    void setUp() {
        loginRateLimitFilter.clearBuckets();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
        User user = new User("ratelimituser", "rl@test.com", PasswordUtils.hash("pass"));
        userRepository.save(user);
    }

    @Test
    @DisplayName("login_shouldReturn429_afterExceedingRateLimit")
    void login_shouldReturn429_afterExceedingRateLimit() throws Exception {
        String body = """
                {"username": "ratelimituser", "password": "pass"}
                """;

        // 3 requêtes autorisées (capacité = 3)
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk());
        }

        // La 4e doit être bloquée
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.error").value("TOO_MANY_REQUESTS"));
    }

    @Test
    @DisplayName("login_shouldReturn401_forWrongCredentials_withinRateLimit")
    void login_shouldReturn401_forWrongCredentials_withinRateLimit() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "ratelimituser", "password": "wrong"}
                                """))
                .andExpect(status().isUnauthorized());
    }
}
