<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${empty annonce ? 'Nouvelle annonce' : 'Modifier l\'annonce'} - MasterAnnonce</title>
    <style>
        * { box-sizing: border-box; margin: 0; padding: 0; }
        body { font-family: Arial, sans-serif; background: #f5f5f5; min-height: 100vh; }
        .navbar { background: #343a40; color: white; padding: 1rem 2rem; display: flex; justify-content: space-between; align-items: center; }
        .navbar h1 { font-size: 1.5rem; }
        .navbar a { color: white; text-decoration: none; margin-left: 1rem; }
        .navbar a:hover { text-decoration: underline; }
        .container { max-width: 600px; margin: 0 auto; padding: 2rem; }
        .back-link { margin-bottom: 1rem; }
        .back-link a { color: #007bff; text-decoration: none; }
        .back-link a:hover { text-decoration: underline; }
        .form-container { background: white; padding: 2rem; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        .form-container h2 { margin-bottom: 1.5rem; color: #333; }
        .form-group { margin-bottom: 1rem; }
        label { display: block; margin-bottom: 0.5rem; font-weight: bold; color: #555; }
        input[type="text"], input[type="email"], textarea, select { width: 100%; padding: 0.75rem; border: 1px solid #ddd; border-radius: 4px; font-size: 1rem; font-family: inherit; }
        input:focus, textarea:focus, select:focus { outline: none; border-color: #007bff; }
        textarea { resize: vertical; min-height: 100px; }
        .char-count { font-size: 0.8rem; color: #666; text-align: right; margin-top: 0.25rem; }
        .btn { padding: 0.75rem 1.5rem; border: none; border-radius: 4px; cursor: pointer; font-size: 1rem; }
        .btn-primary { background: #007bff; color: white; }
        .btn-primary:hover { background: #0056b3; }
        .btn-secondary { background: #6c757d; color: white; margin-left: 0.5rem; }
        .btn-secondary:hover { background: #545b62; }
        .error { background: #f8d7da; color: #721c24; padding: 0.75rem; border-radius: 4px; margin-bottom: 1rem; }
        .actions { margin-top: 1.5rem; display: flex; }
        .user-info { display: flex; align-items: center; gap: 1rem; }
    </style>
</head>
<body>
    <nav class="navbar">
        <h1><a href="${pageContext.request.contextPath}/" style="color: white; text-decoration: none;">MasterAnnonce</a></h1>
        <div class="user-info">
            <span>Bonjour, ${sessionScope.user.username}</span>
            <a href="${pageContext.request.contextPath}/mes-annonces">Mes annonces</a>
            <a href="${pageContext.request.contextPath}/logout">Déconnexion</a>
        </div>
    </nav>

    <div class="container">
        <div class="back-link">
            <a href="${pageContext.request.contextPath}/mes-annonces">← Retour à mes annonces</a>
        </div>

        <div class="form-container">
            <h2>${empty annonce ? 'Nouvelle annonce' : 'Modifier l\'annonce'}</h2>

            <c:if test="${not empty error}">
                <div class="error">${error}</div>
            </c:if>

            <form method="post" action="${pageContext.request.contextPath}${empty annonce ? '/annonces/create' : '/annonces/edit'}">
                <c:if test="${not empty annonce}">
                    <input type="hidden" name="id" value="${annonce.id}">
                </c:if>

                <div class="form-group">
                    <label for="title">Titre *</label>
                    <input type="text" id="title" name="title" value="${title}" required maxlength="64" oninput="updateCount('title', 64)">
                    <div class="char-count"><span id="title-count">${empty title ? 0 : title.length()}</span>/64</div>
                </div>

                <div class="form-group">
                    <label for="description">Description *</label>
                    <textarea id="description" name="description" required maxlength="256" oninput="updateCount('description', 256)">${description}</textarea>
                    <div class="char-count"><span id="description-count">${empty description ? 0 : description.length()}</span>/256</div>
                </div>

                <div class="form-group">
                    <label for="categoryId">Catégorie *</label>
                    <select id="categoryId" name="categoryId" required>
                        <option value="">-- Sélectionnez une catégorie --</option>
                        <c:forEach var="cat" items="${categories}">
                            <option value="${cat.id}" ${selectedCategoryId == cat.id.toString() ? 'selected' : ''}>${cat.label}</option>
                        </c:forEach>
                    </select>
                </div>

                <div class="form-group">
                    <label for="adress">Adresse *</label>
                    <input type="text" id="adress" name="adress" value="${adress}" required maxlength="64" oninput="updateCount('adress', 64)">
                    <div class="char-count"><span id="adress-count">${empty adress ? 0 : adress.length()}</span>/64</div>
                </div>

                <div class="form-group">
                    <label for="mail">Email de contact *</label>
                    <input type="email" id="mail" name="mail" value="${mail}" required maxlength="64">
                </div>

                <div class="actions">
                    <button type="submit" class="btn btn-primary">${empty annonce ? 'Créer l\'annonce' : 'Enregistrer'}</button>
                    <a href="${pageContext.request.contextPath}/mes-annonces" class="btn btn-secondary">Annuler</a>
                </div>
            </form>
        </div>
    </div>

    <script>
        function updateCount(fieldId, max) {
            var field = document.getElementById(fieldId);
            var count = document.getElementById(fieldId + '-count');
            count.textContent = field.value.length;
        }
    </script>
</body>
</html>
