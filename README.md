## Architecture

### Structure du projet

```
Prétraitement CSV  (HeureDepartPreprocessor)
       ↓
  Chargement       (CSVDataSource + RowProcessor, Tribuo)
       ↓
  Split 80/20      (TrainTestSplitter, graine 42)
       ↓
  Entraînement     (LogisticRegressionTrainer / CARTClassificationTrainer)
       ↓
  Évaluation       (LabelEvaluator — accuracy, matrice de confusion)
       ↓
  Sérialisation    (ObjectOutputStream — fichier .ser)
       ↓
  Prédiction       (Model.predict sur un exemple manuel)
```

### Organisation des packages

```
src/main/java/com/tp_ia/
├── App.java                     point d'entrée : prétraitement du CSV de livraisons
└── HeureDepartPreprocessor.java convertit heure_depart (HH:mm) en heure_decimal (double)

src/main/resources/
└── livraison_retards_dataset.csv  jeu de données brut (colonnes : id_course, heure_depart,
                                    distance_km, pluie, jour_semaine, vehicule_type, retard)

src/test/java/com/tp_ia/
├── AppTest.java              test de smoke basique
├── LogistiqueLMIATests.java  pipeline complet de prédiction de retard (7 étapes)
└── PluiePredictionTests.java pipeline complet de prédiction de pluie (8 étapes, 2 modèles)
```

### Jeu de données

Le fichier `livraison_retards_dataset.csv` décrit des courses de livraison :

| Colonne | Type | Valeurs |
|---|---|---|
| `id_course` | int | Identifiant unique |
| `heure_depart` | string HH:mm | Heure de départ (ex : `08:30`) |
| `distance_km` | double | Distance en kilomètres |
| `pluie` | catégoriel | `oui` / `non` |
| `jour_semaine` | catégoriel | `lundi`, `mardi`, … `vendredi` |
| `vehicule_type` | catégoriel | `camion`, `camionnette`, `fourgon` |
| `retard` | catégoriel (cible) | `oui` / `non` |

## Pipeline 1 — Prédiction de retard (`LogistiqueLMIATests`)

### Objectif

Prédire si une livraison sera en **retard** (`oui` / `non`) à partir des features de la course.

### Étapes (ordre d'exécution)

| Ordre | Test | Description |
|---|---|---|
| 1 | `prepareDatasets` | Prétraitement : convertit `heure_depart` → `heure_decimal` et écrit le CSV transformé |
| 2 | `loadDatasets` | Chargement du CSV transformé via `CSVDataSource` et `RowProcessor` |
| 3 | `splitTrainTest` | Split 80 % train / 20 % test, graine aléatoire 42 |
| 4 | `training` | Entraînement d'une régression logistique (`LogisticRegressionTrainer`) |
| 5 | `evaluator` | Évaluation sur le jeu de test (accuracy, matrice de confusion) |
| 6 | `saveModel` | Sérialisation du modèle dans `livraison_regressor.ser` |
| 7 | `predictor` | Chargement du modèle et prédiction sur un exemple manuel |

### Features utilisées

| Feature | Encodage Tribuo | Description |
|---|---|---|
| `heure_decimal` | `DoubleFieldProcessor` | Heure de départ en valeur décimale (ex : `08:30` → `8.5`) |
| `distance_km` | `DoubleFieldProcessor` | Distance en km |
| `pluie` | `IdentityProcessor` (one-hot) | Présence de pluie |
| `jour_semaine` | `IdentityProcessor` (one-hot) | Jour de la semaine |
| `vehicule_type` | `IdentityProcessor` (one-hot) | Type de véhicule |

Cible : `retard` (`oui` / `non`), valeur par défaut `non`.

### Exemple de prédiction

```java
example.add(new Feature("distance_km@value",        120.0));
example.add(new Feature("heure_decimal@value",         8.0));
example.add(new Feature("pluie@non",                   0.0));
example.add(new Feature("jour_semaine@mercredi",       2.0));
example.add(new Feature("vehicule_type@camionnette",   1.0));
```

## Pipeline 2 — Prédiction de pluie (`PluiePredictionTests`)

### Objectif

Prédire s'il **pleut** (`oui` / `non`) à partir du jour de la semaine et du statut de retard. Comparaison de deux algorithmes : régression logistique et arbre de décision CART.

### Étapes (ordre d'exécution)

| Ordre | Test | Description |
|---|---|---|
| 1 | `loadDatasets` | Chargement direct du CSV brut (sans prétraitement) |
| 2 | `splitTrainTest` | Split 80 % / 20 %, graine 42 |
| 3 | `trainingLogistic` | Entraînement d'une régression logistique |
| 4 | `trainingCART` | Entraînement d'un arbre de décision CART |
| 5 | `evaluator` | Évaluation et comparaison des deux modèles (accuracy + matrice de confusion) |
| 6 | `saveModel` | Sérialisation du modèle CART dans `pluie_model.ser` |
| 7 | `predictor` | Chargement et prédiction sur un exemple manuel |
| 8 | `interpretationArbre` | Affichage des features utilisées par l'arbre CART |

### Features utilisées

| Feature | Encodage Tribuo | Description |
|---|---|---|
| `jour_semaine` | `IdentityProcessor` (one-hot) | Jour de la semaine |
| `retard` | `IdentityProcessor` (one-hot) | Statut de retard de la livraison |

