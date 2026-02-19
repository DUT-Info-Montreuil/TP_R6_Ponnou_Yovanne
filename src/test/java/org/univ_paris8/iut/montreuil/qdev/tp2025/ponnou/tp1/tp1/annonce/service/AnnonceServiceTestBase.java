package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.mappers.AnnonceMapper;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.repository.AnnonceRepository;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.repository.CategoryRepository;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
abstract class AnnonceServiceTestBase {

    @Mock
    protected AnnonceRepository annonceRepository;

    @Mock
    protected UserRepository userRepository;

    @Mock
    protected CategoryRepository categoryRepository;

    @Mock
    protected AnnonceMapper annonceMapper;

    protected AnnonceService annonceService;

    @BeforeEach
    void setUpService() {
        annonceService = new AnnonceService(annonceRepository, userRepository, categoryRepository, annonceMapper);
    }
}
