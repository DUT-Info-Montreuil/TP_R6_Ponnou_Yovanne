package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.controller;

import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.model.Annonce;
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

@WebServlet("/annonces/create")
public class AnnonceCreateServlet extends HttpServlet {

    private final AnnonceService annonceService = new AnnonceService();
    private final CategoryService categoryService = new CategoryService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        List<Category> categories = categoryService.findAll();
        request.setAttribute("categories", categories);

        request.getRequestDispatcher("/WEB-INF/views/annonce-form.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("user");

        String title = trim(request.getParameter("title"));
        String description = trim(request.getParameter("description"));
        String adress = trim(request.getParameter("adress"));
        String mail = trim(request.getParameter("mail"));
        String categoryIdParam = request.getParameter("categoryId");

        // Conserver les valeurs saisies
        request.setAttribute("title", title);
        request.setAttribute("description", description);
        request.setAttribute("adress", adress);
        request.setAttribute("mail", mail);
        request.setAttribute("selectedCategoryId", categoryIdParam);

        StringBuilder errors = new StringBuilder();

        if (title.isEmpty()) {
            errors.append("Le titre est obligatoire.<br>");
        } else if (title.length() > 64) {
            errors.append("Le titre ne doit pas dépasser 64 caractères.<br>");
        }

        if (description.isEmpty()) {
            errors.append("La description est obligatoire.<br>");
        } else if (description.length() > 256) {
            errors.append("La description ne doit pas dépasser 256 caractères.<br>");
        }

        if (adress.isEmpty()) {
            errors.append("L'adresse est obligatoire.<br>");
        } else if (adress.length() > 64) {
            errors.append("L'adresse ne doit pas dépasser 64 caractères.<br>");
        }

        if (mail.isEmpty()) {
            errors.append("L'email est obligatoire.<br>");
        } else if (!mail.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            errors.append("L'email n'est pas valide.<br>");
        } else if (mail.length() > 64) {
            errors.append("L'email ne doit pas dépasser 64 caractères.<br>");
        }

        Long categoryId = null;
        if (categoryIdParam == null || categoryIdParam.isEmpty()) {
            errors.append("La catégorie est obligatoire.<br>");
        } else {
            try {
                categoryId = Long.parseLong(categoryIdParam);
            } catch (NumberFormatException e) {
                errors.append("Catégorie invalide.<br>");
            }
        }

        if (errors.length() > 0) {
            List<Category> categories = categoryService.findAll();
            request.setAttribute("categories", categories);
            request.setAttribute("error", errors.toString());
            request.getRequestDispatcher("/WEB-INF/views/annonce-form.jsp").forward(request, response);
            return;
        }

        try {
            Annonce annonce = annonceService.create(title, description, adress, mail,
                    currentUser.getId(), categoryId);

            response.sendRedirect(request.getContextPath() + "/annonce/detail?id=" + annonce.getId());

        } catch (IllegalArgumentException e) {
            List<Category> categories = categoryService.findAll();
            request.setAttribute("categories", categories);
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/annonce-form.jsp").forward(request, response);
        }
    }

    private String trim(String s) {
        return (s == null) ? "" : s.trim();
    }
}
