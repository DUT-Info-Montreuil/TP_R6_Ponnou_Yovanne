<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Hello</title>
</head>
<body>

<h1>
    Hello the World
    <c:if test="${not empty nom}">
        <c:out value=" ${nom}"/>
    </c:if>
</h1>

<form method="post" action="${pageContext.request.contextPath}/hello">
    <label>Votre nom :</label>
    <input type="text" name="nom" required>
    <button type="submit">Envoyer</button>
</form>

<form method="get" action="${pageContext.request.contextPath}/AnnonceAdd" style="display:inline;">
    <button type="submit">Ajouter une annonce</button>
</form>
<form method="get" action="${pageContext.request.contextPath}/AnnonceList" style="display:inline;">
    <button type="submit">Voir la liste des annonces</button>
</form>

</body>
</html>
