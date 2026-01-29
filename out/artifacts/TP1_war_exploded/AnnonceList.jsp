<%--
  Created by IntelliJ IDEA.
  User: yponnou
  Date: 29/01/2026
  Time: 15:09
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.model.Annonce" %>
<!DOCTYPE html>
<html>
<head><title>Liste annonces</title></head>
<body>
<h1>Liste des annonces</h1>

<p><a href="AnnonceAdd">Ajouter une annonce</a></p>

<%
    List<Annonce> annonces = (List<Annonce>) request.getAttribute("annonces");
    if (annonces == null || annonces.isEmpty()) {
        out.print("<p>Aucune annonce.</p>");
    } else {
%>
<table border="1" cellpadding="6">
    <tr>
        <th>ID</th><th>Title</th><th>Mail</th><th>Date</th><th>Actions</th>
    </tr>
    <%
        for (Annonce a : annonces) {
    %>
    <tr>
        <td><%= a.getId() %></td>
        <td><%= a.getTitle() %></td>
        <td><%= a.getMail() %></td>
        <td><%= a.getDate() %></td>
        <td>
            <a href="AnnonceUpdate?id=<%= a.getId() %>">Modifier</a>
            |
            <a href="AnnonceDelete?id=<%= a.getId() %>" onclick="return confirm('Supprimer ?');">Supprimer</a>
        </td>
    </tr>
    <%
        }
    %>
</table>
<%
    }
%>
</body>
</html>
