package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.mapper.UserMapper;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
abstract class UserServiceTestBase {

    @Mock
    protected UserRepository userRepository;

    @Mock
    protected UserMapper userMapper;

    protected final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    protected UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, userMapper, passwordEncoder);
    }
}
