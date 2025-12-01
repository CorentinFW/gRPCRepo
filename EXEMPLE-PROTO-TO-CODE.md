# 🎓 Exemple Concret : Du .proto au code Java

## 📝 Étape 1 : Définition dans hotel.proto

```protobuf
// Fichier: commun/src/main/proto/hotel.proto

syntax = "proto3";
package hotel;

option java_package = "org.tp1.commun.grpc.hotel";
option java_multiple_files = true;

// ═══════════════════════════════════════════════════════════════
// DÉFINITION DU SERVICE (équivalent d'une interface Java)
// ═══════════════════════════════════════════════════════════════
service HotelService {
  rpc RechercherChambres(RechercheRequest) returns (RechercheResponse);
}

// ═══════════════════════════════════════════════════════════════
// DÉFINITION DES MESSAGES (équivalent de DTOs/POJOs)
// ═══════════════════════════════════════════════════════════════
message RechercheRequest {
  string adresse = 1;
  string dateArrive = 2;
  string dateDepart = 3;
  float prixMin = 4;
  float prixMax = 5;
  int32 nbrEtoile = 6;
  int32 nbrLits = 7;
}

message RechercheResponse {
  repeated ChambreMessage chambres = 1;
}

message ChambreMessage {
  int64 id = 1;
  string nom = 2;
  float prix = 3;
  int32 nbrDeLit = 4;
  int32 nbrEtoile = 5;
  bool disponible = 6;
  string imageUrl = 7;
  string hotelNom = 8;
  string hotelAdresse = 9;
}
```

---

## 🔧 Étape 2 : Compilation avec Maven

```bash
cd commun
mvn clean install
```

**Ce qui se passe :**
1. Le plugin `protobuf-maven-plugin` lit `hotel.proto`
2. Il exécute `protoc` (compilateur Protocol Buffers)
3. Il génère automatiquement les classes Java dans `target/generated-sources/`

---

## 🎯 Étape 3 : Code généré automatiquement

### A) Classe de service : HotelServiceGrpc.java

```java
// Fichier généré automatiquement par protobuf
// target/generated-sources/protobuf/grpc-java/org/tp1/commun/grpc/hotel/HotelServiceGrpc.java

package org.tp1.commun.grpc.hotel;

import io.grpc.*;
import io.grpc.stub.*;

public final class HotelServiceGrpc {

    // ═══════════════════════════════════════════════════════════
    // 1️⃣  CLASSE DE BASE POUR LE SERVEUR
    // ═══════════════════════════════════════════════════════════
    public static abstract class HotelServiceImplBase 
            implements io.grpc.BindableService {
        
        /**
         * Méthode à implémenter côté serveur
         */
        public void rechercherChambres(
                RechercheRequest request,
                StreamObserver<RechercheResponse> responseObserver) {
            
            // Par défaut : méthode non implémentée
            responseObserver.onError(new StatusRuntimeException(
                Status.UNIMPLEMENTED.withDescription(
                    "Method hotel.HotelService/RechercherChambres is unimplemented")));
        }
        
        @Override
        public ServerServiceDefinition bindService() {
            return ServerServiceDefinition.builder("hotel.HotelService")
                .addMethod(getRechercherChambresMethod(),
                    asyncUnaryCall(new MethodHandlers<>(this, 0)))
                .build();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // 2️⃣  STUB SYNCHRONE (BLOQUANT) POUR LE CLIENT
    // ═══════════════════════════════════════════════════════════
    public static final class HotelServiceBlockingStub 
            extends AbstractBlockingStub<HotelServiceBlockingStub> {
        
        private HotelServiceBlockingStub(Channel channel, CallOptions callOptions) {
            super(channel, callOptions);
        }
        
        /**
         * Appel synchrone vers le serveur
         */
        public RechercheResponse rechercherChambres(RechercheRequest request) {
            return blockingUnaryCall(
                getChannel(), 
                getRechercherChambresMethod(), 
                getCallOptions(), 
                request);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // 3️⃣  STUB ASYNCHRONE POUR LE CLIENT
    // ═══════════════════════════════════════════════════════════
    public static final class HotelServiceStub 
            extends AbstractAsyncStub<HotelServiceStub> {
        
        private HotelServiceStub(Channel channel, CallOptions callOptions) {
            super(channel, callOptions);
        }
        
        /**
         * Appel asynchrone avec callback
         */
        public void rechercherChambres(
                RechercheRequest request,
                StreamObserver<RechercheResponse> responseObserver) {
            
            asyncUnaryCall(
                getChannel().newCall(getRechercherChambresMethod(), getCallOptions()),
                request,
                responseObserver);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // 4️⃣  STUB AVEC FUTURE POUR LE CLIENT
    // ═══════════════════════════════════════════════════════════
    public static final class HotelServiceFutureStub 
            extends AbstractFutureStub<HotelServiceFutureStub> {
        
        private HotelServiceFutureStub(Channel channel, CallOptions callOptions) {
            super(channel, callOptions);
        }
        
        /**
         * Appel asynchrone retournant un Future
         */
        public ListenableFuture<RechercheResponse> rechercherChambres(
                RechercheRequest request) {
            
            return futureUnaryCall(
                getChannel().newCall(getRechercherChambresMethod(), getCallOptions()),
                request);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // MÉTHODES UTILITAIRES (descripteurs, etc.)
    // ═══════════════════════════════════════════════════════════
    
    private static final MethodDescriptor<RechercheRequest, RechercheResponse> 
        METHOD_RECHERCHER_CHAMBRES = MethodDescriptor.create(
            MethodDescriptor.MethodType.UNARY,
            "hotel.HotelService/RechercherChambres",
            ProtoUtils.marshaller(RechercheRequest.getDefaultInstance()),
            ProtoUtils.marshaller(RechercheResponse.getDefaultInstance()));
    
    public static MethodDescriptor<RechercheRequest, RechercheResponse> 
            getRechercherChambresMethod() {
        return METHOD_RECHERCHER_CHAMBRES;
    }
}
```

