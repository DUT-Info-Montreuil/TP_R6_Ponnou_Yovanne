<%--
  Created by IntelliJ IDEA.
  User: yponnou
  Date: 29/01/2026
  Time: 14:23
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Hello</title>
</head>
<body>

<h1>
    Hello the World
    <%
        String nom = (String) request.getAttribute("nom");
        if (nom != null && !nom.isBlank()) {
            out.print(" " + nom);
        }
    %>
</h1>

<form method="post" action="<%= request.getContextPath() %>/hello">
    <label>Votre nom :</label>
    <input type="text" name="nom" required>
    <button type="submit">Envoyer</button>
</form>

</body>
</html>

