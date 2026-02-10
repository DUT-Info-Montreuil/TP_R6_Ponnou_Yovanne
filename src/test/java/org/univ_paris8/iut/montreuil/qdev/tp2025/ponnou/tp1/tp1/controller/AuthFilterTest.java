package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.controller;

import org.junit.jupiter.api.*;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.filter.AuthFilter;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.model.User;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static org.mockito.Mockito.*;

/**
 * Niveau 4b – Test du filtre d'authentification AuthFilter
 */
class AuthFilterTest {

    private AuthFilter authFilter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain chain;
    private HttpSession session;

    @BeforeEach
    void setUp() {
        authFilter = new AuthFilter();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        chain = mock(FilterChain.class);
        session = mock(HttpSession.class);
    }

    // ==================== Utilisateur authentifié ====================

    @Test
    @DisplayName("Utilisateur authentifié : le filtre laisse passer la requête")
    void doFilter_shouldContinueChain_whenUserIsLoggedIn() throws Exception {
        User user = new User("alice", "alice@test.com", "password");

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(user);

        authFilter.doFilter(request, response, chain);

        // La chaîne de filtres continue
        verify(chain).doFilter(request, response);
        // Pas de redirection
        verify(response, never()).sendRedirect(anyString());
    }

    // ==================== Utilisateur non authentifié ====================

    @Test
    @DisplayName("Utilisateur non authentifié : redirection vers /login")
    void doFilter_shouldRedirectToLogin_whenNoSession() throws Exception {
        HttpSession newSession = mock(HttpSession.class);

        when(request.getSession(false)).thenReturn(null);
        when(request.getSession()).thenReturn(newSession);
        when(request.getRequestURI()).thenReturn("/masterannonce/annonces/create");
        when(request.getQueryString()).thenReturn(null);
        when(request.getContextPath()).thenReturn("/masterannonce");

        authFilter.doFilter(request, response, chain);

        // La chaîne NE continue PAS
        verify(chain, never()).doFilter(request, response);
        // L'URL demandée est sauvegardée en session
        verify(newSession).setAttribute("redirectAfterLogin", "/masterannonce/annonces/create");
        // Redirection vers login
        verify(response).sendRedirect("/masterannonce/login");
    }

    @Test
    @DisplayName("Utilisateur non authentifié avec query string : l'URL complète est sauvegardée")
    void doFilter_shouldSaveFullUrl_whenQueryStringPresent() throws Exception {
        HttpSession newSession = mock(HttpSession.class);

        when(request.getSession(false)).thenReturn(null);
        when(request.getSession()).thenReturn(newSession);
        when(request.getRequestURI()).thenReturn("/masterannonce/annonce/detail");
        when(request.getQueryString()).thenReturn("id=42");
        when(request.getContextPath()).thenReturn("/masterannonce");

        authFilter.doFilter(request, response, chain);

        verify(newSession).setAttribute("redirectAfterLogin", "/masterannonce/annonce/detail?id=42");
        verify(response).sendRedirect("/masterannonce/login");
    }

    @Test
    @DisplayName("Session existante mais sans utilisateur : redirection vers /login")
    void doFilter_shouldRedirectToLogin_whenSessionExistsButNoUser() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(null);
        when(request.getSession()).thenReturn(session);
        when(request.getRequestURI()).thenReturn("/masterannonce/mes-annonces");
        when(request.getQueryString()).thenReturn(null);
        when(request.getContextPath()).thenReturn("/masterannonce");

        authFilter.doFilter(request, response, chain);

        verify(chain, never()).doFilter(request, response);
        verify(response).sendRedirect("/masterannonce/login");
    }
}
