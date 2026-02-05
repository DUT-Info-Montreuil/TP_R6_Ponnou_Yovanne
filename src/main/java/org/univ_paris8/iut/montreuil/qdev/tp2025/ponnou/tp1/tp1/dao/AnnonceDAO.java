package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.dao;

import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.model.Annonce;

public class AnnonceDAO extends GenericDAO<Annonce, Long> {

    public AnnonceDAO() {
        super(Annonce.class);
    }
}
