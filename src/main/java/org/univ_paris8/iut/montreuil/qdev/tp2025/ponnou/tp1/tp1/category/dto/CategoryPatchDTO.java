package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.dto;

import javax.validation.constraints.Size;

public class CategoryPatchDTO {

    @Size(max = 100, message = "label must not exceed 100 characters")
    private String label;

    public CategoryPatchDTO() {
    }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
}
