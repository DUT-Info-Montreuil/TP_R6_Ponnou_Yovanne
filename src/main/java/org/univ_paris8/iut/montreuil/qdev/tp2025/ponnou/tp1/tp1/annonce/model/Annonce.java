package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.model;

import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.model.Category;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.model.User;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@Entity
@Table(name = "annonces")
public class Annonce {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 64, message = "Le titre ne doit pas dépasser 64 caractères")
    @Column(nullable = false, length = 64)
    private String title;

    @NotBlank(message = "La description est obligatoire")
    @Size(max = 256, message = "La description ne doit pas dépasser 256 caractères")
    @Column(nullable = false, length = 256)
    private String description;

    @NotBlank(message = "L'adresse est obligatoire")
    @Size(max = 64, message = "L'adresse ne doit pas dépasser 64 caractères")
    @Column(nullable = false, length = 64)
    private String adress;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
    @Size(max = 64, message = "L'email ne doit pas dépasser 64 caractères")
    @Column(nullable = false, length = 64)
    private String mail;

    @Column(name = "date", nullable = false, updatable = false)
    private Timestamp date;

    @NotNull(message = "Le statut est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AnnonceStatus status = AnnonceStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    public Annonce() {
    }

    public Annonce(String title, String description, String adress, String mail) {
        this.title = title;
        this.description = description;
        this.adress = adress;
        this.mail = mail;
        this.status = AnnonceStatus.DRAFT;
    }

    @PrePersist
    protected void onCreate() {
        this.date = new Timestamp(System.currentTimeMillis());
        if (this.status == null) {
            this.status = AnnonceStatus.DRAFT;
        }
    }
}
