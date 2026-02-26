## Architecture

### Structure en couches

```
REST Controllers  (@RestController, Spring MVC)
       ↓
   Services       (règles métier, @Transactional)
       ↓
  Repositories    (Spring Data JPA, JpaSpecificationExecutor)
       ↓
    Entités JPA   (Hibernate, lazy loading)
       ↓
   PostgreSQL
```

### Organisation des packages

```
src/main/java/.../
├── annonce/
│   ├── controller/   AnnonceController.java        GET/POST/PUT/PATCH/DELETE /api/annonces
│   │                 AnnonceMetaController.java     GET /api/meta/annonces
│   ├── service/      AnnonceService.java            cycle de vie, règles métier, pagination
│   ├── repository/   AnnonceRepository.java         requêtes JPQL, filtres, tri
│   │                 AnnonceSpecifications.java     recherche multi-critères (Specification)
│   ├── model/        Annonce.java, AnnonceStatus.java
│   ├── dto/          AnnonceDTO / CreateDTO / UpdateDTO / PatchDTO
│   ├── mappers/      AnnonceMapper.java             (MapStruct)
│   └── utils/        AnnonceFieldValidator.java     validation des champs de tri par introspection
│
├── user/
│   ├── controller/   UserController.java            CRUD /api/users
│   ├── service/      UserService.java               hash BCrypt, vérif email unique
│   ├── repository/   UserRepository.java
│   ├── model/        User.java
│   ├── dto/          UserDTO / CreateDTO / UpdateDTO / PatchDTO
│   └── mapper/       UserMapper.java
│
├── category/
│   ├── controller/   CategoryController.java        CRUD /api/categories
│   ├── service/      CategoryService.java
│   ├── repository/   CategoryRepository.java
│   ├── model/        Category.java
│   ├── dto/          CategoryDTO / CreateDTO / UpdateDTO / PatchDTO
│   └── mapper/       CategoryMapper.java
│
├── auth/
│   ├── controller/   AuthController.java            POST /api/auth/login, /refresh, /logout
│   ├── dto/          LoginDTO, TokenDTO, RefreshRequestDTO
│   ├── model/        RefreshToken.java
│   ├── repository/   RefreshTokenRepository.java
│   ├── service/      RefreshTokenService.java
│   └── security/     SecurityConfig.java            SecurityFilterChain, BCrypt
│                     JwtService.java                génération/validation JWT (HMAC-SHA)
│                     JwtAuthenticationFilter.java   filtre JWT dans la chaîne Spring
│                     AuthenticatedUser.java         annotation pour récupérer l'utilisateur courant
│
└── common/
    ├── config/       PasswordUtils.java             utilitaire de hachage
    │                 OpenApiConfig.java             configuration SpringDoc
    ├── dto/          PaginatedResponse.java
    ├── exception/    GlobalExceptionHandler.java    @RestControllerAdvice centralisé
    │                 ResourceNotFoundException, ForbiddenOperationException, ErrorResponse
    ├── logging/      ExecutionLoggingAspect.java    @Around sur tous les services (AOP)
    │                 CorrelationIdFilter.java        injection MDC correlationId
    └── ratelimit/    LoginRateLimitFilter.java       rate limiting login (5 req/60s)
```

### Modèle de données

```
User ──< Annonce >── Category
User ──< RefreshToken
```

| Entité | Champs notables |
|---|---|
| `User` | id, username (unique), email (unique), password (BCrypt), createdAt |
| `Category` | id, label (unique, max 100) |
| `Annonce` | id, title (max 64), description (max 256), adress, mail, date, status (DRAFT/PUBLISHED/ARCHIVED), author (User, LAZY), category (Category, LAZY) |
| `RefreshToken` | id, token (UUID), user, expiryDate |

### Endpoints REST

