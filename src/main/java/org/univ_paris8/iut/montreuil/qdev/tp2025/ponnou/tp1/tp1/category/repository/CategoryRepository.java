package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.model.Category;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByLabel(String label);

    boolean existsByLabel(String label);
}
