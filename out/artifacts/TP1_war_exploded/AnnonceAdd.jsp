<%--
  Created by IntelliJ IDEA.
  User: yponnou
  Date: 29/01/2026
  Time: 15:00
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head><title>Ajouter annonce</title></head>
<body>
<h1>Ajouter une annonce</h1>

<%
    String error = (String) request.getAttribute("error");
    if (error != null) { out.print("<p style='color:red'>" + error + "</p>"); }
%>

<form method="post" action="AnnonceAdd">
    <label>Title:</label><br/>
    <input type="text" name="title"/><br/><br/>

    <label>Description:</label><br/>
    <textarea name="description" rows="4" cols="50"></textarea><br/><br/>

    <label>Adress:</label><br/>
    <input type="text" name="adress"/><br/><br/>

    <label>Mail:</label><br/>
    <input type="email" name="mail"/><br/><br/>

    <button type="submit">Enregistrer</button>
</form>

<p><a href="AnnonceList">Voir la liste</a></p>
</body>
</html>
