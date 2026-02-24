package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.controller;

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
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.dto.CategoryDTO;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.mapper.CategoryMapper;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.model.Category;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.service.CategoryService;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.exception.GlobalExceptionHandler;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.logging.CorrelationIdFilter;

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

@WebMvcTest(controllers = CategoryController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, CorrelationIdFilter.class, GlobalExceptionHandler.class})
class CategoryControllerRestTest {

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

    // ── GET /api/categories ──────────────────────────────────────────────────

    @Test
    @DisplayName("getAll_shouldReturn200WithPaginatedCategories")
    @WithMockUser
    void getAll_shouldReturn200WithPaginatedCategories() throws Exception {
        Category cat = new Category("Immobilier");
        cat.setId(1L);
        CategoryDTO dto = new CategoryDTO();
        dto.setId(1L);
        dto.setLabel("Immobilier");

        when(categoryService.findAllPaginated(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(cat)));
        when(categoryMapper.toDTO(cat)).thenReturn(dto);

        mockMvc.perform(get("/api/categories").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(1))
                .andExpect(jsonPath("$.items[0].label").value("Immobilier"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalItems").value(1));
    }

    @Test
    @DisplayName("getAll_withoutAuth_shouldReturn200")
    void getAll_withoutAuth_shouldReturn200() throws Exception {
        when(categoryService.findAllPaginated(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk());
    }

    // ── GET /api/categories/{id} ─────────────────────────────────────────────

    @Test
    @DisplayName("getById_shouldReturn200_whenFound")
    @WithMockUser
    void getById_shouldReturn200_whenFound() throws Exception {
        Category cat = new Category("Vehicules");
        cat.setId(2L);
        CategoryDTO dto = new CategoryDTO();
        dto.setId(2L);
        dto.setLabel("Vehicules");

        when(categoryService.findById(2L)).thenReturn(Optional.of(cat));
        when(categoryMapper.toDTO(cat)).thenReturn(dto);

        mockMvc.perform(get("/api/categories/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.label").value("Vehicules"));
    }

    @Test
    @DisplayName("getById_shouldReturn404_whenNotFound")
    @WithMockUser
    void getById_shouldReturn404_whenNotFound() throws Exception {
        when(categoryService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/categories/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    // ── PUT /api/categories/{id} ─────────────────────────────────────────────

    @Test
    @DisplayName("update_asAdmin_shouldReturn200")
    @WithMockUser(roles = "ADMIN")
    void update_asAdmin_shouldReturn200() throws Exception {
        Category updated = new Category("Electronique");
        updated.setId(1L);
        CategoryDTO dto = new CategoryDTO();
        dto.setId(1L);
        dto.setLabel("Electronique");

        when(categoryService.update(eq(1L), eq("Electronique"))).thenReturn(updated);
        when(categoryMapper.toDTO(updated)).thenReturn(dto);

        mockMvc.perform(put("/api/categories/1")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("label", "Electronique"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.label").value("Electronique"));
    }

    @Test
    @DisplayName("update_asUser_shouldReturn403")
    @WithMockUser(roles = "USER")
    void update_asUser_shouldReturn403() throws Exception {
        mockMvc.perform(put("/api/categories/1")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("label", "Electronique"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("update_withBlankLabel_shouldReturn400")
    @WithMockUser(roles = "ADMIN")
    void update_withBlankLabel_shouldReturn400() throws Exception {
        mockMvc.perform(put("/api/categories/1")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("label", ""))))
                .andExpect(status().isBadRequest());
    }

    // ── DELETE /api/categories/{id} ──────────────────────────────────────────

    @Test
    @DisplayName("delete_asAdmin_shouldReturn204")
    @WithMockUser(roles = "ADMIN")
    void delete_asAdmin_shouldReturn204() throws Exception {
        doNothing().when(categoryService).delete(1L);

        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("delete_asUser_shouldReturn403")
    @WithMockUser(roles = "USER")
    void delete_asUser_shouldReturn403() throws Exception {
        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("delete_whenNotFound_shouldReturn404")
    @WithMockUser(roles = "ADMIN")
    void delete_whenNotFound_shouldReturn404() throws Exception {
        doThrow(new org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.exception.ResourceNotFoundException("Categorie non trouvee"))
                .when(categoryService).delete(99L);

        mockMvc.perform(delete("/api/categories/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("delete_withPublishedAnnonces_shouldReturn409")
    @WithMockUser(roles = "ADMIN")
    void delete_withPublishedAnnonces_shouldReturn409() throws Exception {
        doThrow(new IllegalStateException("Impossible de supprimer une categorie contenant des annonces"))
                .when(categoryService).delete(1L);

        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CONFLICT"));
    }

    // ── PATCH /api/categories/{id} ───────────────────────────────────────────

    @Test
    @DisplayName("patch_asAdmin_shouldReturn200")
    @WithMockUser(roles = "ADMIN")
    void patch_asAdmin_shouldReturn200() throws Exception {
        Category patched = new Category("Services");
        patched.setId(1L);
        CategoryDTO dto = new CategoryDTO();
        dto.setId(1L);
        dto.setLabel("Services");

        when(categoryService.patch(eq(1L), any())).thenReturn(patched);
        when(categoryMapper.toDTO(patched)).thenReturn(dto);

        mockMvc.perform(patch("/api/categories/1")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("label", "Services"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.label").value("Services"));
    }

    @Test
    @DisplayName("patch_asUser_shouldReturn403")
    @WithMockUser(roles = "USER")
    void patch_asUser_shouldReturn403() throws Exception {
        mockMvc.perform(patch("/api/categories/1")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("label", "Services"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("patch_whenNotFound_shouldReturn404")
    @WithMockUser(roles = "ADMIN")
    void patch_whenNotFound_shouldReturn404() throws Exception {
        doThrow(new org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.exception.ResourceNotFoundException("Categorie non trouvee"))
                .when(categoryService).patch(eq(99L), any());

        mockMvc.perform(patch("/api/categories/99")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("label", "Services"))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("patch_withLabelTooLong_shouldReturn400")
    @WithMockUser(roles = "ADMIN")
    void patch_withLabelTooLong_shouldReturn400() throws Exception {
        String tooLong = "A".repeat(101);

        mockMvc.perform(patch("/api/categories/1")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("label", tooLong))))
                .andExpect(status().isBadRequest());
    }
}
