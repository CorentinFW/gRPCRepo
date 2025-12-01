# 🏨 Hotellerie - Service SOAP

## 📋 Description

Service SOAP qui représente un hôtel dans le système de réservation. Chaque instance gère ses propres chambres, disponibilités et réservations.

Le service expose un endpoint SOAP permettant de :
- Rechercher des chambres selon des critères (ville, dates, prix, nombre de lits)
- Effectuer des réservations avec validation des dates
- Obtenir la liste des réservations

---

## 🏗️ Architecture

### Structure du code

```
Hotellerie/
├── src/main/java/org/tp1/hotellerie/
│   ├── HotellerieApplication.java       # Point d'entrée Spring Boot
│   │
│   ├── soap/
│   │   ├── HotelEndpoint.java           # Endpoint SOAP (reçoit les requêtes)
│   │   ├── HotelService.java            # Logique métier de l'hôtel
│   │   ├── WebServiceConfig.java        # Configuration Spring-WS
│   │   └── generated/                   # Classes générées depuis XSD (JAXB)
│   │
│   └── model/
│       ├── Hotel.java                   # Modèle de l'hôtel
│       ├── Chambre.java                 # Modèle de la chambre
│       ├── Reservation.java             # Modèle de la réservation
│       └── Client.java                  # Modèle du client
│
└── src/main/resources/
    ├── application.properties           # Configuration par défaut
    ├── application-paris.properties     # Configuration Paris (port 8082)
    ├── application-lyon.properties      # Configuration Lyon (port 8083)
    ├── application-montpellier.properties # Configuration Montpellier (port 8084)
    │
    ├── wsdl/
    │   └── hotel.wsdl                   # Contrat WSDL du service
    │
    └── xsd/
        └── hotel.xsd                    # Schéma des messages SOAP
```

---

## 🔧 Fonctionnement

### 1. Démarrage et Initialisation

Lorsque le service démarre :
1. **Spring Boot** charge la configuration du profil (paris, lyon ou montpellier)
2. Le **HotelService** initialise les données en mémoire :
   - Informations de l'hôtel (nom, adresse, étoiles)
   - Liste de chambres disponibles (numéro, prix, lits, image)
   - Liste vide de réservations
3. **Spring-WS** publie l'endpoint SOAP sur `/ws`
4. Le **WSDL** est accessible sur `/ws?wsdl`

### 2. Endpoint SOAP

Le `HotelEndpoint` utilise les annotations Spring-WS :

```java
@Endpoint                                    // Marque la classe comme endpoint SOAP
@PayloadRoot(namespace = "...", localPart = "rechercherChambresRequest")
@ResponsePayload                             // Retourne une réponse SOAP
public RechercherChambresResponse rechercherChambres(@RequestPayload RechercherChambresRequest request)
```

**Processus de traitement** :
1. Spring-WS reçoit le message SOAP XML
2. JAXB **unmarshall** (désérialise) le XML en objets Java
3. L'endpoint extrait les paramètres de la requête
4. Le `HotelService` traite la logique métier
5. L'endpoint construit la réponse avec les objets générés JAXB
6. JAXB **marshall** (sérialise) la réponse en XML SOAP
7. Spring-WS renvoie la réponse au client

### 3. Service Métier (HotelService)

#### Recherche de chambres (`rechercherChambres`)

**Critères de filtrage** :
- **Adresse** : doit correspondre à la ville de l'hôtel
- **Dates** : la chambre ne doit pas être réservée sur la période
- **Prix** : entre prix min et prix max (si spécifiés)
- **Nombre de lits** : exact ou supérieur (si spécifié)

**Algorithme de vérification des dates** :
```
Pour chaque chambre :
  Pour chaque réservation de cette chambre :
    Si la période demandée chevauche la réservation existante :
      → Chambre non disponible
  Si aucun chevauchement :
    → Chambre disponible
```

Un chevauchement existe si :
```
dateArrivéeDemandée < dateDépartRéservée 
  ET 
dateDépartDemandée > dateArrivéeRéservée
```

#### Réservation (`effectuerReservation`)

