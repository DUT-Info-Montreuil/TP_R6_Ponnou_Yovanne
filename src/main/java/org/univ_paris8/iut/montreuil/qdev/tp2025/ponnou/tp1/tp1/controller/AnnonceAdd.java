package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.controller;

import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.model.Category;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.model.User;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.service.AnnonceService;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.service.CategoryService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet("/AnnonceAdd")
public class AnnonceAdd extends HttpServlet {

    private final AnnonceService annonceService = new AnnonceService();
    private final CategoryService categoryService = new CategoryService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Category> categories = categoryService.findAll();
        request.setAttribute("categories", categories);
        request.getRequestDispatcher("/AnnonceAdd.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("user");

        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String title = trim(request.getParameter("title"));
        String description = trim(request.getParameter("description"));
        String adress = trim(request.getParameter("adress"));
        String mail = trim(request.getParameter("mail"));
        String categoryIdParam = request.getParameter("categoryId");

        if (title.isEmpty() || description.isEmpty() || adress.isEmpty() || mail.isEmpty() || categoryIdParam == null || categoryIdParam.isEmpty()) {
            request.setAttribute("error", "Tous les champs sont obligatoires.");
            request.setAttribute("categories", categoryService.findAll());
            request.getRequestDispatcher("/AnnonceAdd.jsp").forward(request, response);
            return;
        }

        try {
            Long categoryId = Long.parseLong(categoryIdParam);
            annonceService.create(title, description, adress, mail, currentUser.getId(), categoryId);
            response.sendRedirect(request.getContextPath() + "/AnnonceList");
        } catch (NumberFormatException e) {
            request.setAttribute("error", "Catégorie invalide.");
            request.setAttribute("categories", categoryService.findAll());
            request.getRequestDispatcher("/AnnonceAdd.jsp").forward(request, response);
        } catch (IllegalArgumentException e) {
            request.setAttribute("error", e.getMessage());
            request.setAttribute("categories", categoryService.findAll());
            request.getRequestDispatcher("/AnnonceAdd.jsp").forward(request, response);
        }
    }

    private String trim(String s) {
        return (s == null) ? "" : s.trim();
    }
}
