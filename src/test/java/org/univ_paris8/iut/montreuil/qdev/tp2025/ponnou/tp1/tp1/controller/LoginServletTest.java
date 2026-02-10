package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.controller;

import org.junit.jupiter.api.*;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.model.User;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static org.mockito.Mockito.*;

/**
 * Niveau 4a – Tests de la LoginServlet avec mocks HTTP
 */
class LoginServletTest {

    private LoginServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        servlet = new LoginServlet();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        dispatcher = mock(RequestDispatcher.class);
    }

    // ==================== Tests GET ====================

    @Test
    @DisplayName("GET /login : affiche la page de connexion si non connecté")
    void doGet_shouldForwardToLoginPage_whenNotLoggedIn() throws Exception {
        when(request.getSession(false)).thenReturn(null);
        when(request.getRequestDispatcher("/WEB-INF/views/login.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(dispatcher).forward(request, response);
        verify(response, never()).sendRedirect(anyString());
    }

    @Test
    @DisplayName("GET /login : redirige vers l'accueil si déjà connecté")
    void doGet_shouldRedirectToHome_whenAlreadyLoggedIn() throws Exception {
        User user = new User("alice", "alice@test.com", "password");

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(user);
        when(request.getContextPath()).thenReturn("/masterannonce");

        servlet.doGet(request, response);

        verify(response).sendRedirect("/masterannonce/");
        verify(dispatcher, never()).forward(any(), any());
    }

    // ==================== Tests POST ====================

    @Test
    @DisplayName("POST /login : champs vides → erreur affichée")
    void doPost_shouldShowError_whenFieldsEmpty() throws Exception {
        when(request.getParameter("username")).thenReturn("");
        when(request.getParameter("password")).thenReturn("");
        when(request.getRequestDispatcher("/WEB-INF/views/login.jsp")).thenReturn(dispatcher);

        servlet.doPost(request, response);

        verify(request).setAttribute(eq("error"), contains("Veuillez remplir"));
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("POST /login : username null est traité comme vide")
    void doPost_shouldHandleNullUsername() throws Exception {
        when(request.getParameter("username")).thenReturn(null);
        when(request.getParameter("password")).thenReturn("password");
        when(request.getRequestDispatcher("/WEB-INF/views/login.jsp")).thenReturn(dispatcher);

        servlet.doPost(request, response);

        verify(request).setAttribute(eq("error"), contains("Veuillez remplir"));
        verify(dispatcher).forward(request, response);
    }
}
