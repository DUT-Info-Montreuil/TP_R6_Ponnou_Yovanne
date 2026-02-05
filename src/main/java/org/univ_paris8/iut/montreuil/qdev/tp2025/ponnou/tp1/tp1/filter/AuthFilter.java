package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Filtre pour protéger les pages nécessitant une authentification
 */
@WebFilter(urlPatterns = {"/annonces/*", "/mes-annonces", "/annonce/*"})
public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        HttpSession session = httpRequest.getSession(false);
        boolean isLoggedIn = (session != null && session.getAttribute("user") != null);

        if (isLoggedIn) {
            chain.doFilter(request, response);
        } else {
            String requestURI = httpRequest.getRequestURI();
            String queryString = httpRequest.getQueryString();
            String fullURL = queryString != null ? requestURI + "?" + queryString : requestURI;

            httpRequest.getSession().setAttribute("redirectAfterLogin", fullURL);
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login");
        }
    }

    @Override
    public void destroy() {
    }
}
