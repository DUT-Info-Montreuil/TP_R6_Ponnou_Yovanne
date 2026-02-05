package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.dao;

import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.model.User;

public class UserDAO extends GenericDAO<User, Long> {

    public UserDAO() {
        super(User.class);
    }
}
