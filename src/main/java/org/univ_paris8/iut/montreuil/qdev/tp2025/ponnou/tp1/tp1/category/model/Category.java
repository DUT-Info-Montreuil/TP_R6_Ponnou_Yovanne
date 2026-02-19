package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.model;

import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.model.Annonce;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le libellé est obligatoire")
    @Size(max = 100, message = "Le libellé ne doit pas dépasser 100 caractères")
    @Column(unique = true, nullable = false, length = 100)
    private String label;

    @OneToMany(mappedBy = "category")
    private List<Annonce> annonces = new ArrayList<>();

    public Category() {
    }

    public Category(String label) {
        this.label = label;
    }
}