### B) Classes de messages générées

```java
// ChambreMessage.java (simplifié)
package org.tp1.commun.grpc.hotel;

public final class ChambreMessage extends GeneratedMessageV3 {
    
    // Champs privés
    private long id_;
    private String nom_ = "";
    private float prix_;
    private int nbrDeLit_;
    private int nbrEtoile_;
    private boolean disponible_;
    private String imageUrl_ = "";
    private String hotelNom_ = "";
    private String hotelAdresse_ = "";
    
    // Getters
    public long getId() { return id_; }
    public String getNom() { return nom_; }
    public float getPrix() { return prix_; }
    public int getNbrDeLit() { return nbrDeLit_; }
    public int getNbrEtoile() { return nbrEtoile_; }
    public boolean getDisponible() { return disponible_; }
    public String getImageUrl() { return imageUrl_; }
    public String getHotelNom() { return hotelNom_; }
    public String getHotelAdresse() { return hotelAdresse_; }
    
    // Builder pattern
    public static Builder newBuilder() {
        return new Builder();
    }
    
    public static final class Builder 
            extends GeneratedMessageV3.Builder<Builder> {
        
        private long id_;
        private String nom_ = "";
        private float prix_;
        // ... autres champs
        
        public Builder setId(long value) {
            id_ = value;
            onChanged();
            return this;
        }
        
        public Builder setNom(String value) {
            if (value == null) throw new NullPointerException();
            nom_ = value;
            onChanged();
            return this;
        }
        
        public Builder setPrix(float value) {
            prix_ = value;
            onChanged();
            return this;
        }
        
        // ... autres setters
        
        public ChambreMessage build() {
            ChambreMessage result = new ChambreMessage(this);
            return result;
        }
    }
}
```

---

## 💻 Étape 4 : Utilisation dans votre code

### A) CÔTÉ SERVEUR (Hotellerie)

