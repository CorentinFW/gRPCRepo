# 🗄️ Base de données H2 : REST vs gRPC

## ✅ RÉPONSE COURTE

**La base de données H2 fonctionne EXACTEMENT de la même façon entre REST et gRPC !**

Il n'y a **AUCUN changement** au niveau de la couche persistance. Seule la **couche de communication** a changé (REST → gRPC).

---

## 📊 Architecture en couches

### Architecture AVANT (REST)

```
┌─────────────────────────────────────────┐
│         CLIENT (Interface Swing)         │
└─────────────────┬───────────────────────┘
                  │ HTTP REST
                  ▼
┌─────────────────────────────────────────┐
│      AGENCE (REST Controller)           │
└─────────────────┬───────────────────────┘
                  │ HTTP REST
                  ▼
┌─────────────────────────────────────────┐
│      HOTELLERIE (REST Controller)       │ ← Couche communication REST
├─────────────────────────────────────────┤
│      HotelService (@Service)            │ ← Couche métier (INCHANGÉE)
├─────────────────────────────────────────┤
│   Repositories JPA (@Repository)        │ ← Couche persistance (INCHANGÉE)
├─────────────────────────────────────────┤
│   Entités JPA (@Entity)                 │ ← Modèle de données (INCHANGÉ)
├─────────────────────────────────────────┤
│   Base H2 (fichier .mv.db)              │ ← Base de données (INCHANGÉE)
└─────────────────────────────────────────┘
```

### Architecture MAINTENANT (gRPC)

```
┌─────────────────────────────────────────┐
│         CLIENT (Interface Swing)         │
└─────────────────┬───────────────────────┘
                  │ gRPC
                  ▼
┌─────────────────────────────────────────┐
│      AGENCE (gRPC Service)              │
└─────────────────┬───────────────────────┘
                  │ gRPC
                  ▼
┌─────────────────────────────────────────┐
│   HOTELLERIE (HotelGrpcService)         │ ← Couche communication gRPC (NOUVELLE)
├─────────────────────────────────────────┤
│   HotelService (@Service)               │ ← Couche métier (INCHANGÉE) ✅
├─────────────────────────────────────────┤
│   Repositories JPA (@Repository)        │ ← Couche persistance (INCHANGÉE) ✅
├─────────────────────────────────────────┤
│   Entités JPA (@Entity)                 │ ← Modèle de données (INCHANGÉ) ✅
├─────────────────────────────────────────┤
│   Base H2 (fichier .mv.db)              │ ← Base de données (INCHANGÉE) ✅
└─────────────────────────────────────────┘
```

**📌 Seule la couche de communication a changé !**

---

## 🔍 Preuve : Le code de persistance est IDENTIQUE

### 1. Les entités JPA (@Entity) - INCHANGÉES

```java
// Chambre.java
@Entity
@Table(name = "chambres")
public class Chambre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nom;
    private Float prix;
    private Integer nbrDeLit;
    private String imageUrl;
    
    @ManyToOne
    @JoinColumn(name = "hotel_id")
    private Hotel hotel;
    
    // Getters/Setters
}

// Reservation.java
@Entity
@Table(name = "reservations")
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    private Chambre chambre;
    
    @ManyToOne
    private Client client;
    
    private String dateArrive;
    private String dateDepart;
    
    // Getters/Setters
}
```

**✅ Aucun changement dans les entités !**

### 2. Les repositories JPA - INCHANGÉS

```java
@Repository
public interface ChambreRepository extends JpaRepository<Chambre, Long> {
    List<Chambre> findByHotelId(Long hotelId);
    long countByHotelId(Long hotelId);
}

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByHotelId(Long hotelId);
    long countByHotelId(Long hotelId);
}
```

**✅ Aucun changement dans les repositories !**

### 3. Le service métier (HotelService) - INCHANGÉ

```java
@Service
@Transactional
public class HotelService {
    
    @Autowired
    private HotelRepository hotelRepository;
    
    @Autowired
    private ChambreRepository chambreRepository;
    
    @Autowired
    private ReservationRepository reservationRepository;
    
    @Autowired
    private ClientRepository clientRepository;
    
    // Méthodes métier INCHANGÉES
    public List<Chambre> rechercherChambres(...) {
        // Utilise chambreRepository.findByHotelId()
        // MÊME CODE qu'avant !
    }
    
    public ReservationResult effectuerReservation(...) {
        // Utilise reservationRepository.save()
        // MÊME CODE qu'avant !
    }
    
    public List<ChambreDTO> getChambresReservees() {
        // Utilise reservationRepository.findByHotelId()
        // MÊME CODE qu'avant !
    }
}
```

**✅ HotelService utilise TOUJOURS les mêmes repositories JPA !**