| Verbe | URI | Auth requise | Description |
|---|---|---|---|
| `POST` | `/api/auth/login` | Non | Authentification, retourne access token + refresh token |
| `POST` | `/api/auth/refresh` | Non | Renouvelle l'access token via refresh token |
| `POST` | `/api/auth/logout` | Non | Révoque le refresh token |
| `GET` | `/api/annonces` | Non | Liste paginée avec filtres multi-critères |
| `GET` | `/api/annonces/{id}` | Non | Détail d'une annonce |
| `POST` | `/api/annonces` | Oui | Création (auteur = utilisateur connecté) |
| `PUT` | `/api/annonces/{id}` | Oui | Mise à jour complète (auteur uniquement, DRAFT seulement) |
| `PATCH` | `/api/annonces/{id}` | Oui | Mise à jour partielle (auteur uniquement, DRAFT seulement) |
| `DELETE` | `/api/annonces/{id}` | Oui | Suppression (auteur uniquement, ARCHIVED seulement) |
| `POST` | `/api/annonces/{id}/publish` | Oui | Publication (auteur uniquement) |
| `POST` | `/api/annonces/{id}/archive` | Oui (ADMIN) | Archivage (ROLE_ADMIN uniquement) |
| `GET` | `/api/meta/annonces` | Non | Champs filtrables/triables exposés via introspection |
| `GET` | `/api/users` | Non | Liste des utilisateurs |
| `POST` | `/api/users` | Non | Inscription |
| `PUT` | `/api/users/{id}` | Oui | Mise à jour (propriétaire ou ADMIN) |
| `DELETE` | `/api/users/{id}` | Oui | Suppression (propriétaire ou ADMIN) |
| `GET` | `/api/categories` | Non | Liste des catégories |
| `POST` | `/api/categories` | Oui (ADMIN) | Création |
| `PUT` | `/api/categories/{id}` | Oui (ADMIN) | Mise à jour |
| `DELETE` | `/api/categories/{id}` | Oui (ADMIN) | Suppression |

#### Filtres disponibles sur `GET /api/annonces`

| Paramètre | Type | Description |
|---|---|---|
| `q` | string | Recherche sur title et description (LIKE) |
| `status` | string | Filtre par statut (`DRAFT`, `PUBLISHED`, `ARCHIVED`) |
| `categoryId` | long | Filtre par catégorie |
| `authorId` | long | Filtre par auteur |
| `fromDate` | timestamp | Date de création >= |
| `toDate` | timestamp | Date de création <= |
| `page` | int | Numéro de page (défaut 0) |
| `size` | int | Taille de page (défaut 10) |
| `sort` | string | Champ de tri (ex: `title,asc`) — validé par introspection |

### Règles métier

- Une annonce en statut `PUBLISHED` ne peut pas être modifiée (PUT/PATCH refusé).
- Seul l'auteur peut modifier, patcher ou supprimer son annonce.
- Une annonce doit être `ARCHIVED` avant de pouvoir être supprimée.
- Seul un `ROLE_ADMIN` peut archiver une annonce.
- Une annonce `ARCHIVED` ne peut pas être publiée.
- Le champ `sort` est validé par introspection (`Annonce.class.getDeclaredFields()`) — un champ inconnu renvoie 400.

### Structure des tests

```
src/test/java/.../
├── annonce/
│   ├── service/       *ServiceTest.java               tests unitaires (Mockito)
│   ├── controller/    *ControllerRestTest.java         tests REST (@SpringBootTest + MockMvc)
│   ├── mappers/       *MapperTest.java
│   ├── repository/    *RepositoryTest.java             tests JPA (Testcontainers)
│   └── integration/   AnnonceWorkflowIntegrationTest.java
├── user/
│   ├── service/       AuthFindDeleteUserServiceTest.java, CreateUpdateUserServiceTest.java
│   └── controller/    UserControllerRestTest.java
├── category/          (idem)
├── auth/
│   ├── controller/    AuthControllerRestTest.java
│   └── integration/   AuthFlowIntegrationTest.java
└── common/
    └── ratelimit/     LoginRateLimitFilterTest.java
```

Les tests d'intégration et de repository utilisent **Testcontainers** (PostgreSQL réel). Les tests unitaires mockent les dépendances via Mockito.

## Sécurité JWT

### Flow d'authentification

1. **Login** : `POST /api/auth/login` avec `{"username": "...", "password": "..."}`.
   - Spring Security vérifie les credentials via `UserService` (BCrypt).
   - En cas de succès, `JwtService` génère un **access token** (24h) et un **refresh token** (7j, persisté en base).
   - Le token JWT contient les claims : `uid` (userId), `role`.