```java
// HotelGrpcService.java
package org.tp1.hotellerie.grpc;

import net.devh.boot.grpc.server.service.GrpcService;
import org.tp1.commun.grpc.hotel.*;  // ← Import des stubs générés
import io.grpc.stub.StreamObserver;

@GrpcService  // Annotation Spring Boot gRPC
public class HotelGrpcService 
        extends HotelServiceGrpc.HotelServiceImplBase {  // ← Hérite du stub généré
    
    @Autowired
    private HotelService hotelService;  // Service métier existant
    
    /**
     * Implémentation de la méthode définie dans hotel.proto
     */
    @Override
    public void rechercherChambres(
            RechercheRequest request,              // ← Message généré
            StreamObserver<RechercheResponse> responseObserver) {  // ← Callback gRPC
        
        try {
            // 1. Appeler la logique métier existante
            List<Chambre> chambres = hotelService.rechercherChambres(
                request.getAdresse(),
                request.getDateArrive(),
                request.getDateDepart(),
                request.getPrixMin() > 0 ? request.getPrixMin() : null,
                request.getPrixMax() > 0 ? request.getPrixMax() : null,
                request.getNbrEtoile() > 0 ? request.getNbrEtoile() : null,
                request.getNbrLits() > 0 ? request.getNbrLits() : null
            );
            
            Hotel hotel = hotelService.getHotel();
            
            // 2. Convertir les entités JPA en messages gRPC
            List<ChambreMessage> chambreMessages = chambres.stream()
                .map(chambre -> ChambreMessage.newBuilder()  // ← Builder généré
                    .setId(chambre.getId())
                    .setNom(chambre.getNom())
                    .setPrix(chambre.getPrix())
                    .setNbrDeLit(chambre.getNbrDeLit())
                    .setNbrEtoile(hotel.getType().ordinal() + 1)
                    .setDisponible(true)
                    .setImageUrl(chambre.getImageUrl() != null ? chambre.getImageUrl() : "")
                    .setHotelNom(hotel.getNom())
                    .setHotelAdresse(hotel.getAdresse())
                    .build())  // ← Construit le message immutable
                .collect(Collectors.toList());
            
            // 3. Construire la réponse gRPC
            RechercheResponse response = RechercheResponse.newBuilder()  // ← Builder généré
                .addAllChambres(chambreMessages)
                .build();
            
            // 4. Envoyer la réponse au client
            responseObserver.onNext(response);      // Envoyer les données
            responseObserver.onCompleted();         // Terminer le stream
            
        } catch (Exception e) {
            // 5. En cas d'erreur, envoyer une exception gRPC
            responseObserver.onError(
                io.grpc.Status.INTERNAL
                    .withDescription("Erreur: " + e.getMessage())
                    .asRuntimeException()
            );
        }
    }
}
```

**Ce qui se passe au démarrage du serveur :**
```
1. Spring Boot détecte @GrpcService
2. Crée un serveur gRPC sur le port configuré (9092)
3. Enregistre HotelGrpcService comme implémentation du service
4. Le serveur écoute les connexions gRPC entrantes
```

---

### B) CÔTÉ CLIENT (Agence)

```java
// HotelGrpcClient.java
package org.tp1.agence.grpc.client;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import org.tp1.commun.grpc.hotel.*;  // ← Import des stubs générés

@Service
public class HotelGrpcClient {
    
    /**
     * Injection automatique du stub client
     * Spring Boot gRPC crée automatiquement la connexion
     */
    @GrpcClient("hotel-paris")  // ← Nom configuré dans application.properties
    private HotelServiceGrpc.HotelServiceBlockingStub hotelParisStub;  // ← Stub généré
    
    @GrpcClient("hotel-lyon")
    private HotelServiceGrpc.HotelServiceBlockingStub hotelLyonStub;
    
    @GrpcClient("hotel-montpellier")
    private HotelServiceGrpc.HotelServiceBlockingStub hotelMontpellierStub;
    
    /**
     * Rechercher des chambres dans un hôtel via gRPC
     */
    public List<ChambreMessage> rechercherChambres(String hotelName, RechercheRequest request) {
        try {
            // 1. Choisir le bon stub selon l'hôtel
            HotelServiceGrpc.HotelServiceBlockingStub stub = getStubForHotel(hotelName);
            
            // 2. Appel gRPC SYNCHRONE
            //    La méthode rechercherChambres() a été générée automatiquement
            RechercheResponse response = stub.rechercherChambres(request);
            
            // 3. Retourner la liste des chambres
            return response.getChambresList();  // ← Getter généré
            
        } catch (io.grpc.StatusRuntimeException e) {
            System.err.println("Erreur gRPC: " + e.getStatus());
            return new ArrayList<>();
        }
    }
    
    private HotelServiceGrpc.HotelServiceBlockingStub getStubForHotel(String hotelName) {
        if (hotelName.toLowerCase().contains("paris")) {
            return hotelParisStub;
        } else if (hotelName.toLowerCase().contains("lyon")) {
            return hotelLyonStub;
        } else if (hotelName.toLowerCase().contains("montpellier")) {
            return hotelMontpellierStub;
        }
        return null;
    }
}
```

