## Architecture

Le projet suit une architecture :

```
Presentation (Servlets / JSP)
        |
   Service (logique métier)
        |
   DAO (accès aux données via JPA/Hibernate)
        |
   Base de données (PostgreSQL)
```

### Couche Model

Trois entités JPA avec leurs relations :
- **User** : compte utilisateur (username, email, password) — relation `@OneToMany` vers Annonce
- **Annonce** : annonce avec titre, description, adresse, statut (DRAFT / PUBLISHED / ARCHIVED) — relations `@ManyToOne` vers User et Category
- **Category** : catégorie d'annonce (label unique) — relation `@OneToMany` vers Annonce

### Couche DAO

Un **GenericDAO\<T, ID\>** factorise les opérations CRUD et le filtrage dynamique (construction de requêtes JPQL avec filtres, recherche par mot-clé, pagination, tri et jointures). Les DAOs spécialisés (`UserDAO`, `AnnonceDAO`, `CategoryDAO`) héritent de ce GenericDAO sans code supplémentaire.

### Couche Service

Chaque service gère les transactions JPA (begin / commit / rollback) et applique les règles métier :
- **UserService** : création, authentification, unicité username/email, changement de mot de passe
- **AnnonceService** : CRUD, publication, archivage, recherche par mot-clé/catégorie/auteur avec pagination
- **CategoryService** : CRUD avec interdiction de supprimer une catégorie contenant des annonces

### Couche Controller

10 servlets gèrent les différentes pages :
- **HomeServlet** (`/`, `/home`) : liste des annonces publiées avec pagination, filtre par catégorie et recherche
- **LoginServlet / LogoutServlet / RegisterServlet** : authentification et inscription
- **AnnonceCreateServlet / AnnonceEditServlet / AnnonceDetailServlet / AnnonceDeleteServlet** : CRUD annonces
- **AnnonceStatusServlet** : changement de statut (publier / archiver)
- **MesAnnoncesServlet** : annonces de l'utilisateur connecté

### Sécurité

- **AuthFilter** : intercepte les URLs protégées (`/annonces/*`, `/mes-annonces`, `/annonce/*`) et redirige vers le login si l'utilisateur n'est pas authentifié
- Validation des entrées avec Hibernate Validator (`@NotBlank`, `@Email`, `@Size`)
- Protection contre l'injection SQL dans le GenericDAO (validation des noms de champs par regex)

### Tests

- **Tests unitaires** : JUnit 5 + Mockito pour les services et controllers (mock de l'EntityManager)
- **Tests d'intégration** : base H2 en mémoire avec un persistence unit dédié
- **Tests de workflow** : création → publication → archivage

## Problèmes rencontrés

1. **Gestion manuelle de l'EntityManager** : Chaque service doit gérer lui-même le cycle de vie de l'EntityManager (ouverture, transaction, fermeture). Cela entraînait des fuites de connexions et des `LazyInitializationException` quand les entités étaient accédées en dehors d'une session active.

2. **Duplication de code dans les DAOs** : chaque DAO (User, Annonce, Category) répétait les mêmes opérations CRUD, rendant le code difficile à maintenir et source d'incohérences.

3. **Filtrage dynamique des requêtes** : construire des requêtes JPQL avec des filtres optionnels (mot-clé, catégorie, statut, pagination) de manière sécurisée et flexible était complexe à implémenter proprement.

4. **Sécurisation des routes** : protéger certaines URLs tout en laissant les pages publiques accessibles (accueil, login, inscription) nécessitait une gestion fine des filtres de servlets.

5. **LazyInitializationException** : les relations `@ManyToOne(fetch = FetchType.LAZY)` sur `Annonce.author` et `Annonce.category` créent des objets proxy. Dès qu'on ferme l'EntityManager et qu'on tente d'accéder à `annonce.getAuthor().getUsername()`, Hibernate lève une `LazyInitializationException` car le proxy ne peut plus charger les données.

6. **Problème N+1 requêtes** : en chargeant une liste de N annonces sans précaution, chaque accès à `annonce.getAuthor()` déclenchait une requête SQL supplémentaire. Pour 5 annonces, cela générait 1 + 5 = 6 requêtes au lieu d'une seule.

7. **`persist()` vs `merge()` — entité détachée** : appeler `em.persist()` sur une entité déjà détachée (qui a un ID existant) lève une `PersistenceException`. Inversement, appeler `em.merge()` sur une entité nouvelle sans `@GeneratedValue` correctement configuré peut créer des doublons au lieu de mettre à jour.

8. **`em.remove()` sur entité détachée** : tenter de supprimer une entité qui n'est pas gérée par le contexte de persistence courant provoque une `IllegalArgumentException`. Il faut d'abord rattacher l'entité avec `merge()` avant de pouvoir la supprimer.

9. **Cascade et orphanRemoval** : la relation `User.annonces` avec `cascade = CascadeType.ALL` et `orphanRemoval = true` signifie que la suppression d'un User supprime automatiquement toutes ses annonces. Sans cette configuration, la suppression échouait avec une violation de contrainte de clé étrangère. Mais mal configurée, elle peut supprimer des données involontairement.

## Solutions apportées

1. **Pattern utilitaire EntityManagerUtil** : centralisation de la création de l'EntityManagerFactory dans un singleton, avec initialisation via `AppContextListener` au démarrage de l'application et fermeture propre à l'arrêt. Chaque service ouvre et ferme son EntityManager dans un bloc `try-finally` pour éviter les fuites.

2. **GenericDAO générique** : mise en place d'un DAO générique paramétré (`GenericDAO<T, ID>`) qui factorise toutes les opérations CRUD. Les DAOs spécialisés héritent sans ajouter de code, éliminant la duplication.

3. **Construction dynamique de JPQL** : le GenericDAO construit les requêtes dynamiquement en ajoutant les clauses `WHERE`, `JOIN`, `ORDER BY` et `LIKE` selon les filtres passés en paramètre. Les noms de champs sont validés par regex pour prévenir les injections SQL.

4. **AuthFilter avec redirection intelligente** : le filtre sauvegarde l'URL demandée avant de rediriger vers le login, puis redirige l'utilisateur vers sa destination initiale après authentification. Les pages publiques sont explicitement exclues du filtrage.

5. **JOIN FETCH pour le Lazy Loading** : toutes les requêtes du `AnnonceService` utilisent `LEFT JOIN FETCH e.author LEFT JOIN FETCH e.category` pour charger les relations en une seule requête SQL. Cela permet d'accéder aux données de l'auteur et de la catégorie même après fermeture de l'EntityManager, et élimine le problème N+1.

6. **Résolution du N+1** : le `DISTINCT` dans le `SELECT` du GenericDAO (ajouté automatiquement quand un JOIN FETCH est présent) évite les doublons causés par les jointures. Combinée au JOIN FETCH, une seule requête SQL charge les annonces avec leurs relations.

7. **Séparation `save()` / `update()` dans le GenericDAO** : `save()` utilise `em.persist()` pour les nouvelles entités, `update()` utilise `em.merge()` pour les entités existantes.

8. **Vérification `em.contains()` avant `remove()`** : dans la méthode `delete()` du GenericDAO, si l'entité n'est pas dans le contexte de persistence courant, elle est d'abord rattachée via `merge()` avant d'être supprimée, évitant l'`IllegalArgumentException`.

9. **Configuration fine des cascades** : `CascadeType.ALL` + `orphanRemoval = true` sur `User.annonces` pour la suppression en cascade. La relation `Category.annonces` n'a pas de cascade, et le `CategoryService` vérifie qu'aucune annonce n'est liée avant de permettre la suppression d'une catégorie.
