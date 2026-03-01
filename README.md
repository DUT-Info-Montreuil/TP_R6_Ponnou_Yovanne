## Architecture

### Structure en couches

```
REST Controller  (BlockchainController, Spring MVC)
       ↓
   Service        (BlockchainService, logique métier)
       ↓
  Blockchain      (gestionnaire de la chaîne en mémoire)
       ↓
    Block          (unité immuable avec hash SHA-256)
```

### Organisation des packages

```
src/main/java/com/tp_blockchain_ponnou_yovanne/
├── App.java                    point d'entrée Spring Boot
├── ApplicationBlockChain.java  démo console (POW, POS, PBFT, POA, export JSON)
├── Block.java                  bloc immuable : index, timestamp, data, previousHash, nonce, consensus, validator, hash
├── Blockchain.java             gestionnaire de la chaîne : ajout, validation, export JSON
├── BlockchainController.java   endpoints REST : GET /chain, GET /validate, POST /block
├── BlockchainService.java      délégation vers Blockchain selon le type de consensus
├── TicketData.java             modèle de données : eventId, artist, status, owner
├── ValidatorNode.java          nœud validateur : id, stake, authority, honest
└── OpenApiConfig.java          configuration SpringDoc / Swagger
```

### Modèle de données

#### Block

| Champ | Type | Description |
|---|---|---|
| `index` | int | Position dans la chaîne |
| `timestamp` | String | Horodatage ISO-8601 (Instant) |
| `data` | TicketData | Données du ticket d'événement |
| `previousHash` | String | Hash SHA-256 du bloc précédent |
| `nonce` | long | Compteur utilisé par le PoW |
| `consensus` | String | Mécanisme utilisé : BASIC, POW, POS, PBFT, POA, GENESIS |
| `validator` | String | Identifiant du validateur (ex: `Validator-A`, `MINER`, `PBFT-CLUSTER`) |
| `hash` | String | Hash SHA-256 calculé à la construction |

#### TicketData

| Champ | Exemple |
|---|---|
| `eventId` | `EVT-2026-001` |
| `artist` | `Coldplay` |
| `status` | `PURCHASED`, `RESOLD`, `USED`, `INVALID` |
| `owner` | `Alice` |

#### ValidatorNode

| Champ | Description |
|---|---|
| `id` | Identifiant du nœud (ex: `Validator-A`) |
| `stake` | Poids dans la sélection PoS |
| `authority` | Autorisé à valider en PoA |
| `honest` | Vote oui en PBFT |

### Endpoints REST

Base URL : `http://localhost:8080/api/blockchain`

| Verbe | URI | Description |
|---|---|---|
| `GET` | `/chain` | Retourne la liste complète des blocs |
| `GET` | `/validate` | Vérifie l'intégrité de la chaîne |
| `POST` | `/block` | Ajoute un bloc avec un type de consensus |

#### Corps JSON pour `POST /block`

```json
{
  "eventId": "EVT-2026-001",
  "artist": "Coldplay",
  "status": "PURCHASED",
  "owner": "Alice",
  "consensus": "POW"
}
```

Valeurs `consensus` supportées :

| Valeur | Mécanisme |
|---|---|
| `BASIC` | Ajout direct sans validation supplémentaire (défaut si absent) |
| `POW` | Proof of Work — minage avec difficulté 4 (hash commençant par `0000`) |
| `POS` | Proof of Stake — sélection aléatoire pondérée par le stake des validateurs |
| `PBFT` | Practical Byzantine Fault Tolerance — vote majoritaire des nœuds honnêtes |
| `POA` | Proof of Authority — round-robin sur les validateurs ayant `authority = true` |

### Mécanismes de consensus

#### Proof of Work (PoW)

Le hash du bloc doit commencer par `powDifficulty` zéros (difficulté = 4). La méthode `Block.mineProofOfWork()` incrémente le `nonce` jusqu'à trouver un hash valide.

```
while (!hash.startsWith("0000")) { nonce++; }
```

La validation `isChainValid()` vérifie que les blocs PoW respectent toujours le préfixe requis.

#### Proof of Stake (PoS)

Un validateur est sélectionné aléatoirement, proportionnellement à son `stake` :

| Validateur | Stake | Probabilité |
|---|---|---|
| Validator-A | 50 | 50 % |
| Validator-B | 30 | 30 % |
| Validator-C | 15 | 15 % |
| Validator-D | 5 | 5 % |

#### Practical Byzantine Fault Tolerance (PBFT)

Avec `n = 4` nœuds et `f = (n-1)/3 = 1` nœud byzantin toléré, le bloc est accepté si au moins `2f+1 = 3` nœuds votent oui (`honest = true`). Avec 3 nœuds honnêtes sur 4, le consensus est atteint.

#### Proof of Authority (PoA)

Seuls les nœuds avec `authority = true` peuvent valider. La sélection se fait en round-robin selon l'index du bloc dans la chaîne.

### Validation de la chaîne

