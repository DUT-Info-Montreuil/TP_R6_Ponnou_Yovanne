package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.dto;

public class CategoryDTO {

    private Long id;
    private String label;

    public CategoryDTO() {
    }

    private CategoryDTO(Builder builder) {
        this.id = builder.id;
        this.label = builder.label;
    }

    public Long getId() { return id; }
    public String getLabel() { return label; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String label;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder label(String label) { this.label = label; return this; }

        public CategoryDTO build() {
            return new CategoryDTO(this);
        }
    }
}
