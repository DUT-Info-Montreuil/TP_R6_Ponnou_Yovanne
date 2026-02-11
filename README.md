## Architecture

Le projet suit une architecture :

```
Presentation (Servlets / JSP)
        |
   Service (logique metier)
        |
   DAO (acces aux donnees via JPA/Hibernate)
        |
   Base de donnees (PostgreSQL)
```

### Couche Model

Trois entites JPA avec leurs relations :
- **User** : compte utilisateur (username, email, password) — relation `@OneToMany` vers Annonce
- **Annonce** : annonce avec titre, description, adresse, statut (DRAFT / PUBLISHED / ARCHIVED) — relations `@ManyToOne` vers User et Category
- **Category** : categorie d'annonce (label unique) — relation `@OneToMany` vers Annonce
- **AnnonceStatus** : enum definissant le cycle de vie d'une annonce

### Couche DAO

Un **GenericDAO\<T, ID\>** factorise les operations CRUD et le filtrage dynamique (construction de requetes JPQL avec filtres, recherche par mot-cle, pagination, tri et jointures). Les DAOs specialises (`UserDAO`, `AnnonceDAO`, `CategoryDAO`) heritent de ce GenericDAO sans code supplementaire.

### Couche Service

Chaque service gere les transactions JPA (begin / commit / rollback) et applique les regles metier :
- **UserService** : creation, authentification, unicite username/email, changement de mot de passe
- **AnnonceService** : CRUD, publication, archivage, recherche par mot-cle/categorie/auteur avec pagination
- **CategoryService** : CRUD avec interdiction de supprimer une categorie contenant des annonces

Les services retournent des `Optional<T>` pour gerer proprement l'absence de resultat.

### Couche Controller

10 servlets gerent les differentes pages :
- **HomeServlet** (`/`, `/home`) : liste des annonces publiees avec pagination, filtre par categorie et recherche
- **LoginServlet / LogoutServlet / RegisterServlet** : authentification et inscription
- **AnnonceCreateServlet / AnnonceEditServlet / AnnonceDetailServlet / AnnonceDeleteServlet** : CRUD annonces
- **AnnonceStatusServlet** : changement de statut (publier / archiver)
- **MesAnnoncesServlet** : annonces de l'utilisateur connecte

### Securite

- **AuthFilter** : intercepte les URLs protegees (`/annonces/*`, `/mes-annonces`, `/annonce/*`) et redirige vers le login si l'utilisateur n'est pas authentifie
- Validation des entrees avec Hibernate Validator (`@NotBlank`, `@Email`, `@Size`)
- Protection contre l'injection SQL dans le GenericDAO (validation des noms de champs par regex)

### Tests