### 4. La configuration de la base H2 - INCHANGÉE

```properties
# application-paris.properties

# Base de données H2 (IDENTIQUE)
spring.datasource.url=jdbc:h2:file:./data/hotellerie-paris-db
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA/Hibernate (IDENTIQUE)
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

# Console H2 (IDENTIQUE)
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

**✅ Configuration H2 identique !**

---

## 🔄 Comment ça fonctionne maintenant ?

### Exemple : Effectuer une réservation

#### AVANT (REST)
```
CLIENT REST
    │
    │ POST /reservations (JSON)
    ▼
HotelController (@RestController)
    │
    │ hotelService.effectuerReservation(...)
    ▼
HotelService (@Service)
    │
    │ reservationRepository.save(...)
    ▼
Base H2
```

#### MAINTENANT (gRPC)
```
CLIENT gRPC
    │
    │ RPC effectuerReservation (Protobuf)
    ▼
HotelGrpcService (@GrpcService)
    │
    │ hotelService.effectuerReservation(...)  ← MÊME APPEL !
    ▼
HotelService (@Service)
    │
    │ reservationRepository.save(...)  ← MÊME CODE !
    ▼
Base H2  ← MÊME BASE !
```

**📌 Seule la couche de réception de la requête a changé (REST → gRPC)**

---

## 🎯 Ce qui a changé vs ce qui est resté identique

### ❌ A CHANGÉ : Couche de communication

| Aspect | AVANT (REST) | MAINTENANT (gRPC) |
|--------|--------------|-------------------|
| **Contrôleur** | `HotelController` avec `@RestController` | `HotelGrpcService` avec `@GrpcService` |
| **Endpoints** | HTTP REST (`/chambres`, `/reservations`) | RPC gRPC (`rechercherChambres`, `effectuerReservation`) |
| **Format de données** | JSON | Protocol Buffers (binaire) |
| **Transport** | HTTP/1.1 | HTTP/2 |
| **Annotations** | `@GetMapping`, `@PostMapping` | `@Override` des méthodes gRPC |

### ✅ INCHANGÉ : Tout le reste !

| Aspect | Statut |
|--------|--------|
| **Entités JPA** (`@Entity`) | ✅ IDENTIQUES |
| **Repositories** (`@Repository`) | ✅ IDENTIQUES |
| **Service métier** (`HotelService`) | ✅ IDENTIQUE |
| **Base de données H2** | ✅ IDENTIQUE |
| **Fichiers de données** (`hotellerie-paris-db.mv.db`) | ✅ IDENTIQUES |
| **Configuration H2** | ✅ IDENTIQUE |
| **Schéma de base** (tables, colonnes) | ✅ IDENTIQUE |
| **Logique métier** (recherche, réservation) | ✅ IDENTIQUE |

---

## 📂 Structure des fichiers de base H2

```
gRPCRepo/
└── Hotellerie/
    └── data/
        ├── hotellerie-paris-db.mv.db       ← Base H2 Paris (INCHANGÉE)
        ├── hotellerie-lyon-db.mv.db        ← Base H2 Lyon (INCHANGÉE)
        └── hotellerie-montpellier-db.mv.db ← Base H2 Montpellier (INCHANGÉE)
```

**Ces fichiers :**
- ✅ Sont les MÊMES qu'avant
- ✅ Contiennent les MÊMES données
- ✅ Utilisent le MÊME schéma
- ✅ Fonctionnent de la MÊME façon

---

## 🔬 Exemple concret : Méthode getChambresReservees()

### Dans HotelService (INCHANGÉ)

```java
@Service
@Transactional
public class HotelService {
    
    public List<ChambreDTO> getChambresReservees() {
        List<Reservation> reservations = reservationRepository.findByHotelId(hotelId);
        
        return reservations.stream()
            .map(reservation -> {
                Chambre chambre = reservation.getChambre();
                return new ChambreDTO(
                    chambre.getId(),
                    chambre.getNom(),
                    chambre.getPrix(),
                    chambre.getNbrDeLit(),
                    // ...
                );
            })
            .collect(Collectors.toList());
    }
}
```

**Cette méthode :**
- ✅ Existe depuis le début (version REST)
- ✅ Utilise `reservationRepository` (JPA)
- ✅ Interroge la base H2
- ✅ N'a PAS changé !

### Dans HotelGrpcService (NOUVEAU - appelle la méthode existante)

```java
@GrpcService
public class HotelGrpcService extends HotelServiceGrpc.HotelServiceImplBase {
    
    @Autowired
    private HotelService hotelService;  // ← Le MÊME service métier !
    
