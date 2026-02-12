package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.dao;

import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.model.Annonce;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.dao.GenericDAO;

public class AnnonceDAO extends GenericDAO<Annonce, Long> {

    public AnnonceDAO() {
        super(Annonce.class);
    }
}
