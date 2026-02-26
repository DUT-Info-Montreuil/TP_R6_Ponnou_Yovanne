package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.repository;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.repository.AnnonceRepository;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.model.Category;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
abstract class CategoryRepositoryTestBase {

    @Autowired
    protected CategoryRepository categoryRepository;

    @Autowired
    protected AnnonceRepository annonceRepository;

    @Autowired
    protected TestEntityManager entityManager;

    protected Category existing;

    @BeforeEach
    void setUpBase() {
        annonceRepository.deleteAll();
        categoryRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
        existing = new Category("Immobilier");
        entityManager.persist(existing);
        entityManager.flush();
    }
}
