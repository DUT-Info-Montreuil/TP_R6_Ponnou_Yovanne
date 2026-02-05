<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>MasterAnnonce - Accueil</title>
    <style>
        * { box-sizing: border-box; margin: 0; padding: 0; }
        body { font-family: Arial, sans-serif; background: #f5f5f5; min-height: 100vh; }
        .navbar { background: #343a40; color: white; padding: 1rem 2rem; display: flex; justify-content: space-between; align-items: center; }
        .navbar h1 { font-size: 1.5rem; }
        .navbar a { color: white; text-decoration: none; margin-left: 1rem; }
        .navbar a:hover { text-decoration: underline; }
        .container { max-width: 1200px; margin: 0 auto; padding: 2rem; }
        .filters { background: white; padding: 1rem; border-radius: 8px; margin-bottom: 1.5rem; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }
        .filters form { display: flex; gap: 1rem; flex-wrap: wrap; align-items: center; }
        .filters input, .filters select { padding: 0.5rem; border: 1px solid #ddd; border-radius: 4px; }
        .filters input[type="text"] { flex: 1; min-width: 200px; }
        .filters button { padding: 0.5rem 1rem; background: #007bff; color: white; border: none; border-radius: 4px; cursor: pointer; }
        .filters button:hover { background: #0056b3; }
        .annonce-list { display: grid; gap: 1rem; }
        .annonce-card { background: white; padding: 1.5rem; border-radius: 8px; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }
        .annonce-card h2 { margin-bottom: 0.5rem; color: #333; }
        .annonce-card h2 a { color: inherit; text-decoration: none; }
        .annonce-card h2 a:hover { color: #007bff; }
        .annonce-meta { color: #666; font-size: 0.9rem; margin-bottom: 0.5rem; }
        .annonce-meta span { margin-right: 1rem; }
        .annonce-description { color: #555; line-height: 1.5; }
        .category-badge { display: inline-block; background: #e9ecef; color: #495057; padding: 0.25rem 0.5rem; border-radius: 4px; font-size: 0.8rem; }
        .pagination { display: flex; justify-content: center; gap: 0.5rem; margin-top: 2rem; }
        .pagination a, .pagination span { padding: 0.5rem 1rem; border: 1px solid #ddd; border-radius: 4px; text-decoration: none; color: #333; }
        .pagination a:hover { background: #e9ecef; }
        .pagination .active { background: #007bff; color: white; border-color: #007bff; }
        .empty { text-align: center; padding: 3rem; color: #666; }
        .user-info { display: flex; align-items: center; gap: 1rem; }
    </style>
</head>
<body>
    <nav class="navbar">
        <h1>MasterAnnonce</h1>
        <div class="user-info">
            <c:choose>
                <c:when test="${not empty sessionScope.user}">
                    <span>Bonjour, ${sessionScope.user.username}</span>
                    <a href="${pageContext.request.contextPath}/mes-annonces">Mes annonces</a>
                    <a href="${pageContext.request.contextPath}/annonces/create">Nouvelle annonce</a>
                    <a href="${pageContext.request.contextPath}/logout">Déconnexion</a>
                </c:when>
                <c:otherwise>
                    <a href="${pageContext.request.contextPath}/login">Connexion</a>
                    <a href="${pageContext.request.contextPath}/register">Inscription</a>
                </c:otherwise>
            </c:choose>
        </div>
    </nav>

    <div class="container">
        <div class="filters">
            <form method="get" action="${pageContext.request.contextPath}/">
                <input type="text" name="keyword" placeholder="Rechercher..." value="${keyword}">
                <select name="category">
                    <option value="">Toutes les catégories</option>
                    <c:forEach var="cat" items="${categories}">
                        <option value="${cat.id}" ${selectedCategory == cat.id ? 'selected' : ''}>${cat.label}</option>
                    </c:forEach>
                </select>
                <button type="submit">Rechercher</button>
                <c:if test="${not empty keyword or not empty selectedCategory}">
                    <a href="${pageContext.request.contextPath}/" style="color: #666;">Effacer les filtres</a>
                </c:if>
            </form>
        </div>

        <p style="margin-bottom: 1rem; color: #666;">${totalCount} annonce(s) trouvée(s)</p>

        <div class="annonce-list">
            <c:choose>
                <c:when test="${empty annonces}">
                    <div class="empty">
                        <p>Aucune annonce pour le moment.</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <c:forEach var="annonce" items="${annonces}">
                        <div class="annonce-card">
                            <h2><a href="${pageContext.request.contextPath}/annonce/detail?id=${annonce.id}">${annonce.title}</a></h2>
                            <div class="annonce-meta">
                                <span class="category-badge">${annonce.category.label}</span>
                                <span>Par ${annonce.author.username}</span>
                                <span><fmt:formatDate value="${annonce.date}" pattern="dd/MM/yyyy HH:mm"/></span>
                            </div>
                            <p class="annonce-description">${annonce.description}</p>
                        </div>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
        </div>

        <c:if test="${totalPages > 1}">
            <div class="pagination">
                <c:if test="${currentPage > 0}">
                    <a href="?page=${currentPage - 1}<c:if test='${not empty keyword}'>&keyword=${keyword}</c:if><c:if test='${not empty selectedCategory}'>&category=${selectedCategory}</c:if>">Précédent</a>
                </c:if>

                <c:forEach begin="0" end="${totalPages - 1}" var="i">
                    <c:choose>
                        <c:when test="${i == currentPage}">
                            <span class="active">${i + 1}</span>
                        </c:when>
                        <c:otherwise>
                            <a href="?page=${i}<c:if test='${not empty keyword}'>&keyword=${keyword}</c:if><c:if test='${not empty selectedCategory}'>&category=${selectedCategory}</c:if>">${i + 1}</a>
                        </c:otherwise>
                    </c:choose>
                </c:forEach>

                <c:if test="${currentPage < totalPages - 1}">
                    <a href="?page=${currentPage + 1}<c:if test='${not empty keyword}'>&keyword=${keyword}</c:if><c:if test='${not empty selectedCategory}'>&category=${selectedCategory}</c:if>">Suivant</a>
                </c:if>
            </div>
        </c:if>
    </div>
</body>
</html>