- **Tests unitaires** : JUnit 5 + Mockito pour les services et controllers (mock de l'EntityManager)
- **Tests d'integration** : base H2 en memoire avec un persistence unit dedie
- **Tests de workflow** : creation → publication → archivage

## Problemes rencontres

1. **Gestion manuelle de l'EntityManager** : Chaque service doit gerer lui-meme le cycle de vie de l'EntityManager (ouverture, transaction, fermeture). Cela entrainait des fuites de connexions et des `LazyInitializationException` quand les entites etaient accédées en dehors d'une session active.

2. **Duplication de code dans les DAOs** : chaque DAO (User, Annonce, Category) repetait les memes operations CRUD, rendant le code difficile a maintenir et source d'incoherences.

3. **Filtrage dynamique des requetes** : construire des requetes JPQL avec des filtres optionnels (mot-cle, categorie, statut, pagination) de maniere securisee et flexible etait complexe a implementer proprement.

4. **Securisation des routes** : proteger certaines URLs tout en laissant les pages publiques accessibles (accueil, login, inscription) necessitait une gestion fine des filtres de servlets.

5. **LazyInitializationException** : les relations `@ManyToOne(fetch = FetchType.LAZY)` sur `Annonce.author` et `Annonce.category` creent des objets proxy. Des qu'on ferme l'EntityManager et qu'on tente d'acceder a `annonce.getAuthor().getUsername()`, Hibernate leve une `LazyInitializationException` car le proxy ne peut plus charger les donnees.

6. **Probleme N+1 requetes** : en chargeant une liste de N annonces sans precaution, chaque acces a `annonce.getAuthor()` declenchait une requete SQL supplementaire. Pour 5 annonces, cela generait 1 + 5 = 6 requetes au lieu d'une seule.

7. **`persist()` vs `merge()` — entite detachee** : appeler `em.persist()` sur une entite deja detachee (qui a un ID existant) leve une `PersistenceException`. Inversement, appeler `em.merge()` sur une entite nouvelle sans `@GeneratedValue` correctement configure peut creer des doublons au lieu de mettre a jour.

8. **`em.remove()` sur entite detachee** : tenter de supprimer une entite qui n'est pas geree par le contexte de persistence courant provoque une `IllegalArgumentException`. Il faut d'abord rattacher l'entite avec `merge()` avant de pouvoir la supprimer.

9. **Cascade et orphanRemoval** : la relation `User.annonces` avec `cascade = CascadeType.ALL` et `orphanRemoval = true` signifie que la suppression d'un User supprime automatiquement toutes ses annonces. Sans cette configuration, la suppression echouait avec une violation de contrainte de cle etrangere. Mais mal configuree, elle peut supprimer des donnees involontairement.

## Solutions apportees

1. **Pattern utilitaire EntityManagerUtil** : centralisation de la creation de l'EntityManagerFactory dans un singleton, avec initialisation via `AppContextListener` au demarrage de l'application et fermeture propre a l'arret. Chaque service ouvre et ferme son EntityManager dans un bloc `try-finally` pour eviter les fuites.

2. **GenericDAO generique** : mise en place d'un DAO generique parametre (`GenericDAO<T, ID>`) qui factorise toutes les operations CRUD. Les DAOs specialises heritent sans ajouter de code, eliminant la duplication.

3. **Construction dynamique de JPQL** : le GenericDAO construit les requetes dynamiquement en ajoutant les clauses `WHERE`, `JOIN`, `ORDER BY` et `LIKE` selon les filtres passes en parametre. Les noms de champs sont valides par regex pour prevenir les injections SQL.

4. **AuthFilter avec redirection intelligente** : le filtre sauvegarde l'URL demandee avant de rediriger vers le login, puis redirige l'utilisateur vers sa destination initiale apres authentification. Les pages publiques sont explicitement exclues du filtrage.

5. **JOIN FETCH pour le Lazy Loading** : toutes les requetes du `AnnonceService` utilisent `LEFT JOIN FETCH e.author LEFT JOIN FETCH e.category` pour charger les relations en une seule requete SQL. Cela permet d'acceder aux donnees de l'auteur et de la categorie meme apres fermeture de l'EntityManager, et elimine le probleme N+1.

6. **Resolution du N+1** : le `DISTINCT` dans le `SELECT` du GenericDAO (ajoute automatiquement quand un JOIN FETCH est present) evite les doublons causes par les jointures. Combinee au JOIN FETCH, une seule requete SQL charge les annonces avec leurs relations.

7. **Separation `save()` / `update()` dans le GenericDAO** : `save()` utilise `em.persist()` pour les nouvelles entites, `update()` utilise `em.merge()` pour les entites existantes. Cette separation explicite evite les confusions entre creation et mise a jour.

8. **Verification `em.contains()` avant `remove()`** : dans la methode `delete()` du GenericDAO, si l'entite n'est pas dans le contexte de persistence courant, elle est d'abord rattachee via `merge()` avant d'etre supprimee, evitant l'`IllegalArgumentException`.

9. **Configuration fine des cascades** : `CascadeType.ALL` + `orphanRemoval = true` sur `User.annonces` pour la suppression en cascade. La relation `Category.annonces` n'a pas de cascade, et le `CategoryService` verifie qu'aucune annonce n'est liee avant de permettre la suppression d'une categorie.
