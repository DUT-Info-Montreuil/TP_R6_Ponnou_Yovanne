# TP1_R6_Ponnou_Yovanne

## Architecture

Le projet suit une architecture :

```
Présentation (Servlets / JSP)
        |
   DAO (accès aux données via JDBC)
        |
   Base de données (SQLite)
```

### Couche Model

Une seule entité POJO (sans annotations JPA) :
- **Annonce** : annonce avec titre, description, adresse, mail et date de création — aucune relation vers d'autres entités

### Couche DAO

Un **DAO\<T\>** abstrait définit le contrat CRUD (find, findAll, create, update, delete). Le **AnnonceDAO** implémente ces opérations avec des requêtes JDBC préparées (PreparedStatement) et un accès à la base via le singleton `ConnectionDB`.

### Couche Controller

5 servlets gèrent les différentes pages :
- **HelloServlet** (`/hello`) : page d'accueil avec formulaire de salutation
- **AnnonceList** (`/AnnonceList`) : liste de toutes les annonces triées par date décroissante
- **AnnonceAdd** (`/AnnonceAdd`) : formulaire de création d'une annonce avec validation des champs
- **AnnonceUpdate** (`/AnnonceUpdate`) : formulaire de modification d'une annonce existante
- **AnnonceDelete** (`/AnnonceDelete`) : suppression d'une annonce avec confirmation JavaScript

### Sécurité

- Utilisation de `PreparedStatement` dans le DAO pour les injections SQL
- Utilisation de `<c:out>` dans les JSP pour les failles XSS
- Validation basique des champs (non vide) côté serveur dans les servlets

## Problèmes rencontrés et solutions apportées

1. **Connexion unique partagée (Singleton)** : le `ConnectionDB` utilise une seule connexion JDBC statique partagée entre toutes les requêtes. En cas d'accès concurrent, cela peut provoquer des conflits et des erreurs de type `SQLiteBusyException`, car SQLite ne supporte pas bien les écritures simultanées.
   - **Solution** : le singleton `ConnectionDB` vérifie si la connexion est fermée (`isClosed()`) avant de la réutiliser et la recrée si nécessaire. De plus, le schéma est créé automatiquement (`CREATE TABLE IF NOT EXISTS`), ce qui simplifie le déploiement initial.

2. **Suppression via GET (contrainte JSP)** : le servlet `AnnonceDelete` utilise `doGet()` pour effectuer la suppression, car en JSP/HTML standard il n’existe pas de requête **DELETE** “native”. Un lien `<a href="...">` déclenche uniquement une requête **GET**, et un formulaire HTML ne supporte nativement que **GET** ou **POST**.
   - **Solution mise en place** : ajout d’une confirmation JavaScript `confirm()` dans `AnnonceList.jsp` afin de réduire les suppressions accidentelles.


3. **Pas de gestion des transactions** : les opérations JDBC sont exécutées en auto-commit. En cas d'erreur à mi-parcours d'une opération complexe, il n'y a pas de rollback possible, ce qui peut laisser la base dans un état incohérent.
   - **Solution** : les opérations CRUD restent simples (une seule requête par action), ce qui limite le risque d'incohérence.

4. **Gestion d'erreurs générique** : toutes les méthodes du DAO déclarent `throws Exception`, ce qui masque la nature réelle des erreurs (SQL, connexion, données invalides) et empêche un traitement différencié dans les servlets.
   - **Solution** : les servlets effectuent une validation côté serveur (champs non vides) dans `AnnonceAdd` et `AnnonceUpdate` avant d'appeler le DAO, avec un message d'erreur renvoyé au formulaire. Cela réduit les cas d'erreurs SQL liées à des données invalides.