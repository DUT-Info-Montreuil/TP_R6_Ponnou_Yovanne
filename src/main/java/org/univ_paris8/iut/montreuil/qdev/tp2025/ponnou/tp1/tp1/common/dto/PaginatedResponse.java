package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Reponse paginee")
public class PaginatedResponse<T> {

    @Schema(description = "Elements de la page courante")
    private List<T> items;
    @Schema(description = "Numero de page", example = "0")
    private int page;
    @Schema(description = "Taille de la page", example = "10")
    private int size;
    @Schema(description = "Nombre total d'elements", example = "42")
    private long totalItems;
    @Schema(description = "Nombre total de pages", example = "5")
    private int totalPages;

    public PaginatedResponse() {
    }

    public PaginatedResponse(List<T> items, int page, int size, long totalItems) {
        this.items = items;
        this.page = page;
        this.size = size;
        this.totalItems = totalItems;
        this.totalPages = size > 0 ? (int) Math.ceil((double) totalItems / size) : 0;
    }

    public List<T> getItems() { return items; }
    public void setItems(List<T> items) { this.items = items; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public long getTotalItems() { return totalItems; }
    public void setTotalItems(long totalItems) { this.totalItems = totalItems; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
}
