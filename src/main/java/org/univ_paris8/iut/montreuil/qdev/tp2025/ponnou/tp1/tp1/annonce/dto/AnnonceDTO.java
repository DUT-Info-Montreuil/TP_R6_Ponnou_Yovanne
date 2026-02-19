package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
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
}
