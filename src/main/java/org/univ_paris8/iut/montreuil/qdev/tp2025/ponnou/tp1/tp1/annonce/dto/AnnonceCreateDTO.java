package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class AnnonceCreateDTO {

    @NotBlank(message = "title is required")
    @Size(max = 64, message = "title must not exceed 64 characters")
    private String title;

    @NotBlank(message = "description is required")
    @Size(max = 256, message = "description must not exceed 256 characters")
    private String description;

    @NotBlank(message = "adress is required")
    @Size(max = 64, message = "adress must not exceed 64 characters")
    private String adress;

    @NotBlank(message = "mail is required")
    @Email(message = "mail must be a valid email")
    @Size(max = 64, message = "mail must not exceed 64 characters")
    private String mail;

    @NotNull(message = "authorId is required")
    private Long authorId;

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

    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
}
