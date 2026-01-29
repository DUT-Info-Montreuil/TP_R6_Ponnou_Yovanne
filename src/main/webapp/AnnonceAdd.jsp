<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head><title>Ajouter annonce</title></head>
<body>
<h1>Ajouter une annonce</h1>

<c:if test="${not empty error}">
    <p style="color:red"><c:out value="${error}"/></p>
</c:if>

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