**Processus** :
1. Vérification que la chambre existe
2. Vérification de la disponibilité sur les dates (même algorithme que recherche)
3. Si disponible :
   - Génération d'un ID unique (incrémental)
   - Création de l'objet Reservation
   - Ajout dans la liste des réservations
   - Retour de l'ID de réservation
4. Si non disponible :
   - Retour d'un ID = 0 (convention d'échec)

**Important** : L'ID = 0 signale au client que la réservation a échoué (dates déjà prises).

#### Liste des réservations (`obtenirReservationsParHotel`)

Retourne toutes les réservations de l'hôtel avec :
- Informations client (nom, prénom)
- Numéro de chambre
- Dates de séjour
- ID de réservation

---

## 🚀 Démarrage

### Profils disponibles

| Profil | Ville | Port | Commande |
|--------|-------|------|----------|
| paris | Paris | 8082 | `mvn spring-boot:run -Dspring-boot.run.profiles=paris` |
| lyon | Lyon | 8083 | `mvn spring-boot:run -Dspring-boot.run.profiles=lyon` |
| montpellier | Montpellier | 8084 | `mvn spring-boot:run -Dspring-boot.run.profiles=montpellier` |

### Démarrage simple (Paris)

```bash
cd Hotellerie
mvn spring-boot:run -Dspring-boot.run.profiles=paris
```

### Vérification

Une fois démarré, testez l'endpoint :
```bash
curl http://localhost:8082/ws?wsdl
```

Vous devriez voir le contrat WSDL XML.

---

## 📡 API SOAP

### Operations disponibles

#### 1. rechercherChambres

**Requête** :
```xml
<rechercherChambresRequest>
  <adresse>Paris</adresse>
  <dateArrive>2025-12-01</dateArrive>
  <dateDepart>2025-12-05</dateDepart>
  <prixMin>50</prixMin>          <!-- Optionnel -->
  <prixMax>150</prixMax>         <!-- Optionnel -->
  <nbrEtoile>4</nbrEtoile>       <!-- Optionnel -->
  <nbrLits>2</nbrLits>           <!-- Optionnel -->
</rechercherChambresRequest>
```

**Réponse** :
```xml
<rechercherChambresResponse>
  <hotelNom>Hôtel Paris Centre</hotelNom>
  <hotelAdresse>Paris</hotelAdresse>
  <chambres>
    <chambre>
      <id>101</id>
      <nom>Chambre 101</nom>
      <prix>120.0</prix>
      <nbrDeLit>2</nbrDeLit>
      <imageUrl>http://localhost:8080/images/hotelle1.png</imageUrl>
    </chambre>
    <!-- ... autres chambres ... -->
  </chambres>
</rechercherChambresResponse>
```

#### 2. effectuerReservation

**Requête** :
```xml
<effectuerReservationRequest>
  <client>
    <nom>Dupont</nom>
    <prenom>Jean</prenom>
    <numeroCarteBleue>1234567890123456</numeroCarteBleue>
  </client>
  <chambreId>101</chambreId>
  <dateArrive>2025-12-01</dateArrive>
  <dateDepart>2025-12-05</dateDepart>
</effectuerReservationRequest>
```

**Réponse** :
```xml
<effectuerReservationResponse>
  <reservationId>1</reservationId>  <!-- 0 si échec (dates prises) -->
</effectuerReservationResponse>
```

#### 3. obtenirReservationsParHotel

**Requête** :
```xml
<obtenirReservationsParHotelRequest>
  <hotelNom>Hôtel Paris Centre</hotelNom>
</obtenirReservationsParHotelRequest>
```

**Réponse** :
```xml
<obtenirReservationsParHotelResponse>
  <reservations>
    <reservation>
      <id>1</id>
      <clientNom>Dupont</clientNom>
      <clientPrenom>Jean</clientPrenom>
      <chambreId>101</chambreId>
      <dateArrive>2025-12-01</dateArrive>
      <dateDepart>2025-12-05</dateDepart>
    </reservation>
    <!-- ... autres réservations ... -->
  </reservations>
</obtenirReservationsParHotelResponse>
```

---

## 🗄️ Modèle de Données

