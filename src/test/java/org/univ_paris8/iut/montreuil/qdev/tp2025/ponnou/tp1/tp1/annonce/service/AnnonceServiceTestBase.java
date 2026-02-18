package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.model.Annonce;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.model.AnnonceStatus;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.repository.AnnonceRepository;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.model.Category;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.repository.CategoryRepository;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.config.EntityManagerUtil;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.model.User;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.repository.UserRepository;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
abstract class AnnonceServiceTestBase {

    @Mock protected AnnonceRepository annonceRepository;
    @Mock protected UserRepository userRepository;
    @Mock protected CategoryRepository categoryRepository;
    @Mock protected EntityManager em;
    @Mock protected EntityTransaction tx;

    protected AnnonceService annonceService;
    protected MockedStatic<EntityManagerUtil> mockedUtil;

    protected User testUser;
    protected Category testCategory;

    @BeforeEach
    void setUp() {
        annonceService = new AnnonceService(annonceRepository, userRepository, categoryRepository);

        mockedUtil = mockStatic(EntityManagerUtil.class);
        mockedUtil.when(EntityManagerUtil::getEntityManager).thenReturn(em);
        lenient().when(em.getTransaction()).thenReturn(tx);
        lenient().when(tx.isActive()).thenReturn(true);

        testUser = new User("author", "author@test.com", "password123");
        testUser.setId(1L);

        testCategory = new Category("Immobilier");
        testCategory.setId(10L);
    }

    @AfterEach
    void tearDown() {
        mockedUtil.close();
    }

    protected Annonce buildAnnonce(Long id, AnnonceStatus status) {
        Annonce annonce = new Annonce("Titre", "Desc", "Paris", "m@t.com");
        annonce.setId(id);
        annonce.setStatus(status);
        annonce.setAuthor(testUser);
        annonce.setCategory(testCategory);
        return annonce;
    }
}
