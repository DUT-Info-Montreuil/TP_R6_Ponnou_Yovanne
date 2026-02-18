package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.repository.AnnonceRepository;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.repository.CategoryRepository;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.config.EntityManagerUtil;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
abstract class CategoryServiceTestBase {

    @Mock protected CategoryRepository categoryRepository;
    @Mock protected AnnonceRepository annonceRepository;
    @Mock protected EntityManager em;
    @Mock protected EntityTransaction tx;

    protected CategoryService categoryService;
    protected MockedStatic<EntityManagerUtil> mockedUtil;

    @BeforeEach
    void setUp() {
        categoryService = new CategoryService(categoryRepository, annonceRepository);

        mockedUtil = mockStatic(EntityManagerUtil.class);
        mockedUtil.when(EntityManagerUtil::getEntityManager).thenReturn(em);
        lenient().when(em.getTransaction()).thenReturn(tx);
    }

    @AfterEach
    void tearDown() {
        mockedUtil.close();
    }
}
