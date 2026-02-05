<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${annonce.title} - MasterAnnonce</title>
    <style>
        * { box-sizing: border-box; margin: 0; padding: 0; }
        body { font-family: Arial, sans-serif; background: #f5f5f5; min-height: 100vh; }
        .navbar { background: #343a40; color: white; padding: 1rem 2rem; display: flex; justify-content: space-between; align-items: center; }
        .navbar h1 { font-size: 1.5rem; }
        .navbar a { color: white; text-decoration: none; margin-left: 1rem; }
        .navbar a:hover { text-decoration: underline; }
        .container { max-width: 800px; margin: 0 auto; padding: 2rem; }
        .back-link { margin-bottom: 1rem; }
        .back-link a { color: #007bff; text-decoration: none; }
        .back-link a:hover { text-decoration: underline; }
        .annonce-detail { background: white; padding: 2rem; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        .annonce-header { margin-bottom: 1.5rem; }
        .annonce-header h1 { margin-bottom: 0.5rem; color: #333; }
        .status-badge { display: inline-block; padding: 0.25rem 0.75rem; border-radius: 4px; font-size: 0.8rem; font-weight: bold; text-transform: uppercase; }
        .status-DRAFT { background: #ffc107; color: #000; }
        .status-PUBLISHED { background: #28a745; color: white; }
        .status-ARCHIVED { background: #6c757d; color: white; }
        .category-badge { display: inline-block; background: #e9ecef; color: #495057; padding: 0.25rem 0.5rem; border-radius: 4px; font-size: 0.9rem; margin-left: 0.5rem; }
        .annonce-meta { color: #666; font-size: 0.9rem; margin-top: 0.5rem; }
        .annonce-content { margin: 1.5rem 0; }
        .annonce-content h2 { font-size: 1.1rem; color: #333; margin-bottom: 0.5rem; }
        .annonce-content p { color: #555; line-height: 1.6; margin-bottom: 1rem; }
        .contact-info { background: #f8f9fa; padding: 1rem; border-radius: 4px; margin-top: 1.5rem; }
        .contact-info h3 { font-size: 1rem; margin-bottom: 0.5rem; color: #333; }
        .contact-info p { color: #555; margin: 0.25rem 0; }
        .actions { margin-top: 1.5rem; padding-top: 1.5rem; border-top: 1px solid #eee; display: flex; gap: 0.5rem; flex-wrap: wrap; }
        .btn { padding: 0.5rem 1rem; border: none; border-radius: 4px; cursor: pointer; text-decoration: none; font-size: 0.9rem; display: inline-block; }
        .btn-primary { background: #007bff; color: white; }
        .btn-primary:hover { background: #0056b3; }
        .btn-success { background: #28a745; color: white; }
        .btn-success:hover { background: #218838; }
        .btn-warning { background: #ffc107; color: #000; }
        .btn-warning:hover { background: #e0a800; }
        .btn-danger { background: #dc3545; color: white; }
        .btn-danger:hover { background: #c82333; }
        .user-info { display: flex; align-items: center; gap: 1rem; }
    </style>
</head>
<body>
    <nav class="navbar">
        <h1><a href="${pageContext.request.contextPath}/" style="color: white; text-decoration: none;">MasterAnnonce</a></h1>
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
        <div class="back-link">
            <a href="${pageContext.request.contextPath}/">← Retour à la liste</a>
        </div>

        <div class="annonce-detail">
            <div class="annonce-header">
                <h1>
                    ${annonce.title}
                    <span class="status-badge status-${annonce.status}">${annonce.status}</span>
                    <span class="category-badge">${annonce.category.label}</span>
                </h1>
                <div class="annonce-meta">
                    <span>Par <strong>${annonce.author.username}</strong></span> |
                    <span>Publié le <fmt:formatDate value="${annonce.date}" pattern="dd/MM/yyyy à HH:mm"/></span>
                </div>
            </div>

            <div class="annonce-content">
                <h2>Description</h2>
                <p>${annonce.description}</p>

                <h2>Adresse</h2>
                <p>${annonce.adress}</p>
            </div>

            <div class="contact-info">
                <h3>Contact</h3>
                <p>Email : <a href="mailto:${annonce.mail}">${annonce.mail}</a></p>
            </div>

            <c:if test="${isOwner}">
                <div class="actions">
                    <a href="${pageContext.request.contextPath}/annonces/edit?id=${annonce.id}" class="btn btn-primary">Modifier</a>

                    <c:if test="${annonce.status == 'DRAFT'}">
                        <form method="post" action="${pageContext.request.contextPath}/annonces/status" style="display: inline;">
                            <input type="hidden" name="id" value="${annonce.id}">
                            <input type="hidden" name="action" value="publish">
                            <button type="submit" class="btn btn-success">Publier</button>
                        </form>
                    </c:if>

                    <c:if test="${annonce.status == 'PUBLISHED'}">
                        <form method="post" action="${pageContext.request.contextPath}/annonces/status" style="display: inline;">
                            <input type="hidden" name="id" value="${annonce.id}">
                            <input type="hidden" name="action" value="archive">
                            <button type="submit" class="btn btn-warning">Archiver</button>
                        </form>
                    </c:if>

                    <form method="post" action="${pageContext.request.contextPath}/annonces/delete" style="display: inline;" onsubmit="return confirm('Êtes-vous sûr de vouloir supprimer cette annonce ?');">
                        <input type="hidden" name="id" value="${annonce.id}">
                        <button type="submit" class="btn btn-danger">Supprimer</button>
                    </form>
                </div>
            </c:if>
        </div>
    </div>
</body>
</html>
