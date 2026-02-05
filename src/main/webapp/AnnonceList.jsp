<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Liste annonces</title>
    <style>
        .pagination {
            margin: 20px 0;
        }
        .pagination a, .pagination span {
            padding: 8px 12px;
            margin: 0 2px;
            border: 1px solid #ccc;
            text-decoration: none;
            color: #333;
        }
        .pagination a:hover {
            background-color: #eee;
        }
        .pagination .current {
            background-color: #007bff;
            color: white;
            border-color: #007bff;
        }
        .pagination .disabled {
            color: #999;
            cursor: not-allowed;
        }
    </style>
</head>
<body>
<h1>Liste des annonces</h1>

<p><a href="AnnonceAdd">Ajouter une annonce</a></p>

<p>Total: ${totalAnnonces} annonce(s) - Page ${currentPage} / ${totalPages}</p>

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

        <div class="pagination">
            <c:if test="${currentPage > 1}">
                <a href="AnnonceList?page=1">&laquo; Début</a>
                <a href="AnnonceList?page=${currentPage - 1}">&lsaquo; Précédent</a>
            </c:if>
            <c:if test="${currentPage <= 1}">
                <span class="disabled">&laquo; Début</span>
                <span class="disabled">&lsaquo; Précédent</span>
            </c:if>

            <c:forEach begin="${currentPage - 2 > 1 ? currentPage - 2 : 1}"
                       end="${currentPage + 2 < totalPages ? currentPage + 2 : totalPages}"
                       var="i">
                <c:choose>
                    <c:when test="${i == currentPage}">
                        <span class="current">${i}</span>
                    </c:when>
                    <c:otherwise>
                        <a href="AnnonceList?page=${i}">${i}</a>
                    </c:otherwise>
                </c:choose>
            </c:forEach>

            <c:if test="${currentPage < totalPages}">
                <a href="AnnonceList?page=${currentPage + 1}">Suivant &rsaquo;</a>
                <a href="AnnonceList?page=${totalPages}">Fin &raquo;</a>
            </c:if>
            <c:if test="${currentPage >= totalPages}">
                <span class="disabled">Suivant &rsaquo;</span>
                <span class="disabled">Fin &raquo;</span>
            </c:if>
        </div>
    </c:otherwise>
</c:choose>

</body>
</html>
