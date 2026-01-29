<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head><title>Liste annonces</title></head>
<body>
<h1>Liste des annonces</h1>

<p><a href="AnnonceAdd">Ajouter une annonce</a></p>

<c:choose>
    <c:when test="${empty annonces}">
        <p>Aucune annonce.</p>
    </c:when>
    <c:otherwise>
        <table border="1" cellpadding="6">
            <tr>
                <th>ID</th><th>Title</th><th>Mail</th><th>Date</th><th>Actions</th>
            </tr>

            <c:forEach var="a" items="${annonces}">
                <tr>
                    <td><c:out value="${a.id}"/></td>
                    <td><c:out value="${a.title}"/></td>
                    <td><c:out value="${a.mail}"/></td>
                    <td><c:out value="${a.date}"/></td>
                    <td>
                        <a href="AnnonceUpdate?id=${a.id}">Modifier</a>
                        |
                        <a href="AnnonceDelete?id=${a.id}" onclick="return confirm('Supprimer ?');">Supprimer</a>
                    </td>
                </tr>
            </c:forEach>
        </table>
    </c:otherwise>
</c:choose>

</body>
</html>
