# MasterAnnonce - TP R6

## Architecture

### Architecture générale

Le projet suit une architecture :

```
┌──────────────────────────────────────────────────┐
│                  Couche Web                       │
│   Servlets (@WebServlet) + JSP + AuthFilter       │
├──────────────────────────────────────────────────┤
│                Couche Service                     │
│   UserService / AnnonceService / CategoryService  │
├──────────────────────────────────────────────────┤
│                Couche DAO                         │
│   GenericDAO<T,ID> → UserDAO / AnnonceDAO / ...   │
├──────────────────────────────────────────────────┤
│              Base de données                      │
│   PostgreSQL (prod) / H2 en mémoire (tests)       │
└──────────────────────────────────────────────────┘
```

### Architecture des tests (4 niveaux)

```
src/test/java/
└── org/.../tp1/
    ├── dao/                          # Niveau 1 - Tests DAO (intégration)
    │   ├── UserDAOTest.java
    │   ├── AnnonceDAOTest.java
    │   └── CategoryDAOTest.java
    ├── service/                      # Niveau 2 - Tests Service (unitaires)
    │   ├── UserServiceTest.java
    │   ├── AnnonceServiceTest.java
    │   └── CategoryServiceTest.java
    ├── integration/                  # Niveau 3 - Tests d'intégration métier
    │   ├── AnnonceWorkflowIntegrationTest.java
    │   └── LazyLoadingTest.java
    └── controller/                   # Niveau 4 - Tests Web
        ├── AuthFilterTest.java
        ├── LoginServletTest.java
        └── LogoutServletTest.java
```

**Niveau 1 (DAO)** : Tests d'intégration avec H2 en mémoire. On vérifie les opérations CRUD, la recherche par filtres, la pagination et le tri directement contre une vraie base.

**Niveau 2 (Service)** : Tests unitaires avec Mockito. On mocke `EntityManagerUtil` via `mockStatic` pour injecter un EntityManager H2 contrôlé. On teste les règles métier (unicité username/email, workflow de publication, protection des catégories).

**Niveau 3 (Intégration)** : Tests de bout en bout du workflow métier complet (inscription → création annonce → publication → recherche)

**Niveau 4 (Web)** : Tests des Servlets et du filtre d'authentification avec des mocks HTTP (`HttpServletRequest`, `HttpServletResponse`, `HttpSession`, `FilterChain`).

### Technologies de test

| Outil | Rôle |
|-------|------|
| JUnit 5 | Framework de tests |
| Mockito 5 | Mocking (mocks HTTP, mockStatic pour EntityManagerUtil) |
| H2 Database | Base en mémoire remplaçant PostgreSQL pour les tests |

---

## Problèmes rencontrés et solutions

### 1. EntityManagerUtil

**Problème** : Les services appellent directement `EntityManagerUtil.getEntityManager()` (méthode statique). Il est impossible d'injecter un mock classique car les DAOs sont instanciés avec `new` à l'intérieur des services.

**Solution** : Utilisation de `Mockito.mockStatic(EntityManagerUtil.class)` pour intercepter l'appel statique et retourner un EntityManager connecté à H2 au lieu de PostgreSQL. Cela permet de tester les services sans modifier le code de production.

```java
mockedUtil = mockStatic(EntityManagerUtil.class);
mockedUtil.when(EntityManagerUtil::getEntityManager)
    .thenAnswer(inv -> emf.createEntityManager());
```

### 2. LazyInitializationException

**Problème** : Les relations `@ManyToOne` de l'entité `Annonce` (vers `User` et `Category`) sont configurées en `FetchType.LAZY`. Quand on charge une annonce puis qu'on ferme l'EntityManager, tout accès ultérieur à `annonce.getAuthor().getUsername()` lève une `LazyInitializationException` car le proxy Hibernate ne peut plus charger les données sans session active.

**Solution** : Utilisation des `LEFT JOIN FETCH` dans les requêtes JPQL pour forcer le chargement des relations au moment de la requête.

```java
// SANS JOIN FETCH → LazyInitializationException après em.close()
annonceDAO.findWithFilters(em, filters, null, null, "title ASC", null);

// AVEC JOIN FETCH → les relations sont disponibles même après em.close()
annonceDAO.findWithFilters(em, filters, null, null, "title ASC",
    "LEFT JOIN FETCH e.author LEFT JOIN FETCH e.category");
```

### 3. Problème N+1 queries

**Problème** : Sans `JOIN FETCH`, charger N annonces puis accéder à l'auteur de chacune génère 1 requête pour les annonces + N requêtes supplémentaires pour chaque auteur. Pour 100 annonces, cela fait 101 requêtes SQL au lieu d'une seule.

**Solution** : Le `JOIN FETCH` résout aussi ce problème car tout est chargé en une seule requête SQL avec des jointures.

### 4. Mapping JPA - Gestion des relations bidirectionnelles

**Problème** : La relation `User` ↔ `Annonce` est bidirectionnelle (`@OneToMany` / `@ManyToOne`). Le côté propriétaire est `Annonce` (via `@JoinColumn`). Si on ne définit pas correctement `mappedBy` côté `User`, Hibernate crée une table de jointure inutile ou duplique les colonnes.

**Solution** : Dans les `@AfterEach` des tests, on supprime d'abord les annonces puis les utilisateurs/catégories pour respecter les contraintes de clé étrangère :

```java
em.createQuery("DELETE FROM Annonce").executeUpdate();   // D'abord les enfants
em.createQuery("DELETE FROM Category").executeUpdate();   // Puis les parents
em.createQuery("DELETE FROM User").executeUpdate();
```