**Configuration dans application.properties :**
```properties
# application-agence1.properties
grpc.client.hotel-paris.address=static://localhost:9092
grpc.client.hotel-paris.negotiationType=PLAINTEXT

grpc.client.hotel-lyon.address=static://localhost:9093
grpc.client.hotel-lyon.negotiationType=PLAINTEXT
```

**Ce qui se passe :**
```
1. @GrpcClient("hotel-paris") déclenche la création d'un stub
2. Spring Boot lit la config grpc.client.hotel-paris.address
3. Crée automatiquement la connexion gRPC vers localhost:9092
4. Injecte le stub hotelParisStub prêt à l'emploi
5. Chaque appel stub.rechercherChambres() envoie une requête gRPC
```

---

## 🔄 Flux complet d'un appel gRPC

```
┌─────────────┐                                  ┌─────────────┐
│   CLIENT    │                                  │   SERVEUR   │
│  (Agence)   │                                  │  (Hotel)    │
└─────────────┘                                  └─────────────┘
      │                                                  │
      │  1. Créer la requête                            │
      │     RechercheRequest request =                  │
      │       RechercheRequest.newBuilder()             │
      │         .setAdresse("Paris")                    │
      │         .setDateArrive("2025-01-01")            │
      │         .build();                               │
      │                                                  │
      │  2. Appel via le stub                           │
      │     RechercheResponse response =                │
      │       hotelParisStub                            │
      │         .rechercherChambres(request);           │
      │                                                  │
      │──── gRPC Call (HTTP/2 + Protobuf) ────────────>│
      │     Method: hotel.HotelService/RechercherChambres
      │     Body: [binary protobuf]                     │
      │                                                  │
      │                          3. Recevoir la requête │
      │                             @Override           │
      │                             rechercherChambres()│
      │                                                  │
      │                          4. Traiter             │
      │                             - Validation        │
      │                             - Logique métier    │
      │                             - Accès BDD         │
      │                                                  │
      │                          5. Construire réponse  │
      │                             RechercheResponse   │
      │                               .newBuilder()     │
      │                               .addAllChambres() │
      │                               .build()          │
      │                                                  │
      │<──── gRPC Response (HTTP/2 + Protobuf) ────────│
      │     Status: OK                                  │
      │     Body: [binary protobuf]                     │
      │                                                  │
      │  6. Utiliser la réponse                         │
      │     List<ChambreMessage> chambres =             │
      │       response.getChambresList();               │
      │                                                  │
```

---

## 📊 Résumé : Du .proto au code

| Fichier .proto | Classe générée | Utilisation |
|----------------|----------------|-------------|
| `service HotelService` | `HotelServiceGrpc.java` | Contient tous les stubs |
| `rpc RechercherChambres()` | `HotelServiceImplBase.rechercherChambres()` | Méthode à implémenter (serveur) |
| `rpc RechercherChambres()` | `HotelServiceBlockingStub.rechercherChambres()` | Méthode d'appel (client) |
| `message ChambreMessage` | `ChambreMessage.java` | Classe immutable avec Builder |
| `message RechercheRequest` | `RechercheRequest.java` | Classe immutable avec Builder |
| `message RechercheResponse` | `RechercheResponse.java` | Classe immutable avec Builder |

---

## ✅ Avantages de cette approche

1. **Un seul fichier .proto** → Génère tout le code nécessaire
2. **Type-safe** → Erreurs détectées à la compilation
3. **Immutable** → Les messages ne peuvent pas être modifiés après construction
4. **Builder pattern** → Construction fluide et lisible
5. **Multi-langage** → Même .proto pour Java, Python, Go, etc.
6. **Rétrocompatible** → Ajout de champs sans casser les anciens clients

---

**Voilà ! Les stubs sont générés automatiquement et tout le code nécessaire est créé depuis les fichiers .proto** 🎉

