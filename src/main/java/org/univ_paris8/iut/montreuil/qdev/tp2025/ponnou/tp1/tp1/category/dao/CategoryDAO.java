package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.dao;

import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.dao.GenericDAO;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.model.Category;

public class CategoryDAO extends GenericDAO<Category, Long> {

    public CategoryDAO() {
        super(Category.class);
    }
}
