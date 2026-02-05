package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.controller;

import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.service.AnnonceService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/AnnonceList")
public class AnnonceList extends HttpServlet {

    private static final int PAGE_SIZE = 10;
    private final AnnonceService annonceService = new AnnonceService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int page = 1;
        String pageParam = request.getParameter("page");
        if (pageParam != null) {
            try {
                page = Integer.parseInt(pageParam);
                if (page < 1) page = 1;
            } catch (NumberFormatException ignored) {
            }
        }

        long totalAnnonces = annonceService.count();
        int totalPages = (int) Math.ceil((double) totalAnnonces / PAGE_SIZE);
        if (totalPages < 1) totalPages = 1;
        if (page > totalPages) page = totalPages;

        request.setAttribute("annonces", annonceService.findAllPaginated(page, PAGE_SIZE));
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("pageSize", PAGE_SIZE);
        request.setAttribute("totalAnnonces", totalAnnonces);

        request.getRequestDispatcher("/AnnonceList.jsp").forward(request, response);
    }
}
