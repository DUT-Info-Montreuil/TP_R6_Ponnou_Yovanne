# MasterAnnonce - TP Dev Avance #3

Backend Java EE expose en API REST JSON (JAX-RS + JPA/Hibernate), sans Spring.

## Architecture

Le projet suit une architecture en couches:

1. `controller` (ressources REST JAX-RS)
2. `service` (regles metier + transactions)
3. `repository` (acces donnees JPA)
4. `model` (entites JPA)

Composants transverses:
- Validation Bean Validation sur DTO
- Mapping DTO <-> Entite
- Gestion centralisee des erreurs REST
- Authentification stateless par token

## Exercice 1 - Choix de configuration JAX-RS

Le projet utilise **Jersey 2.41** comme implementation JAX-RS. Ce choix se justifie par :

- **Compatibilite Tomcat** : Jersey fonctionne nativement en tant que servlet filter dans un conteneur Servlet standard (Tomcat 9), sans necessiter un serveur d'applications complet (Payara, WildFly).
- **Pas de Spring** : contrairement a RESTEasy qui est souvent couple a un serveur JBoss/WildFly ou a Spring, Jersey s'integre facilement dans un WAR minimal deploye sur Tomcat via `jersey-container-servlet`.
- **Injection de dependances via HK2** : Jersey embarque HK2, ce qui permet l'injection de dependances (`@Context`, `@QueryParam`, `@PathParam`, etc.) sans framework externe.
- **Jackson natif** : le module `jersey-media-json-jackson` assure la serialisation/deserialisation JSON sans configuration supplementaire.
- **Bean Validation integree** : le module `jersey-bean-validation` active `javax.validation` directement sur les parametres des ressources.
- **Framework de test** : `jersey-test-framework` (Grizzly2) permet de lancer un serveur HTTP embarque pour les tests d'integration REST sans deployer de WAR.

Les endpoints de demonstration sont exposes sur :
- `GET /api/helloWorld` — retourne `{"message": "Hello World"}`
- `GET /api/params?name=X&count=N` — demonstration de `@QueryParam`
- `GET /api/params/{id}?detail=true` — demonstration de `@PathParam` + `@QueryParam`

## Exercice 10 - Industrialisation

### 1) Lancer separement tests unitaires et tests d'integration

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
- Feedback rapide: les tests unitaires tournent vite et sont adaptes au developpement local frequent.
- Robustesse CI: les tests d'integration sont plus lents et plus fragiles (I/O, base, conteneur HTTP), donc mieux controles dans un stage dedie.
- Diagnostic plus simple: en cas d'echec, on sait immediatement si le probleme vient du code metier pur (UT) ou de l'integration technique (IT).

### 2) Logging structure

Ajouts:
- Filtre JAX-RS `RequestResponseLoggingFilter` pour journaliser chaque requete/reponse.
- Correlation par `X-Request-Id` (genere si absent, renvoye dans la reponse).
- `logback.xml` pour un format de logs structure en key/value.
- Remplacement du `printStackTrace` par un logger SLF4J dans le mapper d'exceptions global.

Exemple de ligne de log:

```text
2026-02-16T23:10:12.200+01:00 level=INFO logger=... request_id=... msg="event=http_request_end request_id=... method=GET path=annonces status=200 duration_ms=14 user_id=anonymous"
```

### 3) Tests de charge simples

Un script k6 est fourni:
- `load-tests/annonces-load-test.js`

Il execute une charge de lecture simple sur:
- `GET /api/annonces`
- `GET /api/categories`

Avec ramp-up progressif et seuils de base:
- erreurs HTTP < 1%
- p95 latence < 500 ms

Execution:

```bash
# URL par defaut: http://localhost:8080/masterannonce/api
k6 run load-tests/annonces-load-test.js

# URL personnalisee
k6 run -e BASE_URL=http://localhost:8080/masterannonce/api load-tests/annonces-load-test.js
```

### 4) Documentation API (OpenAPI)

Ajouts:
- Dependance `swagger-jaxrs2`
- Enregistrement OpenAPI dans `RestApplication`

Documentation exposee en runtime:
- `http://localhost:8080/masterannonce/api/openapi.json`
- `http://localhost:8080/masterannonce/api/openapi.yaml`

UI Swagger (navigateur):
- `http://localhost:8080/masterannonce/swagger-ui/`

## Problemes rencontres (Exercice 10)

1. Separation UT/IT sans renommer tout le parc de tests
Le projet contenait deja beaucoup de classes nommees `*Test`, y compris des tests REST et repository (integration). Renommer tout etait couteux et risquait d'introduire de la dette. La solution retenue a ete un filtrage par pattern dans Surefire/Failsafe + profils Maven, ce qui permet une migration progressive sans casser les tests existants.

2. Logging non uniforme et peu exploitable
Le code melangeait des sorties directes (`printStackTrace`) et des logs non structures, ce qui complique l'analyse en environnement reel. La solution a ete d'imposer SLF4J + Logback, un format structure key/value, et un `request_id` de correlation pour suivre un appel de bout en bout.

3. Documentation API absente ou difficile a maintenir
Sans OpenAPI, l'exploration des endpoints dependait du code source et des tests. La solution a ete d'ajouter Swagger Core sur JAX-RS pour exposer automatiquement une spec OpenAPI versionnee.

4. Charge testee manuellement et de facon non reproductible
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

## Fichiers modifies pour l'exercice 10

- `pom.xml`
- `src/main/java/org/univ_paris8/iut/montreuil/qdev/tp2025/ponnou/tp1/tp1/common/config/RestApplication.java`
- `src/main/java/org/univ_paris8/iut/montreuil/qdev/tp2025/ponnou/tp1/tp1/common/logging/RequestResponseLoggingFilter.java`
- `src/main/java/org/univ_paris8/iut/montreuil/qdev/tp2025/ponnou/tp1/tp1/common/exception/ExceptionMappers.java`
- `src/main/resources/logback.xml`
- `load-tests/annonces-load-test.js`

## Prerequis

- JDK 11
- Maven 3.9+
- PostgreSQL (runtime applicatif)
- k6 (pour les tests de charge)