`isChainValid()` parcourt chaque bloc depuis l'index 1 et vérifie :

1. Le hash recalculé correspond au hash stocké.
2. Le `previousHash` correspond au hash du bloc précédent.
3. Pour les blocs PoW : le hash commence bien par le préfixe requis.
4. Le bloc GENESIS est cohérent.

## Industrialisation

### Prérequis

- Java 17
- Maven 3.9+

### Lancer le projet

```bash
mvn spring-boot:run
```

Accès :
- API : `http://localhost:8080`
- Swagger UI : `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON : `http://localhost:8080/v3/api-docs`

### Lancer la démo console

```bash
mvn exec:java -Dexec.mainClass="com.tp_blockchain_ponnou_yovanne.ApplicationBlockChain"
```

La démo ajoute 5 blocs (POW, POS, PBFT, POA, POW), affiche la chaîne et exporte un fichier `blockchain.json`.

### Exemples curl

```bash
# Récupérer la chaîne complète
curl -X GET http://localhost:8080/api/blockchain/chain

# Vérifier l'intégrité
curl -X GET http://localhost:8080/api/blockchain/validate

# Ajouter un bloc en Proof of Work
curl -X POST http://localhost:8080/api/blockchain/block \
  -H "Content-Type: application/json" \
  -d '{"eventId":"EVT-2026-001","artist":"Coldplay","status":"PURCHASED","owner":"Alice","consensus":"POW"}'

# Ajouter un bloc en Proof of Stake
curl -X POST http://localhost:8080/api/blockchain/block \
  -H "Content-Type: application/json" \
  -d '{"eventId":"EVT-2026-001","artist":"Coldplay","status":"RESOLD","owner":"Bob","consensus":"POS"}'
```

## Problèmes rencontrés

### 1. Immuabilité du bloc et cohérence du hash

**Problème** : le hash d'un bloc doit être calculé une seule fois, à la construction, à partir de tous ses champs. Toute modification ultérieure d'un champ rendrait le hash incohérent avec le contenu réel du bloc.

**Solution** : tous les champs de `Block` sont `final`. Le hash est calculé dans le constructeur privé et stocké dans `this.hash`. La méthode `calculateHash()` reste publique uniquement pour permettre à `isChainValid()` de recalculer et comparer sans modifier l'état.

---

### 2. Calcul de hash déterministe pour TicketData

**Problème** : utiliser `toString()` ou la sérialisation par défaut de `TicketData` dans le calcul du hash pouvait produire des valeurs instables (ordre des champs, caractères spéciaux, implémentation de `toString()`).

**Solution** : `TicketData.canonicalValue()` produit une représentation stable `eventId|artist|status|owner` utilisée exclusivement dans le calcul du hash, indépendante de tout framework de sérialisation.

---

### 3. Preuve de travail : timestamp figé pendant le minage

**Problème** : si le timestamp était pris à chaque itération de la boucle de minage, deux exécutions successives produiraient des hashs différents pour le même bloc, rendant la validation impossible.

**Solution** : `Block.mineProofOfWork()` capture le timestamp au début du minage (`Instant.now().toString()`) et l'utilise pour toutes les itérations. Le constructeur privé accepte un timestamp explicite pour garantir la cohérence entre la boucle de recherche et le bloc final créé.

---

### 4. PBFT : condition de consensus avec des nœuds byzantins

**Problème** : avec 4 nœuds dont 1 byzantin possible, la formule naïve `votes > n/2` n'est pas suffisante pour garantir la tolérance aux fautes byzantines.

**Solution** : la formule correcte est `requiredVotes = 2f + 1` avec `f = (n-1)/3`. Avec `n = 4`, `f = 1` et `requiredVotes = 3`. Le bloc est refusé si moins de 3 nœuds honnêtes votent, ce qui protège contre 1 nœud byzantin.

---

### 5. Validation PoW dans isChainValid()

**Problème** : un bloc PoW dont le hash avait été forcé (ou corrompu) pouvait passer la vérification de cohérence (`hash == calculateHash()`) si la valeur corrompue était enregistrée dans le champ `hash` du bloc.

**Solution** : `isChainValid()` applique deux niveaux de vérification pour les blocs PoW : d'abord la cohérence du hash recalculé (`current.hash.equals(current.calculateHash())`), puis le respect du préfixe de difficulté (`current.hash.startsWith("0000")`). Un bloc avec un hash valide mais ne respectant pas la difficulté est rejeté.

---

### 6. Spring Boot : bean Blockchain partagé entre requêtes

**Problème** : la `Blockchain` est instanciée directement dans `BlockchainService` comme champ d'instance. Si le service était prototypé ou recréé, chaque requête aurait sa propre chaîne, perdant l'historique.

**Solution** : `BlockchainService` est un bean `@Service` (singleton Spring par défaut). La `Blockchain` est un champ `final` initialisé dans le constructeur implicite du service. Toutes les requêtes partagent ainsi la même instance de blockchain pour toute la durée de vie de l'application.