Cible : `pluie` (`oui` / `non`), valeur par défaut `non`.

### Exemple de prédiction

```java
example.add(new Feature("jour_semaine@vendredi", 1.0));
example.add(new Feature("retard@oui",            1.0));
```

### Comparaison des modèles

| Algorithme | Classe Tribuo | Avantages |
|---|---|---|
| Régression logistique | `LogisticRegressionTrainer` | Simple, rapide, interprétable via les coefficients |
| Arbre de décision CART | `CARTClassificationTrainer` | Interprétable visuellement, capture les non-linéarités |

## Prétraitement — `HeureDepartPreprocessor`

La colonne `heure_depart` au format `HH:mm` n'est pas directement exploitable par un modèle numérique. Le préprocesseur la convertit en `heure_decimal` :

```
08:30  →  8.5
16:45  →  16.75
```

**Formule** : `heures + minutes / 60.0`

La nouvelle colonne `heure_decimal` est insérée juste après `heure_depart` dans le CSV de sortie. Seul le pipeline logistique utilise cette transformation — le pipeline pluie travaille directement sur le CSV brut.

## Industrialisation

### Prérequis

- Java 17
- Maven 3.9+

### Lancer les tests

```bash
# Tous les tests (les deux pipelines)
mvn test

# Un seul pipeline
mvn test -Dtest=LogistiqueLMIATests
mvn test -Dtest=PluiePredictionTests
```

Les fichiers intermédiaires générés pendant les tests (`*_converted.csv`, `*.ser`) sont supprimés automatiquement dans `@AfterAll`.

### Lancer le prétraitement seul (console)

```bash
mvn compile exec:java -Dexec.mainClass="com.tp_ia.App"
```

Produit `livraison_retards_preprocessed.csv` dans `src/main/resources/`.

## Problèmes rencontrés

### 1. Colonne `heure_depart` non numérique

**Problème** : Tribuo ne peut pas ingérer directement une heure au format `HH:mm` avec un `DoubleFieldProcessor`. Tenter de lire `08:30` comme double lève une `NumberFormatException` au chargement du dataset.

**Solution** : `HeureDepartPreprocessor` relit le CSV ligne par ligne, convertit `heure_depart` en `heure_decimal` (double) et écrit un nouveau fichier avec la colonne ajoutée. Le pipeline logistique charge ce fichier transformé, pas le fichier brut.

---

### 2. Encodage one-hot des variables catégorielles avec Tribuo

**Problème** : les colonnes `pluie`, `jour_semaine` et `vehicule_type` sont des chaînes de caractères. Les modèles numériques (régression logistique) n'acceptent que des valeurs numériques.

**Solution** : `IdentityProcessor` de Tribuo crée automatiquement une feature binaire par valeur distincte (`pluie@oui`, `pluie@non`, `jour_semaine@lundi`, etc.). Lors de la prédiction manuelle, il faut nommer les features avec ce suffixe exact (`jour_semaine@vendredi`) et leur donner la valeur `1.0`.

---

### 3. Ordre d'exécution des tests dépendants

**Problème** : les tests d'un même pipeline se dépendent séquentiellement (le split ne peut pas se faire avant le chargement, l'évaluation nécessite le modèle entraîné, etc.). JUnit 5 ne garantit pas d'ordre d'exécution par défaut.

**Solution** : `@TestMethodOrder(MethodOrderer.OrderAnnotation.class)` sur la classe de test, et `@Order(n)` sur chaque méthode. Les champs partagés entre les tests (`dataSource`, `train`, `test`, `model`) sont déclarés `static` pour persister entre les méthodes.

---

### 4. Nettoyage des fichiers générés après les tests

**Problème** : les tests génèrent des fichiers sur le disque (`*_converted.csv`, `*.ser`). Sans nettoyage, ils s'accumulent entre les exécutions et peuvent fausser un rechargement de modèle (fichier `.ser` d'un entraînement précédent réutilisé involontairement).

**Solution** : `@AfterAll` supprime les fichiers générés après chaque suite de tests. La suppression est elle-même vérifiée par un `assertTrue(deleted)` pour détecter un échec de nettoyage.

---

### 5. Graine aléatoire pour la reproductibilité du split

**Problème** : sans graine fixe, le `TrainTestSplitter` produit un split différent à chaque exécution, rendant les résultats d'accuracy non reproductibles et les comparaisons entre modèles impossibles.

**Solution** : le splitter est initialisé avec une graine fixe (`42L`) dans les deux pipelines. Les proportions train/test (80 %/20 %) et les exemples dans chaque ensemble sont identiques à chaque exécution.

---

### 6. Comparaison logistique vs CART sur un dataset peu corrélé

**Problème** : `pluie` ne dépend pas fortement de `jour_semaine` et `retard` dans le dataset de livraisons. Les deux modèles risquent d'atteindre la même accuracy (proche du taux de classe majoritaire), rendant la comparaison peu informative.

**Solution** : le test `evaluator` affiche la matrice de confusion complète et l'accuracy de chaque modèle. Le test `interpretationArbre` liste les features effectivement utilisées par l'arbre CART, ce qui permet de vérifier si le modèle a trouvé des splits significatifs ou s'il prédit uniquement la classe majoritaire.
