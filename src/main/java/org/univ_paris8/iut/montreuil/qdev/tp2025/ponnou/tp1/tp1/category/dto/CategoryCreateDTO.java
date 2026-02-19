package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CategoryCreateDTO {

    @Schema(description = "Libelle de la categorie", example = "Immobilier")
    @NotBlank(message = "label is required")
    @Size(max = 100, message = "label must not exceed 100 characters")
    private String label;

    public CategoryCreateDTO() {
    }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
}
