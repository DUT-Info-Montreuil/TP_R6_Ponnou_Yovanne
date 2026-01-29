package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.model;

import java.sql.Timestamp;

public class Annonce {
    private int id;
    private String title;
    private String description;
    private String adress;
    private String mail;
    private Timestamp date;

    public Annonce() {}

    // getters/setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAdress() { return adress; }
    public void setAdress(String adress) { this.adress = adress; }

    public String getMail() { return mail; }
    public void setMail(String mail) { this.mail = mail; }

    public Timestamp getDate() { return date; }
    public void setDate(Timestamp date) { this.date = date; }
}
