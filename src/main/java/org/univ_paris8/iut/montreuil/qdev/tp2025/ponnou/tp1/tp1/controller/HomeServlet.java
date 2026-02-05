package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.controller;

import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.model.Annonce;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.model.Category;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.service.AnnonceService;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.service.CategoryService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;


@WebServlet(urlPatterns = {"/home", ""})
public class HomeServlet extends HttpServlet {

    private static final int PAGE_SIZE = 10;

    private final AnnonceService annonceService = new AnnonceService();
    private final CategoryService categoryService = new CategoryService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int page = 0;
        String pageParam = request.getParameter("page");
        if (pageParam != null) {
            try {
                page = Math.max(0, Integer.parseInt(pageParam));
            } catch (NumberFormatException ignored) {
            }
        }

        String keyword = request.getParameter("keyword");
        String categoryIdParam = request.getParameter("category");
        Long categoryId = null;
        if (categoryIdParam != null && !categoryIdParam.isEmpty()) {
            try {
                categoryId = Long.parseLong(categoryIdParam);
            } catch (NumberFormatException ignored) {
            }
        }

        List<Annonce> annonces;
        long totalCount;

        if (keyword != null && !keyword.trim().isEmpty()) {
            // key word
            annonces = annonceService.searchByKeywordPaginated(keyword.trim(), page, PAGE_SIZE);
            totalCount = annonceService.countByKeyword(keyword.trim());
            request.setAttribute("keyword", keyword.trim());
        } else if (categoryId != null) {
            // filtre categorie
            annonces = annonceService.findByCategoryPaginated(categoryId, page, PAGE_SIZE);
            totalCount = annonceService.countByCategory(categoryId);
            request.setAttribute("selectedCategory", categoryId);
        } else {
            annonces = annonceService.findPublishedPaginated(page, PAGE_SIZE);
            totalCount = annonceService.countPublished();
        }

        int totalPages = (int) Math.ceil((double) totalCount / PAGE_SIZE);

        List<Category> categories = categoryService.findAll();

        request.setAttribute("annonces", annonces);
        request.setAttribute("categories", categories);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalCount", totalCount);

        request.getRequestDispatcher("/WEB-INF/views/home.jsp").forward(request, response);
    }
}
