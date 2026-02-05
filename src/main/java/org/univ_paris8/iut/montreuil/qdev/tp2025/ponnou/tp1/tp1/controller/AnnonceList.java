package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.controller;

import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.repository.AnnonceRepository;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/AnnonceList")
public class AnnonceList extends HttpServlet {

    private final AnnonceRepository annonceRepository = new AnnonceRepository();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("annonces", annonceRepository.findAll());
        request.getRequestDispatcher("/AnnonceList.jsp").forward(request, response);
    }
}
