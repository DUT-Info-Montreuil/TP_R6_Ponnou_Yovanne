package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.controller;

import org.junit.jupiter.api.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static org.mockito.Mockito.*;

/**
 * Niveau 4a – Tests de la LogoutServlet avec mocks HTTP
 */
class LogoutServletTest {

    private LogoutServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        servlet = new LogoutServlet();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
    }

    @Test
    @DisplayName("GET /logout : invalide la session et redirige vers l'accueil")
    void doGet_shouldInvalidateSessionAndRedirect() throws Exception {
        HttpSession session = mock(HttpSession.class);
        when(request.getSession(false)).thenReturn(session);
        when(request.getContextPath()).thenReturn("/masterannonce");

        servlet.doGet(request, response);

        verify(session).invalidate();
        verify(response).sendRedirect("/masterannonce/");
    }

    @Test
    @DisplayName("GET /logout : sans session existante, redirige quand même")
    void doGet_shouldRedirect_whenNoSession() throws Exception {
        when(request.getSession(false)).thenReturn(null);
        when(request.getContextPath()).thenReturn("/masterannonce");

        servlet.doGet(request, response);

        verify(response).sendRedirect("/masterannonce/");
    }
}
