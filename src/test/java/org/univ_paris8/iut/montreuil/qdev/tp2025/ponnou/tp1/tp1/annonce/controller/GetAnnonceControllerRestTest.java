package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.dto.AnnonceDTO;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.model.Annonce;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.model.AnnonceStatus;

import java.sql.Timestamp;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GetAnnonceControllerRestTest extends AnnonceControllerRestTestBase {

    @Test
    @DisplayName("getAll_shouldReturn200WithPagination")
    void getAll_shouldReturn200WithPagination() throws Exception {
        Annonce entity = new Annonce("Titre", "Desc", "Paris", "mail@test.com");
        entity.setId(1L);

        AnnonceDTO dto = new AnnonceDTO();
        dto.setId(1L);
        dto.setTitle("Titre");

        Page<Annonce> page = new PageImpl<>(List.of(entity), Pageable.ofSize(10).withPage(0), 1);
        when(annonceService.searchWithFilters(any(), any(), any(), any(), any(), any(), any())).thenReturn(page);
        when(annonceMapper.toDTO(entity)).thenReturn(dto);

        mockMvc.perform(get("/api/annonces").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(1))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalItems").value(1));
    }

    @Test
    @DisplayName("getAll_withSortParam_shouldSortCorrectly")
    void getAll_withSortParam_shouldSortCorrectly() throws Exception {
        when(annonceService.searchWithFilters(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Page.empty());

        mockMvc.perform(get("/api/annonces").param("sort", "title,asc"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(annonceService).searchWithFilters(any(), any(), any(), any(), any(), any(), pageableCaptor.capture());
        Sort.Order order = pageableCaptor.getValue().getSort().getOrderFor("title");
        org.junit.jupiter.api.Assertions.assertNotNull(order);
        org.junit.jupiter.api.Assertions.assertEquals(Sort.Direction.ASC, order.getDirection());
    }

    @Test
    @DisplayName("getAll_withInvalidSort_shouldReturn400")
    void getAll_withInvalidSort_shouldReturn400() throws Exception {
        mockMvc.perform(get("/api/annonces").param("sort", "invalidField,asc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"));
    }

    @Test
    @DisplayName("getAll_withFilters_shouldFilterResults")
    void getAll_withFilters_shouldFilterResults() throws Exception {
        when(annonceService.searchWithFilters(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Page.empty());

        mockMvc.perform(get("/api/annonces")
                        .param("keyword", "appart")
                        .param("status", "PUBLISHED")
                        .param("categoryId", "1")
                        .param("authorId", "2")
                        .param("fromDate", "2024-01-01T00:00:00")
                        .param("toDate", "2025-12-31T23:59:59"))
                .andExpect(status().isOk());

        verify(annonceService).searchWithFilters(
                eq("appart"),
                eq(1L),
                eq(2L),
                eq(AnnonceStatus.PUBLISHED),
                eq(Timestamp.valueOf("2024-01-01 00:00:00")),
                eq(Timestamp.valueOf("2025-12-31 23:59:59")),
                any(Pageable.class));
    }
}
