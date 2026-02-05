package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.controller;

import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.model.Annonce;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.model.User;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.service.AnnonceService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Optional;


@WebServlet("/annonces/delete")
public class AnnonceDeleteServlet extends HttpServlet {

    private final AnnonceService annonceService = new AnnonceService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String idParam = request.getParameter("id");
        if (idParam == null || idParam.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/mes-annonces");
            return;
        }

        Long id;
        try {
            id = Long.parseLong(idParam);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/mes-annonces");
            return;
        }

        Optional<Annonce> annonceOpt = annonceService.findById(id);
        if (annonceOpt.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/mes-annonces");
            return;
        }

        Annonce annonce = annonceOpt.get();

        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("user");
        if (!currentUser.getId().equals(annonce.getAuthor().getId())) {
            response.sendRedirect(request.getContextPath() + "/mes-annonces");
            return;
        }

        annonceService.delete(id);

        response.sendRedirect(request.getContextPath() + "/mes-annonces");
    }
}
