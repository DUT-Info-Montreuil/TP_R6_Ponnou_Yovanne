## Architecture

Le projet suit une architecture en couches:

1. `controller` (ressources REST JAX-RS)
2. `service` (regles metier + transactions)
3. `repository` (acces donnees JPA)
4. `model` (entites JPA)

## Industrialisation

### Lancer separement tests unitaires et tests d'integration

Configuration Maven ajoutee:
- `maven-surefire-plugin` pour les tests unitaires
- `maven-failsafe-plugin` pour les tests d'integration
- Profils Maven `integration-tests` et `integration-only`

Commandes:

```bash
# Tests unitaires uniquement
mvn test

# Tests d'integration uniquement
mvn verify -Pintegration-only

# Build complet (unitaires + integration)
mvn verify -Pintegration-tests
```

Pourquoi separer les executions:
- Les tests unitaires tournent vite et sont adaptes au dev local.
- CI : les tests d'integration sont plus lents et plus fragiles, donc mieux controles dans un stage dedie.
- Diagnostic plus simple: en cas d'echec, on sait immediatement si le probleme vient du code metier pur (UT) ou de l'integration technique (IT).

### Tests de charge simples

Un script k6 est fourni :
- `load-tests/annonces-load-test.js`

Il execute une charge de lecture simple sur :
- `GET /api/annonces`
- `GET /api/categories`

Execution:

```bash
# URL par defaut: http://localhost:8080/masterannonce/api
k6 run load-tests/annonces-load-test.js

# URL personnalisee
k6 run -e BASE_URL=http://localhost:8080/masterannonce/api load-tests/annonces-load-test.js
```

### Documentation API (OpenAPI)

Ajouts:
- Dependance `swagger-jaxrs2`
- Enregistrement OpenAPI dans `RestApplication`

Documentation exposee en runtime:
- `http://localhost:8080/masterannonce/api/openapi.json`
- `http://localhost:8080/masterannonce/api/openapi.yaml`

UI Swagger (navigateur):
- `http://localhost:8080/masterannonce/swagger-ui/`

## Problemes rencontres

1. Logging non uniforme et peu exploitable
Le code melangeait des sorties directes (`printStackTrace`) et des logs non structures, ce qui complique l'analyse en environnement reel. La solution a ete d'imposer SLF4J + Logback, un format structure key/value, et un `request_id` de correlation pour suivre un appel de bout en bout.

2. Charge testee manuellement et de facon non reproductible
Les verifications de performance etaient manuelles et non comparables dans le temps. La solution a ete d'ajouter un script k6 versionne dans le repo, avec scenario et seuils minimaux reproductibles.

## Flow d'authentification (JAAS)

1. **Login** : `POST /api/login` avec `{"username": "...", "password": "..."}`.
   - Le `AuthController` cree un `LoginContext("MasterAnnonceLogin", callbackHandler)`.
   - Le `DbLoginModule` verifie les credentials en base via `UserService.authenticate()`.
   - En cas de succes, un token UUID est genere via `TokenStore`, et le `Subject` est peuple avec `UserPrincipal` + `RolePrincipal`.
   - Le token est retourne au client dans `{"token": "uuid", "username": "..."}`.

2. **Requete protegee** : le client envoie `Authorization: Bearer <token>` a chaque appel.
   - Le `AuthTokenFilter` (JAX-RS `@NameBinding`) intercepte les endpoints annotes `@Secured`.
   - Il cree un `LoginContext("MasterAnnonceToken", callbackHandler)` avec le token.
   - Le `TokenLoginModule` valide le token dans le `TokenStore` en memoire et reconstitue le `Subject`.
   - L'identite (`userId`) est injectee dans le `ContainerRequestContext` et un `SecurityContext` custom est mis en place.

3. **Code metier** : les services recuperent le `userId` depuis le contexte pour appliquer les regles (seul l'auteur modifie/supprime, etc.).

## Collection Postman

La collection Postman est disponible dans `postman/MasterAnnonce.postman_collection.json`.

Elle contient des requetes organisees par dossier :
- **Demo** : helloWorld, params (QueryParam + PathParam)
- **Auth** : login valide, login invalide
- **Users** : CRUD complet
- **Categories** : CRUD complet
- **Annonces** : CRUD + publish + archive + delete
- **Security Tests** : 401 sans token, 401 token invalide, 400 validation, 404

Le script de login sauvegarde automatiquement le token dans la variable `{{token}}` pour les requetes suivantes.
