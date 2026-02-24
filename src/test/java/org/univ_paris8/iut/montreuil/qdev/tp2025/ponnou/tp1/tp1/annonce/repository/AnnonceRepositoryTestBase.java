package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.repository;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.model.Annonce;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.model.AnnonceStatus;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.model.Category;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.model.User;

import java.sql.Timestamp;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
abstract class AnnonceRepositoryTestBase {

    @Autowired
    protected AnnonceRepository annonceRepository;

    @Autowired
    protected TestEntityManager entityManager;

    protected User testUser;
    protected User otherUser;
    protected Category testCategory;
    protected Category otherCategory;

    @BeforeEach
    void initBaseData() {
        testUser = persistUser("author", "author@test.com");
        otherUser = persistUser("other", "other@test.com");
        testCategory = persistCategory("Immobilier");
        otherCategory = persistCategory("Vehicules");
    }

    protected User persistUser(String username, String email) {
        User user = new User(username, email, "password");
        entityManager.persist(user);
        entityManager.flush();
        return user;
    }

    protected Category persistCategory(String label) {
        Category category = new Category(label);
        entityManager.persist(category);
        entityManager.flush();
        return category;
    }

    protected Annonce persistAnnonce(String title,
                                     String description,
                                     AnnonceStatus status,
                                     Timestamp timestamp,
                                     User user,
                                     Category category) {
        Annonce annonce = new Annonce(title, description, "Paris", "contact@test.com");
        annonce.setStatus(status);
        annonce.setAuthor(user);
        annonce.setCategory(category);

        entityManager.persist(annonce);
        entityManager.flush();

        entityManager.getEntityManager()
                .createQuery("UPDATE Annonce a SET a.date = :date WHERE a.id = :id")
                .setParameter("date", timestamp)
                .setParameter("id", annonce.getId())
                .executeUpdate();
        entityManager.flush();
        entityManager.clear();

        return annonceRepository.findById(annonce.getId()).orElseThrow();
    }
}
