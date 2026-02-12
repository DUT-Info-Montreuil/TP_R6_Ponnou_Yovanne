package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.dao;

import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.dao.GenericDAO;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.model.User;

public class UserDAO extends GenericDAO<User, Long> {

    public UserDAO() {
        super(User.class);
    }
}
