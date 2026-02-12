package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.dto;

/**
 * DTO de requete pour la creation d'une annonce (POST).
 */
public class AnnonceCreateDTO {

    private String title;
    private String description;
    private String adress;
    private String mail;
    private Long authorId;
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
