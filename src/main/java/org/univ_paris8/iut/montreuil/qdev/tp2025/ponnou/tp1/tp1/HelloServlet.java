package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet(name = "helloServlet", urlPatterns = "/hello")
public class HelloServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/hello.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String nom = request.getParameter("nom");
        if (nom == null) nom = "";
        request.setAttribute("nom", nom.trim());
        request.getRequestDispatcher("/hello.jsp").forward(request, response);
    }
}
