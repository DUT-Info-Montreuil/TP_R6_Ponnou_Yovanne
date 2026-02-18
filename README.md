## Architecture

Le projet suit une architecture en couches :

1. `controller` (ressources REST JAX-RS)
2. `service` (règles métier + transactions)
3. `repository` (accès données JPA)
4. `model` (entités JPA)

## Industrialisation

### Lancer séparément tests unitaires et tests d'intégration

Configuration Maven ajoutée :
- `maven-surefire-plugin` pour les tests unitaires
- `maven-failsafe-plugin` pour les tests d'intégration
- Profils Maven `integration-tests` et `integration-only`

Commandes :

```bash
# Tests unitaires uniquement
mvn test

# Tests d'intégration uniquement
mvn verify -Pintegration-only

# Build complet (unitaires + intégration)
mvn verify -Pintegration-tests
```

Pourquoi séparer les exécutions :
- Les tests unitaires tournent vite et sont adaptés au développement local.
- CI : les tests d'intégration sont plus lents et plus fragiles, donc mieux contrôlés dans un stage dédié.
- Diagnostic plus simple : en cas d'échec, on sait immédiatement si le problème vient du code métier pur (UT) ou de l'intégration technique (IT).

### Tests de charge simples

Script k6 :
- `load-tests/annonces-load-test.js`

Il exécute une charge de lecture simple sur :
- `GET /api/annonces`
- `GET /api/categories`

Exécution :

```bash
# URL par défaut : http://localhost:8080/masterannonce/api
k6 run load-tests/annonces-load-test.js

# URL personnalisée
k6 run -e BASE_URL=http://localhost:8080/masterannonce/api load-tests/annonces-load-test.js
```

### Documentation API (OpenAPI)

Ajouts :
- Dépendance `swagger-jaxrs2`
- Enregistrement OpenAPI dans `RestApplication`

Documentation exposée en runtime :
- `http://localhost:8080/masterannonce/api/openapi.json`
- `http://localhost:8080/masterannonce/api/openapi.yaml`

UI Swagger (navigateur) :
- `http://localhost:8080/masterannonce/swagger-ui/`

## Problèmes rencontrés

### 1. Logs uniquement en console, aucune persistance

**Problème** : Logback était configuré avec un seul appender `STDOUT`. En production, les logs disparaissent au redémarrage et sont impossibles à analyser.

**Solution** : Ajout de trois `RollingFileAppender` distincts dans `logback.xml` avec rotation journalière : `logs/info.log`, `logs/warn.log`, `logs/error.log`. Chaque fichier ne reçoit que les logs de son niveau via un `LevelFilter` avec une rétention de 30 jours.

---

### 2. JAAS : `LoginContext` non trouvé au démarrage

**Problème** : Au lancement de l'application, JAAS lève `javax.security.auth.login.LoginException: No LoginModules configured for MasterAnnonceLogin` car le fichier `jaas.conf` n'est pas chargé.

**Solution** : Appel explicite de `JaasConfig.install()` dans le constructeur de `RestApplication`, qui positionne la propriété système `java.security.auth.login.config` pointant vers `src/main/resources/jaas.conf`. Aussi configuré dans le plugin Cargo via `<systemProperties>`.

---

### 3. JAAS : `TokenStore` vide après redémarrage

**Problème** : Le `TokenStore` est un singleton en mémoire. Après un redémarrage de Tomcat, tous les tokens sont perdus et les clients reçoivent des `401` alors que leurs tokens semblent valides.

**Solution** : Aucune solution trouvée dans le cadre du projet. Il faudrait une persistance en base ou via Redis pour un vrai environnement de prod.

---

### 4. Swagger UI : erreur 404 sur chaque requête "Try it out"

**Problème** : Swagger UI générait des URLs incorrectes car aucun champ `servers` n'était défini dans la spec OpenAPI. Il utilisait `/` par défaut, envoyant les requêtes vers `http://localhost:8080/annonces` au lieu de `http://localhost:8080/masterannonce/api/annonces`.

**Solution** : Ajout de `.servers(List.of(new Server().url("/masterannonce")))` dans `RestApplication.registerOpenApi()`. Swagger UI concatène ensuite le basePath `/api` (depuis `@ApplicationPath`) pour former la bonne URL.

---

### 5. `AuthTokenFilter` appliqué à tous les endpoints

**Problème** : Le filtre d'authentification était marqué `@Provider` sans `@NameBinding`, ce qui l'appliquait globalement à tous les endpoints JAX-RS, y compris `GET /annonces` et `POST /login`.

**Solution** : Utilisation d'une annotation `@Secured` custom comme `@NameBinding`. Le filtre n'est activé que sur les méthodes/classes annotées `@Secured`, laissant les endpoints publics libres d'accès.

## Flow d'authentification (JAAS)

1. **Login** : `POST /api/login` avec `{"username": "...", "password": "..."}`.
   - Le `AuthController` crée un `LoginContext("MasterAnnonceLogin", callbackHandler)`.
   - Le `DbLoginModule` vérifie les credentials en base via `UserService.authenticate()`.
   - En cas de succès, un token UUID est généré via `TokenStore`, et le `Subject` est peuplé avec `UserPrincipal` + `RolePrincipal`.
   - Le token est retourné au client dans `{"token": "uuid", "username": "..."}`.

2. **Requête protégée** : le client envoie `Authorization: Bearer <token>` à chaque appel.
   - Le `AuthTokenFilter` (JAX-RS `@NameBinding`) intercepte les endpoints annotés `@Secured`.
   - Il crée un `LoginContext("MasterAnnonceToken", callbackHandler)` avec le token.
   - Le `TokenLoginModule` valide le token dans le `TokenStore` en mémoire et reconstitue le `Subject`.
   - L'identité (`userId`) est injectée dans le `ContainerRequestContext` et un `SecurityContext` custom est mis en place.

3. **Code métier** : les services récupèrent le `userId` depuis le contexte pour appliquer les règles (seul l'auteur modifie/supprime, etc.).

## Collection Postman

La collection Postman est disponible dans `postman/MasterAnnonce.postman_collection.json`.

Elle contient des requêtes organisées par dossier :
- **Demo** : helloWorld, params (QueryParam + PathParam)
- **Auth** : login valide, login invalide
- **Users** : CRUD complet
- **Categories** : CRUD complet
- **Annonces** : CRUD + publish + archive + delete
- **Security Tests** : 401 sans token, 401 token invalide, 400 validation, 404

Le script de login sauvegarde automatiquement le token dans la variable `{{token}}` pour les requêtes suivantes.
