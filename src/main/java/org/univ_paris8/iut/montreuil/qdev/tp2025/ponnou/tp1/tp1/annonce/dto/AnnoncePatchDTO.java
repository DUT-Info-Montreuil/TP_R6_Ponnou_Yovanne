package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public class AnnoncePatchDTO {

    @Size(max = 64, message = "title must not exceed 64 characters")
    private String title;

    @Size(max = 256, message = "description must not exceed 256 characters")
    private String description;

    @Size(max = 64, message = "adress must not exceed 64 characters")
    private String adress;

    @Email(message = "mail must be a valid email")
    @Size(max = 64, message = "mail must not exceed 64 characters")
    private String mail;

    private Long categoryId;

    public AnnoncePatchDTO() {
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
