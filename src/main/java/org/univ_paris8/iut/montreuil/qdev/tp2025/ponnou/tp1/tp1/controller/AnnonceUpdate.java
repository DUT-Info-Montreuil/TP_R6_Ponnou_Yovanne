package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.controller;

import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.model.Annonce;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.model.Category;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.service.AnnonceService;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.service.CategoryService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@WebServlet("/AnnonceUpdate")
public class AnnonceUpdate extends HttpServlet {

    private final AnnonceService annonceService = new AnnonceService();
    private final CategoryService categoryService = new CategoryService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Long id = Long.parseLong(request.getParameter("id"));
            Optional<Annonce> annonceOpt = annonceService.findById(id);

            if (annonceOpt.isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/AnnonceList");
                return;
            }

            List<Category> categories = categoryService.findAll();
            request.setAttribute("annonce", annonceOpt.get());
            request.setAttribute("categories", categories);
            request.getRequestDispatcher("/AnnonceUpdate.jsp").forward(request, response);

        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/AnnonceList");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        try {
            Long id = Long.parseLong(request.getParameter("id"));

            String title = trim(request.getParameter("title"));
            String description = trim(request.getParameter("description"));
            String adress = trim(request.getParameter("adress"));
            String mail = trim(request.getParameter("mail"));
            String categoryIdParam = request.getParameter("categoryId");

            if (title.isEmpty() || description.isEmpty() || adress.isEmpty() || mail.isEmpty() || categoryIdParam == null || categoryIdParam.isEmpty()) {
                request.setAttribute("error", "Tous les champs sont obligatoires.");
                annonceService.findById(id).ifPresent(a -> request.setAttribute("annonce", a));
                request.setAttribute("categories", categoryService.findAll());
                request.getRequestDispatcher("/AnnonceUpdate.jsp").forward(request, response);
                return;
            }

            Long categoryId = Long.parseLong(categoryIdParam);
            annonceService.update(id, title, description, adress, mail, categoryId);

            response.sendRedirect(request.getContextPath() + "/AnnonceList");

        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/AnnonceList");
        } catch (IllegalArgumentException e) {
            request.setAttribute("error", e.getMessage());
            request.setAttribute("categories", categoryService.findAll());
            request.getRequestDispatcher("/AnnonceUpdate.jsp").forward(request, response);
        }
    }

    private String trim(String s) {
        return (s == null) ? "" : s.trim();
    }
}
