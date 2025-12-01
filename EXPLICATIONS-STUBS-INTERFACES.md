# 📚 Guide complet : Stubs gRPC et Interfaces

## 🎯 Question 1 : Où sont les stubs ?

### Réponse courte
Les **stubs sont générés automatiquement** par le compilateur Protocol Buffers et se trouvent dans :
```
commun/target/generated-sources/protobuf/grpc-java/org/tp1/commun/grpc/
```

### Structure détaillée

```
commun/
└── target/
    ├── generated-sources/
    │   └── protobuf/
    │       ├── grpc-java/           ← STUBS gRPC (services)
    │       │   ├── org/tp1/commun/grpc/hotel/
    │       │   │   └── HotelServiceGrpc.java        ★ STUB PRINCIPAL
    │       │   └── org/tp1/commun/grpc/agence/
    │       │       └── AgenceServiceGrpc.java       ★ STUB PRINCIPAL
    │       │
    │       └── java/                ← Messages Protocol Buffers
    │           ├── org/tp1/commun/grpc/hotel/
    │           │   ├── ChambreMessage.java
    │           │   ├── RechercheRequest.java
    │           │   ├── RechercheResponse.java
    │           │   ├── ReservationRequest.java
    │           │   ├── ReservationResponse.java
    │           │   ├── HotelInfoRequest.java
    │           │   └── HotelInfoResponse.java
    │           │
    │           └── org/tp1/commun/grpc/agence/
    │               ├── PingRequest.java
    │               ├── PingResponse.java
    │               └── AgenceReservationRequest.java
    │
    └── classes/                     ← Fichiers .class compilés
        └── org/tp1/commun/grpc/
            ├── hotel/
            │   ├── HotelServiceGrpc.class
            │   ├── HotelServiceGrpc$1.class
            │   ├── HotelServiceGrpc$HotelServiceStub.class            ← Stub asynchrone
            │   ├── HotelServiceGrpc$HotelServiceBlockingStub.class    ← Stub synchrone (bloquant)
            │   ├── HotelServiceGrpc$HotelServiceFutureStub.class      ← Stub avec Future
            │   └── HotelServiceGrpc$HotelServiceImplBase.class        ← Classe de base serveur
            └── agence/
                └── AgenceServiceGrpc.class (+ stubs similaires)
```

### Les 4 types de stubs générés

Pour chaque service (HotelService, AgenceService), protobuf génère **4 types de stubs** :

#### 1. **BlockingStub** (Synchrone - Celui qu'on utilise)
```java
// Exemple d'utilisation dans AgenceGrpcClient.java
@GrpcClient("hotel-paris")
private HotelServiceGrpc.HotelServiceBlockingStub hotelParisStub;

// Appel synchrone
RechercheResponse response = hotelParisStub.rechercherChambres(request);
```

#### 2. **AsyncStub** (Asynchrone avec callbacks)
```java
// Pas utilisé dans notre projet, mais disponible
HotelServiceGrpc.HotelServiceStub asyncStub = ...;
asyncStub.rechercherChambres(request, new StreamObserver<RechercheResponse>() {
    @Override
    public void onNext(RechercheResponse value) {
        // Traiter la réponse
    }
    @Override
    public void onError(Throwable t) {
        // Gérer l'erreur
    }
    @Override
    public void onCompleted() {
        // Fin du stream
    }
});
```

#### 3. **FutureStub** (Asynchrone avec Future)
```java
// Pas utilisé dans notre projet
HotelServiceGrpc.HotelServiceFutureStub futureStub = ...;
ListenableFuture<RechercheResponse> future = futureStub.rechercherChambres(request);
```

#### 4. **ImplBase** (Classe de base pour le serveur)
```java
// Utilisé dans HotelGrpcService.java
@GrpcService
public class HotelGrpcService extends HotelServiceGrpc.HotelServiceImplBase {
    @Override
    public void rechercherChambres(RechercheRequest request, 
                                   StreamObserver<RechercheResponse> responseObserver) {
        // Implémentation
    }
}
```

---

## 🎯 Question 2 : Pourquoi n'y a-t-il pas d'interface Java classique dans `commun` ?

### Réponse : Les fichiers `.proto` REMPLACENT les interfaces Java !

En gRPC, **les fichiers Protocol Buffers (.proto) sont les interfaces** :

### Architecture traditionnelle Java (REST)

```
commun/
└── src/main/java/
    └── org/tp1/commun/
        ├── HotelService.java          ← Interface Java manuelle
        ├── AgenceService.java         ← Interface Java manuelle
        └── dto/
            ├── ChambreDTO.java        ← POJO manuel
            ├── RechercheRequest.java  ← POJO manuel
            └── ...
```

