package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.controller;

import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.service.AnnonceService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/AnnonceDelete")
public class AnnonceDelete extends HttpServlet {

    private final AnnonceService annonceService = new AnnonceService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Long id = Long.parseLong(request.getParameter("id"));
            annonceService.delete(id);
            response.sendRedirect(request.getContextPath() + "/AnnonceList");
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/AnnonceList");
        }
    }
}
