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
import java.util.List;

@WebServlet("/mes-annonces")
public class MesAnnoncesServlet extends HttpServlet {

    private static final int PAGE_SIZE = 10;

    private final AnnonceService annonceService = new AnnonceService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("user");

        int page = 0;
        String pageParam = request.getParameter("page");
        if (pageParam != null) {
            try {
                page = Math.max(0, Integer.parseInt(pageParam));
            } catch (NumberFormatException ignored) {
            }
        }

        List<Annonce> annonces = annonceService.findByAuthorPaginated(currentUser.getId(), page, PAGE_SIZE);
        long totalCount = annonceService.countByAuthor(currentUser.getId());
        int totalPages = (int) Math.ceil((double) totalCount / PAGE_SIZE);

        request.setAttribute("annonces", annonces);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalCount", totalCount);

        request.getRequestDispatcher("/WEB-INF/views/mes-annonces.jsp").forward(request, response);
    }
}
