package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AnnonceCreateDTO {

    @Schema(description = "Titre de l'annonce", example = "Appartement 3 pieces")
    @NotBlank(message = "title is required")
    @Size(max = 64, message = "title must not exceed 64 characters")
    private String title;

    @Schema(description = "Description de l'annonce", example = "Appartement lumineux proche metro")
    @NotBlank(message = "description is required")
    @Size(max = 256, message = "description must not exceed 256 characters")
    private String description;

    @Schema(description = "Adresse du bien", example = "12 Rue de Paris, Montreuil")
    @NotBlank(message = "adress is required")
    @Size(max = 64, message = "adress must not exceed 64 characters")
    private String adress;

    @Schema(description = "Email de contact", example = "contact@masterannonce.fr")
    @NotBlank(message = "mail is required")
    @Email(message = "mail must be a valid email")
    @Size(max = 64, message = "mail must not exceed 64 characters")
    private String mail;

    @Schema(description = "Identifiant de la categorie", example = "1")
    @NotNull(message = "categoryId is required")
    private Long categoryId;

    public AnnonceCreateDTO() {
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAdress() { return adress; }
    public void setAdress(String adress) { this.adress = adress; }

    public String getMail() { return mail; }
    public void setMail(String mail) { this.mail = mail; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
}
