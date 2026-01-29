package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.controller;

import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.dao.AnnonceDAO;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.model.Annonce;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/AnnonceAdd")
public class AnnonceAdd extends HttpServlet {

    private final AnnonceDAO dao = new AnnonceDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/AnnonceAdd.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String title = trim(request.getParameter("title"));
        String description = trim(request.getParameter("description"));
        String adress = trim(request.getParameter("adress"));
        String mail = trim(request.getParameter("mail"));

        if (title.isEmpty() || description.isEmpty() || adress.isEmpty() || mail.isEmpty()) {
            request.setAttribute("error", "Tous les champs sont obligatoires.");
            request.getRequestDispatcher("/AnnonceAdd.jsp").forward(request, response);
            return;
        }

        try {
            Annonce a = new Annonce();
            a.setTitle(title);
            a.setDescription(description);
            a.setAdress(adress);
            a.setMail(mail);

            dao.create(a);
            response.sendRedirect(request.getContextPath() + "/AnnonceList");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private String trim(String s) {
        return (s == null) ? "" : s.trim();
    }
}