2. **Requête protégée** : le client envoie `Authorization: Bearer <access_token>`.
   - `JwtAuthenticationFilter` intercepte la requête, valide le token via `JwtService`.
   - L'identité est injectée dans le `SecurityContext` Spring.
   - `@AuthenticatedUser` (annotation custom) permet de récupérer l'utilisateur courant dans un controller.

3. **Refresh** : `POST /api/auth/refresh` avec `{"refreshToken": "..."}`.
   - `RefreshTokenService` vérifie le token en base et son expiration.
   - Retourne un nouvel access token.

4. **Logout** : `POST /api/auth/logout` révoque le refresh token en base.

### Rate limiting

L'endpoint `/api/auth/login` est protégé par `LoginRateLimitFilter` :
- **5 tentatives** autorisées par adresse IP.
- **Fenêtre** de 60 secondes (token bucket, rechargement automatique).
- Au-delà : `429 Too Many Requests`.

## Logging (AOP)

`ExecutionLoggingAspect` intercepte **tous les appels de service** via `@Around` :
- Log d'entrée : classe, méthode, paramètres (avec redaction automatique des champs `password`, `token`, `secret`).
- Log de sortie : durée d'exécution en ms.
- Log d'erreur : type d'exception + message + durée.

`CorrelationIdFilter` génère un `correlationId` UUID à chaque requête et le propage via MDC, visible dans tous les logs sous `%X{correlationId}`.

## Industrialisation

### Prérequis

- Java 17 ou 21
- Maven 3.9+
- Docker Desktop (ou Docker Engine + Compose plugin)

### Lancer le projet (Docker)

```bash
# Lancement complet (application + PostgreSQL)
docker compose up --build

# Arrêt
docker compose down

# Arrêt + suppression du volume PostgreSQL
docker compose down -v
```

Accès :
- API : `http://localhost:8080`
- Swagger UI : `http://localhost:8080/swagger-ui.html`
- Health : `http://localhost:8080/actuator/health`

### Lancer les tests

```bash
# Build complet + tous les tests (unitaires + intégration)
mvn -B clean verify

# Tests unitaires uniquement
mvn test
```

### Documentation API (OpenAPI / Swagger)

- Swagger UI : `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON : `http://localhost:8080/api-docs`

## CI/CD (GitHub Actions)

Le workflow est défini dans [.github/workflows/ci.yml](.github/workflows/ci.yml).

Comportement :
- Déclenchement sur `push` (toutes branches) et `pull_request` vers `main`
- Matrice Java : **17 et 21**
- Commande : `mvn -B clean verify`
- Upload artifacts (Java 17 uniquement) :
  - `master-annonce-jar` → `target/masterannonce.jar`
  - `jacoco-report` → `target/site/jacoco/`
- Job Docker :
  - Build de l'image et push vers Docker Hub

### Choix base de données en CI — Testcontainers (Option 1)

**Testcontainers** est utilisé pour les tests d'intégration. Au lieu d'une base H2 in-memory, chaque exécution de test démarre un vrai conteneur PostgreSQL via le driver JDBC `jdbc:tc:postgresql:15:///`.

Avantages :
- Reproductible : même base que la production (PostgreSQL, pas H2)
- Fiable : aucun écart de comportement SQL entre environnements
- Aucun service PostgreSQL à déclarer dans le workflow CI — Docker est disponible nativement sur `ubuntu-latest`

Configuration dans [src/test/resources/application-test.yml](src/test/resources/application-test.yml) :
```yaml
spring:
  datasource:
    url: jdbc:tc:postgresql:15:///masterannonce
    driver-class-name: org.testcontainers.jdbc.ContainerDatabaseDriver
```

### Artifacts produits

| Nom | Contenu |
|---|---|
| `master-annonce-jar` | `target/masterannonce.jar` |
| `jacoco-report` | rapport de couverture JaCoCo (`target/site/jacoco/`) |
| `masterannonce-docker-image` | image Docker exportée (uniquement sur `main`) |

