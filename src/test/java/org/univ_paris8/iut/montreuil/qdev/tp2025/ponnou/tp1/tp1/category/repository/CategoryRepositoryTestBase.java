package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.repository;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.model.Category;

@DataJpaTest
abstract class CategoryRepositoryTestBase {

    @Autowired
    protected CategoryRepository categoryRepository;

    @Autowired
    protected TestEntityManager entityManager;

    protected Category existing;

    @BeforeEach
    void setUpBase() {
        existing = new Category("Immobilier");
        entityManager.persist(existing);
        entityManager.flush();
    }
}
