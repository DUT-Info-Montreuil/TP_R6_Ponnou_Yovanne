package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.dto;

import java.sql.Timestamp;

public class AnnonceDTO {

    private Long id;
    private String title;
    private String description;
    private String adress;
    private String mail;
    private Timestamp date;
    private String status;
    private String authorUsername;
    private String categoryLabel;

    public AnnonceDTO() {
    }

    private AnnonceDTO(Builder builder) {
        this.id = builder.id;
        this.title = builder.title;
        this.description = builder.description;
        this.adress = builder.adress;
        this.mail = builder.mail;
        this.date = builder.date;
        this.status = builder.status;
        this.authorUsername = builder.authorUsername;
        this.categoryLabel = builder.categoryLabel;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getAdress() { return adress; }
    public String getMail() { return mail; }
    public Timestamp getDate() { return date; }
    public String getStatus() { return status; }
    public String getAuthorUsername() { return authorUsername; }
    public String getCategoryLabel() { return categoryLabel; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String title;
        private String description;
        private String adress;
        private String mail;
        private Timestamp date;
        private String status;
        private String authorUsername;
        private String categoryLabel;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder adress(String adress) { this.adress = adress; return this; }
        public Builder mail(String mail) { this.mail = mail; return this; }
        public Builder date(Timestamp date) { this.date = date; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder authorUsername(String authorUsername) { this.authorUsername = authorUsername; return this; }
        public Builder categoryLabel(String categoryLabel) { this.categoryLabel = categoryLabel; return this; }

        public AnnonceDTO build() {
            return new AnnonceDTO(this);
        }
    }
}