## Problèmes rencontrés

### 1. Lazy loading en AOP : log des entités JPA déclenche des requêtes inattendues

**Problème** : L'aspect `ExecutionLoggingAspect` loggait initialement les paramètres via `toString()`. Pour les entités JPA avec des relations `LAZY` (ex: `Annonce` → `author`, `category`), appeler `toString()` dans le log déclenchait involontairement des requêtes SQL hors transaction.

**Solution** : L'aspect ne loggue jamais directement les objets JPA. Il détecte les objets appartenant au package de l'application et les remplace par `<NomDeClasse>`, évitant tout appel à `toString()`.

---

### 2. MapStruct et les relations JPA : reconstruction côté service

**Problème** : MapStruct ne peut pas résoudre automatiquement les entités à partir d'un simple ID (ex: `categoryId` dans `AnnonceCreateDTO` → objet `Category`). Tenter de déléguer ça au mapper produisait soit une erreur de compilation, soit une entité détachée.

**Solution** : La reconstruction des relations (`author`, `category`) est faite explicitement dans le service, après le mapping du DTO vers l'entité via MapStruct. Le mapper ne gère que les champs simples ; le service charge les entités liées via leurs repositories.

---

### 3. Testcontainers : conflit de port avec PostgreSQL local

**Problème** : En développement, PostgreSQL tourne sur le port 5433 (mapping docker-compose). Au lancement des tests, Testcontainers démarre son propre PostgreSQL sur un port aléatoire, mais l'`application.yml` principal pointait sur `localhost:5433`, ce qui causait des échecs si le profil test n'était pas actif.

**Solution** : Création d'un profil de test dédié (`application-test.yml`) avec l'URL Testcontainers (`jdbc:tc:postgresql:15:///masterannonce`). Les classes de test annotées `@ActiveProfiles("test")` utilisent ce profil automatiquement.

---

### 4. Validation du champ `sort` par introspection

**Problème** : L'endpoint `GET /api/annonces?sort=motInventé,asc` ne retournait pas d'erreur claire — Spring Data JPA tentait la requête et Hibernate levait une exception cryptique.

**Solution** : `AnnonceFieldValidator` utilise `Annonce.class.getDeclaredFields()` pour construire la liste des champs valides. Si le champ de tri demandé n'est pas dans cette liste, un `400 Bad Request` est retourné avant même d'appeler le service.

---

### 5. Refresh token et stateless JWT : gestion de l'expiration

**Problème** : En JWT pur stateless, il est impossible de révoquer un token avant son expiration naturelle. Un utilisateur déconnecté pouvait continuer à utiliser son access token.

**Solution** : Le refresh token est persisté en base (`RefreshToken`). L'access token reste court (24h), ce qui limite la fenêtre d'exposition. Le logout révoque le refresh token en base, empêchant le renouvellement. Pour une révocation immédiate de l'access token, il faudrait une liste noire (Redis), non implémentée dans ce TP.

---

### 6. SecurityConfig : ordre des filtres Spring Security

**Problème** : L'ordre initial des filtres (`JwtAuthenticationFilter` avant `CorrelationIdFilter`) faisait que les logs d'erreur d'authentification n'avaient pas de `correlationId` dans le MDC, rendant la corrélation impossible pour les erreurs 401.

**Solution** : `CorrelationIdFilter` est ajouté en premier (`addFilterBefore`), puis `LoginRateLimitFilter`, puis `JwtAuthenticationFilter`. Ainsi, le `correlationId` est toujours injecté dans le MDC avant tout traitement sécurité.

## Collection Postman

La collection Postman est disponible dans `postman/MasterAnnonce.postman_collection.json`.

Elle contient des requêtes organisées par dossier :
- **Auth** : login, refresh token, logout, login invalide
- **Users** : CRUD complet, inscription
- **Categories** : CRUD complet
- **Annonces** : CRUD + publish + archive + filtres multi-critères
- **Security Tests** : 401 sans token, 401 token invalide, 403 rôle insuffisant, 429 rate limit, 400 validation

Le script de login sauvegarde automatiquement l'access token dans la variable `{{token}}` pour les requêtes suivantes.
