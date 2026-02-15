package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.repository;

import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.model.Annonce;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.repository.GenericRepository;

public class AnnonceRepository extends GenericRepository<Annonce, Long> {

    public AnnonceRepository() {
        super(Annonce.class);
    }
}