**Problèmes :**
- ❌ Définition manuelle des interfaces
- ❌ DTOs à écrire à la main
- ❌ Pas de validation à la compilation
- ❌ Synchronisation manuelle client/serveur

### Architecture gRPC (notre projet)

```
commun/
└── src/main/proto/
    ├── hotel.proto                    ← Interface + DTOs DÉCLARATIFS
    └── agence.proto                   ← Interface + DTOs DÉCLARATIFS
```

**Avantages :**
- ✅ **Génération automatique** de tout le code
- ✅ **Contract-first** : le .proto est la source de vérité
- ✅ **Type-safe** : validation à la compilation
- ✅ **Multi-langage** : même .proto pour Java, Python, Go, etc.

---

## 🔍 Comparaison détaillée

### Exemple 1 : Définition d'un service

#### ❌ Méthode traditionnelle (REST - Interface Java)
```java
// Fichier: commun/src/main/java/org/tp1/commun/HotelService.java
public interface HotelService {
    List<ChambreDTO> rechercherChambres(RechercheRequest request);
    ReservationResponse effectuerReservation(ReservationRequest request);
}

// Il faut aussi créer tous les DTOs manuellement
// ChambreDTO.java, RechercheRequest.java, etc.
```

#### ✅ Méthode gRPC (Fichier .proto)
```protobuf
// Fichier: commun/src/main/proto/hotel.proto
service HotelService {
  rpc RechercherChambres(RechercheRequest) returns (RechercheResponse);
  rpc EffectuerReservation(ReservationRequest) returns (ReservationResponse);
}

message RechercheRequest {
  string adresse = 1;
  string dateArrive = 2;
  // ... tous les champs définis ici
}
```

**Résultat :** Protobuf génère automatiquement :
- Interface `HotelServiceGrpc.HotelServiceImplBase` (côté serveur)
- Stubs `HotelServiceGrpc.*Stub` (côté client)
- Classes `RechercheRequest`, `RechercheResponse`, etc.

---

## 📊 Ce qui est généré automatiquement

### À partir de `hotel.proto` :

```protobuf
service HotelService {
  rpc RechercherChambres(RechercheRequest) returns (RechercheResponse);
}

message ChambreMessage {
  int64 id = 1;
  string nom = 2;
  float prix = 3;
}
```

### Génère automatiquement :

**1. Classe de service (HotelServiceGrpc.java) :**
```java
public final class HotelServiceGrpc {
    // Classe de base pour l'implémentation serveur
    public static abstract class HotelServiceImplBase implements BindableService {
        public void rechercherChambres(
            RechercheRequest request,
            StreamObserver<RechercheResponse> responseObserver) {
            // Méthode à implémenter
        }
    }
    
    // Stub synchrone pour le client
    public static final class HotelServiceBlockingStub 
        extends AbstractBlockingStub<HotelServiceBlockingStub> {
        public RechercheResponse rechercherChambres(RechercheRequest request) {
            // Implémentation générée
        }
    }
    
    // + AsyncStub, FutureStub...
}
```

**2. Classe de message (ChambreMessage.java) :**
```java
public final class ChambreMessage extends GeneratedMessageV3 {
    private long id_;
    private String nom_ = "";
    private float prix_;
    
    public long getId() { return id_; }
    public String getNom() { return nom_; }
    public float getPrix() { return prix_; }
    
    public static Builder newBuilder() { return new Builder(); }
    
    public static final class Builder extends GeneratedMessageV3.Builder<Builder> {
        public Builder setId(long value) { /* ... */ }
        public Builder setNom(String value) { /* ... */ }
        public Builder setPrix(float value) { /* ... */ }
        public ChambreMessage build() { /* ... */ }
    }
}
```

---

## 🎨 Comment voir les stubs générés

### Méthode 1 : Après compilation

```bash
cd /home/corentinfay/Bureau/gRPCRepo/commun
mvn clean install

# Les stubs sont dans :
ls target/generated-sources/protobuf/grpc-java/org/tp1/commun/grpc/hotel/
# → HotelServiceGrpc.java

ls target/generated-sources/protobuf/java/org/tp1/commun/grpc/hotel/
# → ChambreMessage.java, RechercheRequest.java, etc.
```

### Méthode 2 : Voir les .class compilés

```bash
cd /home/corentinfay/Bureau/gRPCRepo/commun
jar tf target/commun-0.0.1-SNAPSHOT.jar | grep -i grpc | head -20
```

