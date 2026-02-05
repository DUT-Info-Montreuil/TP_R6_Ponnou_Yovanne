<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Mes annonces - MasterAnnonce</title>
    <style>
        * { box-sizing: border-box; margin: 0; padding: 0; }
        body { font-family: Arial, sans-serif; background: #f5f5f5; min-height: 100vh; }
        .navbar { background: #343a40; color: white; padding: 1rem 2rem; display: flex; justify-content: space-between; align-items: center; }
        .navbar h1 { font-size: 1.5rem; }
        .navbar a { color: white; text-decoration: none; margin-left: 1rem; }
        .navbar a:hover { text-decoration: underline; }
        .container { max-width: 1000px; margin: 0 auto; padding: 2rem; }
        .header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; }
        .header h2 { color: #333; }
        .btn { padding: 0.5rem 1rem; border: none; border-radius: 4px; cursor: pointer; text-decoration: none; font-size: 0.9rem; display: inline-block; }
        .btn-primary { background: #007bff; color: white; }
        .btn-primary:hover { background: #0056b3; }
        .annonce-list { display: grid; gap: 1rem; }
        .annonce-card { background: white; padding: 1.5rem; border-radius: 8px; box-shadow: 0 2px 5px rgba(0,0,0,0.1); display: flex; justify-content: space-between; align-items: flex-start; }
        .annonce-info { flex: 1; }
        .annonce-info h3 { margin-bottom: 0.5rem; color: #333; }
        .annonce-info h3 a { color: inherit; text-decoration: none; }
        .annonce-info h3 a:hover { color: #007bff; }
        .annonce-meta { color: #666; font-size: 0.85rem; margin-bottom: 0.5rem; }
        .annonce-meta span { margin-right: 0.75rem; }
        .status-badge { display: inline-block; padding: 0.2rem 0.5rem; border-radius: 4px; font-size: 0.75rem; font-weight: bold; text-transform: uppercase; }
        .status-DRAFT { background: #ffc107; color: #000; }
        .status-PUBLISHED { background: #28a745; color: white; }
        .status-ARCHIVED { background: #6c757d; color: white; }
        .category-badge { display: inline-block; background: #e9ecef; color: #495057; padding: 0.2rem 0.5rem; border-radius: 4px; font-size: 0.75rem; }
        .annonce-actions { display: flex; gap: 0.5rem; flex-wrap: wrap; }
        .btn-sm { padding: 0.35rem 0.75rem; font-size: 0.8rem; }
        .btn-success { background: #28a745; color: white; }
        .btn-success:hover { background: #218838; }
        .btn-warning { background: #ffc107; color: #000; }
        .btn-warning:hover { background: #e0a800; }
        .btn-danger { background: #dc3545; color: white; }
        .btn-danger:hover { background: #c82333; }
        .btn-secondary { background: #6c757d; color: white; }
        .btn-secondary:hover { background: #545b62; }
        .pagination { display: flex; justify-content: center; gap: 0.5rem; margin-top: 2rem; }
        .pagination a, .pagination span { padding: 0.5rem 1rem; border: 1px solid #ddd; border-radius: 4px; text-decoration: none; color: #333; }
        .pagination a:hover { background: #e9ecef; }
        .pagination .active { background: #007bff; color: white; border-color: #007bff; }
        .empty { text-align: center; padding: 3rem; color: #666; background: white; border-radius: 8px; }
        .user-info { display: flex; align-items: center; gap: 1rem; }
    </style>
</head>
<body>
    <nav class="navbar">
        <h1><a href="${pageContext.request.contextPath}/" style="color: white; text-decoration: none;">MasterAnnonce</a></h1>
        <div class="user-info">
            <span>Bonjour, ${sessionScope.user.username}</span>
            <a href="${pageContext.request.contextPath}/">Accueil</a>
            <a href="${pageContext.request.contextPath}/annonces/create">Nouvelle annonce</a>
            <a href="${pageContext.request.contextPath}/logout">Déconnexion</a>
        </div>
    </nav>

    <div class="container">
        <div class="header">
            <h2>Mes annonces (${totalCount})</h2>
            <a href="${pageContext.request.contextPath}/annonces/create" class="btn btn-primary">+ Nouvelle annonce</a>
        </div>

        <div class="annonce-list">
            <c:choose>
                <c:when test="${empty annonces}">
                    <div class="empty">
                        <p>Vous n'avez pas encore d'annonce.</p>
                        <p style="margin-top: 1rem;"><a href="${pageContext.request.contextPath}/annonces/create" class="btn btn-primary">Créer ma première annonce</a></p>
                    </div>
                </c:when>
                <c:otherwise>
                    <c:forEach var="annonce" items="${annonces}">
                        <div class="annonce-card">
                            <div class="annonce-info">
                                <h3>
                                    <a href="${pageContext.request.contextPath}/annonce/detail?id=${annonce.id}">${annonce.title}</a>
                                </h3>
                                <div class="annonce-meta">
                                    <span class="status-badge status-${annonce.status}">${annonce.status}</span>
                                    <span class="category-badge">${annonce.category.label}</span>
                                    <span><fmt:formatDate value="${annonce.date}" pattern="dd/MM/yyyy HH:mm"/></span>
                                </div>
                            </div>
                            <div class="annonce-actions">
                                <a href="${pageContext.request.contextPath}/annonces/edit?id=${annonce.id}" class="btn btn-sm btn-secondary">Modifier</a>

                                <c:if test="${annonce.status == 'DRAFT'}">
                                    <form method="post" action="${pageContext.request.contextPath}/annonces/status" style="display: inline;">
                                        <input type="hidden" name="id" value="${annonce.id}">
                                        <input type="hidden" name="action" value="publish">
                                        <button type="submit" class="btn btn-sm btn-success">Publier</button>
                                    </form>
                                </c:if>

                                <c:if test="${annonce.status == 'PUBLISHED'}">
                                    <form method="post" action="${pageContext.request.contextPath}/annonces/status" style="display: inline;">
                                        <input type="hidden" name="id" value="${annonce.id}">
                                        <input type="hidden" name="action" value="archive">
                                        <button type="submit" class="btn btn-sm btn-warning">Archiver</button>
                                    </form>
                                </c:if>

                                <form method="post" action="${pageContext.request.contextPath}/annonces/delete" style="display: inline;" onsubmit="return confirm('Supprimer cette annonce ?');">
                                    <input type="hidden" name="id" value="${annonce.id}">
                                    <button type="submit" class="btn btn-sm btn-danger">Supprimer</button>
                                </form>
                            </div>
                        </div>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
        </div>

        <c:if test="${totalPages > 1}">
            <div class="pagination">
                <c:if test="${currentPage > 0}">
                    <a href="?page=${currentPage - 1}">Précédent</a>
                </c:if>

                <c:forEach begin="0" end="${totalPages - 1}" var="i">
                    <c:choose>
                        <c:when test="${i == currentPage}">
                            <span class="active">${i + 1}</span>
                        </c:when>
                        <c:otherwise>
                            <a href="?page=${i}">${i + 1}</a>
                        </c:otherwise>
                    </c:choose>
                </c:forEach>

                <c:if test="${currentPage < totalPages - 1}">
                    <a href="?page=${currentPage + 1}">Suivant</a>
                </c:if>
            </div>
        </c:if>
    </div>
</body>
</html>