### Hotel
- `nom` : Nom de l'hôtel
- `adresse` : Ville de l'hôtel
- `etoile` : Nombre d'étoiles (1-5)
- `chambres` : Liste des chambres
- `reservations` : Liste des réservations

### Chambre
- `id` : Numéro de chambre (unique)
- `nom` : Nom affiché de la chambre
- `prix` : Prix par nuit (double)
- `nbrDeLit` : Nombre de lits
- `imageUrl` : URL de l'image de la chambre

### Reservation
- `id` : ID unique de réservation (>0 si succès, =0 si échec)
- `client` : Objet Client
- `chambre` : Objet Chambre réservée
- `dateArrive` : Date d'arrivée (String format YYYY-MM-DD)
- `dateDepart` : Date de départ (String format YYYY-MM-DD)

### Client
- `nom` : Nom du client
- `prenom` : Prénom du client
- `numeroCarteBleue` : Numéro de carte bancaire

---

## ⚙️ Configuration

### application-paris.properties
```properties
server.port=8082
hotel.nom=Hôtel Paris Centre
hotel.adresse=Paris
hotel.etoile=4
```

### application-lyon.properties
```properties
server.port=8083
hotel.nom=Hôtel Lyon Confluence
hotel.adresse=Lyon
hotel.etoile=3
```

### application-montpellier.properties
```properties
server.port=8084
hotel.nom=Hôtel Montpellier Sud
hotel.adresse=Montpellier
hotel.etoile=4
```

---

## 🧪 Tests

### Test avec SoapUI

1. Importer le WSDL : `http://localhost:8082/ws?wsdl`
2. Créer une requête `rechercherChambres`
3. Envoyer la requête
4. Vérifier la réponse

### Test avec curl (requête SOAP manuelle)

```bash
curl -X POST http://localhost:8082/ws \
  -H "Content-Type: text/xml" \
  -d '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:hot="http://tp1.org/hotellerie/soap">
   <soapenv:Header/>
   <soapenv:Body>
      <hot:rechercherChambresRequest>
         <hot:adresse>Paris</hot:adresse>
         <hot:dateArrive>2025-12-01</hot:dateArrive>
         <hot:dateDepart>2025-12-05</hot:dateDepart>
      </hot:rechercherChambresRequest>
   </soapenv:Body>
</soapenv:Envelope>'
```

---

## 📝 Points Techniques Importants

### Génération des classes JAXB

Les classes dans `soap/generated/` sont générées automatiquement par Maven à partir du fichier `hotel.xsd` grâce au plugin `jaxb2-maven-plugin`.

Pour régénérer :
```bash
mvn clean compile
```

### Gestion des dates

Les dates sont gérées en String (format ISO : `YYYY-MM-DD`) pour simplifier les échanges SOAP. La comparaison se fait avec `compareTo()`.

### Données en mémoire

Les données (chambres, réservations) sont stockées en mémoire. Elles sont **réinitialisées à chaque redémarrage** du service.

### Multi-instances

Le même code source gère 3 hôtels différents grâce aux **profils Spring**. Chaque profil définit :
- Un port différent
- Un nom d'hôtel différent
- Une ville différente
- Des chambres différentes (initialisées dans `HotelService`)

---

## 🐛 Résolution de Problèmes

### Port déjà utilisé
```
Error: Port 8082 already in use
```
**Solution** : Un service est déjà en cours. Arrêtez-le :
```bash
pkill -f "spring-boot:run.*paris"
```

### Classes JAXB non trouvées
```
Error: Cannot find symbol in generated package
```
**Solution** : Régénérez les classes :
```bash
mvn clean compile
```

### Pas de chambres disponibles
**Cause** : Les dates demandées chevauchent des réservations existantes.
**Solution** : Essayez d'autres dates ou relancez le service (réinitialise les données).

---

## 📚 Documentation Complémentaire

- [WSDL Hotel](src/main/resources/wsdl/hotel.wsdl)
- [XSD Hotel](src/main/resources/xsd/hotel.xsd)
- [Documentation Spring-WS](https://docs.spring.io/spring-ws/docs/current/reference/)

---

Retour au [README principal](../README.md)