Vous verrez :
```
org/tp1/commun/grpc/hotel/HotelServiceGrpc.class
org/tp1/commun/grpc/hotel/HotelServiceGrpc$HotelServiceBlockingStub.class
org/tp1/commun/grpc/hotel/HotelServiceGrpc$HotelServiceImplBase.class
...
```

### Méthode 3 : Avec votre IDE

Dans IntelliJ IDEA ou Eclipse :
1. Ouvrez le module `commun`
2. Allez dans `target/generated-sources/protobuf/grpc-java`
3. Naviguez vers `org.tp1.commun.grpc.hotel`
4. Ouvrez `HotelServiceGrpc.java`

---

## 🔗 Comment les stubs sont utilisés dans le projet

### Côté Serveur (Hotellerie)

```java
// HotelGrpcService.java
@GrpcService
public class HotelGrpcService 
    extends HotelServiceGrpc.HotelServiceImplBase {  // ← Utilise le stub généré
    
    @Override
    public void rechercherChambres(...) {
        // Implémentation
    }
}
```

### Côté Client (Agence)

```java
// HotelGrpcClient.java
@Service
public class HotelGrpcClient {
    
    @GrpcClient("hotel-paris")
    private HotelServiceGrpc.HotelServiceBlockingStub hotelParisStub;  // ← Utilise le stub généré
    
    public List<ChambreMessage> rechercherChambres(...) {
        RechercheResponse response = hotelParisStub.rechercherChambres(request);
        return response.getChambresList();
    }
}
```

---

## 💡 Avantages de cette approche

### 1. Contract-First
Le fichier `.proto` est le **contrat** entre client et serveur.
- Changez le .proto → Recompilez → Le code client ET serveur est mis à jour automatiquement

### 2. Type Safety
```java
// Impossible de faire des erreurs de typage
ChambreMessage chambre = ChambreMessage.newBuilder()
    .setId(123)           // Type: long
    .setPrix(99.99f)      // Type: float
    .setNom("Suite")      // Type: String
    .build();

// ❌ Erreur de compilation si mauvais type :
.setId("abc")  // ERREUR : String au lieu de long
```

### 3. Génération Multi-langage
Le même `hotel.proto` peut générer du code pour :
- Java ✓
- Python
- Go
- C++
- JavaScript
- Ruby
- PHP
- etc.

### 4. Rétrocompatibilité
Protocol Buffers gère automatiquement l'évolution du schéma :
```protobuf
message ChambreMessage {
  int64 id = 1;
  string nom = 2;
  float prix = 3;
  int32 nbrDeLit = 4;
  // Ajout d'un nouveau champ (compatible !)
  bool climatisation = 5;  // Les anciens clients l'ignorent simplement
}
```

---

## 📝 Résumé

### Où sont les stubs ?
```
commun/target/generated-sources/protobuf/grpc-java/
├── HotelServiceGrpc.java    ← Stubs + classe de base
└── AgenceServiceGrpc.java   ← Stubs + classe de base
```

### Pourquoi pas d'interface Java ?
Parce que **les fichiers `.proto` SONT les interfaces** :
- Plus puissants (multi-langage)
- Génération automatique du code
- Type-safe à la compilation
- Contract-first design

### Les 4 composants générés

| Composant | Utilisation | Exemple |
|-----------|-------------|---------|
| **ImplBase** | Implémentation serveur | `HotelGrpcService extends HotelServiceImplBase` |
| **BlockingStub** | Client synchrone | `@GrpcClient private ...BlockingStub stub` |
| **AsyncStub** | Client asynchrone | Avec callbacks |
| **FutureStub** | Client avec Future | Pour programmation asynchrone |

---

## 🎯 Pour aller plus loin

### Voir le contenu d'un stub généré

```bash
cd /home/corentinfay/Bureau/gRPCRepo/commun
# Décompiler le JAR pour voir les classes
jar xf target/commun-0.0.1-SNAPSHOT.jar

# Ou regarder directement les sources générées
cat target/generated-sources/protobuf/grpc-java/org/tp1/commun/grpc/hotel/HotelServiceGrpc.java
```

### Documentation officielle

- Protocol Buffers : https://protobuf.dev/
- gRPC Java : https://grpc.io/docs/languages/java/
- grpc-spring-boot-starter : https://yidongnan.github.io/grpc-spring-boot-starter/

---

**En résumé : Les stubs sont générés automatiquement et les fichiers .proto remplacent les interfaces Java traditionnelles !** ✨

