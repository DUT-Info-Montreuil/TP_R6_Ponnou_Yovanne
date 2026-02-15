package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.repository;

import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.repository.GenericRepository;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.model.Category;

public class CategoryRepository extends GenericRepository<Category, Long> {

    public CategoryRepository() {
        super(Category.class);
    }
}
