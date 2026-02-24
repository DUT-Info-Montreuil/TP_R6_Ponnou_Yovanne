package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.security.JwtAuthenticationFilter;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.security.JwtService;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.security.SecurityConfig;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.dto.CategoryDTO;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.mapper.CategoryMapper;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.model.Category;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.service.CategoryService;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.logging.CorrelationIdFilter;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.exception.GlobalExceptionHandler;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CategoryController.class)
@Import({ SecurityConfig.class, JwtAuthenticationFilter.class, CorrelationIdFilter.class,
        GlobalExceptionHandler.class })
class CategorySecurityControllerRestTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private CategoryMapper categoryMapper;

    @MockitoBean
    private JwtService jwtService;

    @Test
    @DisplayName("create_asAdmin_shouldReturn201")
    @WithMockUser(roles = "ADMIN")
    void create_asAdmin_shouldReturn201() throws Exception {
        Category created = new Category("Immobilier");
        created.setId(1L);
        CategoryDTO dto = new CategoryDTO();
        dto.setId(1L);
        dto.setLabel("Immobilier");

        when(categoryService.create(eq("Immobilier"))).thenReturn(created);
        when(categoryMapper.toDTO(created)).thenReturn(dto);

        mockMvc.perform(post("/api/categories")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(java.util.Map.of("label", "Immobilier"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.label").value("Immobilier"));
    }

    @Test
    @DisplayName("create_asUser_shouldReturn403")
    @WithMockUser(roles = "USER")
    void create_asUser_shouldReturn403() throws Exception {
        mockMvc.perform(post("/api/categories")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(java.util.Map.of("label", "Immobilier"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("create_withoutAuth_shouldReturn403")
    void create_withoutAuth_shouldReturn403() throws Exception {
        mockMvc.perform(post("/api/categories")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(java.util.Map.of("label", "Immobilier"))))
                .andExpect(status().isForbidden());
    }
}
