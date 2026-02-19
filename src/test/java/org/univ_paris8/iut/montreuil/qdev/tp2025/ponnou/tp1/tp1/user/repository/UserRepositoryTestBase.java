package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.repository;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.model.User;

@DataJpaTest
abstract class UserRepositoryTestBase {

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected TestEntityManager entityManager;

    protected User existing;

    @BeforeEach
    void setUpBase() {
        existing = new User("alice", "alice@test.com", "password123");
        entityManager.persist(existing);
        entityManager.flush();
    }
}