    @Override
    public void getChambresReservees(ChambresReserveesRequest request,
                                     StreamObserver<ChambresReserveesResponse> responseObserver) {
        
        // 1. Appeler le service métier EXISTANT
        List<ChambreDTO> chambresReservees = hotelService.getChambresReservees();
        //                                    ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑
        //                         MÊME MÉTHODE qu'avant (accède à la BDD H2)
        
        // 2. Convertir DTO → Proto (NOUVEAU)
        List<ChambreMessage> chambreMessages = chambresReservees.stream()
            .map(chambre -> ChambreMessage.newBuilder()
                .setId(chambre.getId())
                .setNom(chambre.getNom())
                .setPrix(chambre.getPrix())
                .build())
            .collect(Collectors.toList());
        
        // 3. Retourner la réponse gRPC (NOUVEAU format)
        ChambresReserveesResponse response = ChambresReserveesResponse.newBuilder()
            .addAllChambres(chambreMessages)
            .build();
        
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
```

**Ce qui est nouveau :**
- ❌ Le format de réponse (Protocol Buffers au lieu de JSON)
- ❌ L'interface gRPC (StreamObserver au lieu de ResponseEntity)

**Ce qui est IDENTIQUE :**
- ✅ L'appel à `hotelService.getChambresReservees()`
- ✅ L'accès à la base H2
- ✅ La logique métier
- ✅ Les données retournées

---

## 💡 Analogie simple

Imaginez une bibliothèque :

**AVANT (REST) :**
```
Vous demandez un livre par téléphone (REST/JSON)
    ↓
Le bibliothécaire cherche dans le catalogue (HotelService)
    ↓
Il va dans les étagères (Base H2)
    ↓
Il vous rappelle avec les infos (JSON)
```

**MAINTENANT (gRPC) :**
```
Vous demandez un livre par email (gRPC/Protobuf)
    ↓
Le bibliothécaire cherche dans le catalogue (HotelService) ← MÊME PERSONNE !
    ↓
Il va dans les étagères (Base H2) ← MÊMES ÉTAGÈRES !
    ↓
Il vous répond par email avec les infos (Protobuf)
```

**📌 Le bibliothécaire (HotelService) et les étagères (Base H2) n'ont PAS changé !**

Seul le **moyen de communication** a changé (téléphone → email).

---

## 🔑 Points clés à retenir

1. ✅ **La base H2 est IDENTIQUE** (mêmes fichiers `.mv.db`, même schéma)
2. ✅ **Les entités JPA sont IDENTIQUES** (même `@Entity`, mêmes champs)
3. ✅ **Les repositories sont IDENTIQUES** (mêmes requêtes JPA)
4. ✅ **Le service métier est IDENTIQUE** (même logique)
5. ❌ **Seule la couche de communication a changé** (REST → gRPC)

---

## 📊 Schéma de base de données (INCHANGÉ)

```sql
-- Table: hotels
CREATE TABLE hotels (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nom VARCHAR(255),
    adresse VARCHAR(255),
    type VARCHAR(50),
    ville VARCHAR(100),
    telephone VARCHAR(50)
);

-- Table: chambres
CREATE TABLE chambres (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nom VARCHAR(255),
    prix FLOAT,
    nbr_de_lit INTEGER,
    image_url VARCHAR(500),
    hotel_id BIGINT,
    FOREIGN KEY (hotel_id) REFERENCES hotels(id)
);

-- Table: clients
CREATE TABLE clients (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nom VARCHAR(255),
    prenom VARCHAR(255),
    numero_carte_bancaire VARCHAR(50)
);

-- Table: reservations
CREATE TABLE reservations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    chambre_id BIGINT,
    client_id BIGINT,
    hotel_id BIGINT,
    date_arrive VARCHAR(50),
    date_depart VARCHAR(50),
    FOREIGN KEY (chambre_id) REFERENCES chambres(id),
    FOREIGN KEY (client_id) REFERENCES clients(id),
    FOREIGN KEY (hotel_id) REFERENCES hotels(id)
);
```

**Ce schéma est LE MÊME qu'avant la migration vers gRPC !**

---

## ✅ CONCLUSION

### La base de données H2 fonctionne EXACTEMENT de la même façon !

**Aucun changement au niveau :**
- ❌ Des fichiers de base (`.mv.db`)
- ❌ Du schéma (tables, colonnes)
- ❌ Des entités JPA
- ❌ Des repositories
- ❌ Du service métier
- ❌ De la configuration H2

**Seul changement :**
- ✅ La couche de communication (REST → gRPC)

**En résumé :**
```
HotelGrpcService (NOUVEAU) → HotelService (INCHANGÉ) → Repositories (INCHANGÉS) → Base H2 (INCHANGÉE)
```

**La migration REST → gRPC n'a eu AUCUN impact sur la couche persistance !** 🎉

