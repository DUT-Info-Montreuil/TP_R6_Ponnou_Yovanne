<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head><title>Modifier annonce</title></head>
<body>
<h1>Modifier une annonce</h1>

<c:if test="${not empty error}">
    <p style="color:red"><c:out value="${error}"/></p>
</c:if>

<c:choose>
    <c:when test="${empty annonce}">
        <p>Annonce introuvable.</p>
    </c:when>
    <c:otherwise>
        <form method="post" action="AnnonceUpdate">
            <input type="hidden" name="id" value="${annonce.id}"/>

            <label>Title:</label><br/>
            <input type="text" name="title" value="<c:out value='${annonce.title}'/>"/><br/><br/>

            <label>Description:</label><br/>
            <textarea name="description" rows="4" cols="50"><c:out value="${annonce.description}"/></textarea><br/><br/>

            <label>Adress:</label><br/>
            <input type="text" name="adress" value="<c:out value='${annonce.adress}'/>"/><br/><br/>

            <label>Mail:</label><br/>
            <input type="email" name="mail" value="<c:out value='${annonce.mail}'/>"/><br/><br/>

            <button type="submit">Mettre à jour</button>
        </form>

        <p><a href="AnnonceList">Retour liste</a></p>
    </c:otherwise>
</c:choose>

</body>
</html>
