package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CategoryDTO {

    @Schema(description = "Identifiant de la categorie", example = "1")
    private Long id;
    @Schema(description = "Libelle de la categorie", example = "Immobilier")
    private String label;
}
