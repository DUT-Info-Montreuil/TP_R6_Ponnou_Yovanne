package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
public class AnnonceDTO {

    @Schema(description = "Identifiant unique de l'annonce", example = "42")
    private Long id;
    @Schema(description = "Titre de l'annonce", example = "Appartement 3 pieces")
    private String title;
    @Schema(description = "Description de l'annonce", example = "Appartement lumineux proche metro")
    private String description;
    @Schema(description = "Adresse du bien", example = "12 Rue de Paris, Montreuil")
    private String adress;
    @Schema(description = "Email de contact", example = "contact@masterannonce.fr")
    private String mail;
    @Schema(description = "Date de creation", example = "2026-02-19T10:30:00.000+00:00")
    private Timestamp date;
    @Schema(description = "Statut de l'annonce", example = "DRAFT")
    private String status;
    @Schema(description = "Nom d'utilisateur de l'auteur", example = "yovanne")
    private String authorUsername;
    @Schema(description = "Libelle de la categorie", example = "Immobilier")
    private String categoryLabel;
}
