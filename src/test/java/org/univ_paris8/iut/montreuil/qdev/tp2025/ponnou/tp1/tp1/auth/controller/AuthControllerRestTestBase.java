package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.security.JwtAuthenticationFilter;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.security.JwtService;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.service.RefreshTokenService;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.exception.GlobalExceptionHandler;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.service.UserService;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
abstract class AuthControllerRestTestBase {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockitoBean
    protected UserService userService;

    @MockitoBean
    protected JwtService jwtService;

    @MockitoBean
    protected RefreshTokenService refreshTokenService;

    @MockitoBean
    protected JwtAuthenticationFilter jwtAuthenticationFilter;
}
