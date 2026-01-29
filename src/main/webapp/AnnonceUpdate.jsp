<%--
  Created by IntelliJ IDEA.
  User: yponnou
  Date: 29/01/2026
  Time: 15:09
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.model.Annonce" %>
<!DOCTYPE html>
<html>
<head><title>Modifier annonce</title></head>
<body>
<h1>Modifier une annonce</h1>

<%
    String error = (String) request.getAttribute("error");
    if (error != null) out.print("<p style='color:red'>" + error + "</p>");

    Annonce a = (Annonce) request.getAttribute("annonce");
    if (a == null) { out.print("<p>Annonce introuvable.</p>"); return; }
%>

<form method="post" action="AnnonceUpdate">
    <input type="hidden" name="id" value="<%= a.getId() %>"/>

    <label>Title:</label><br/>
    <input type="text" name="title" value="<%= a.getTitle() %>"/><br/><br/>

    <label>Description:</label><br/>
    <textarea name="description" rows="4" cols="50"><%= a.getDescription() %></textarea><br/><br/>

    <label>Adress:</label><br/>
    <input type="text" name="adress" value="<%= a.getAdress() %>"/><br/><br/>

    <label>Mail:</label><br/>
    <input type="email" name="mail" value="<%= a.getMail() %>"/><br/><br/>

    <button type="submit">Mettre à jour</button>
</form>

<p><a href="AnnonceList">Retour liste</a></p>
</body>
</html>
