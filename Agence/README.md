# 🏢 Agence - Service SOAP Intermédiaire

## 📋 Description

Service SOAP qui joue le rôle d'intermédiaire entre le client et les hôtels. L'agence interroge plusieurs hôtels, agrège leurs résultats et expose une API SOAP unifiée au client.

**Double rôle** :
- **Serveur SOAP** : expose des opérations pour le client
- **Client SOAP** : consomme les services des hôtels

---

## 🏗️ Architecture

### Structure du code

```
Agence/
├── src/main/java/org/tp1/agence/
│   ├── AgenceApplication.java           # Point d'entrée Spring Boot
│   │
│   ├── endpoint/
│   │   └── AgenceEndpoint.java          # Endpoint SOAP (serveur pour le client)
│   │
│   ├── client/
│   │   └── HotelSoapClient.java         # Client SOAP (vers les hôtels)
│   │
│   ├── service/
│   │   └── AgenceService.java           # Logique d'agrégation
│   │
│   ├── dto/
│   │   ├── ChambreDTO.java              # Objet de transfert pour les chambres
│   │   ├── RechercheRequest.java        # Requête de recherche
│   │   ├── ReservationRequest.java      # Requête de réservation
│   │   └── ReservationResponse.java     # Réponse de réservation
│   │
│   ├── config/
│   │   └── SoapClientConfig.java        # Configuration des clients SOAP
│   │
│   └── soap/                            # Classes générées JAXB (agence)
│
└── src/main/resources/
    ├── application.properties           # Configuration (port 8081)
    │
    └── wsdl/
        ├── agence.wsdl                  # Contrat WSDL de l'agence
        ├── hotel-paris.wsdl             # WSDL de l'hôtel Paris
        ├── hotel-lyon.wsdl              # WSDL de l'hôtel Lyon
        └── hotel-montpellier.wsdl       # WSDL de l'hôtel Montpellier
```

---

## 🔧 Fonctionnement

### 1. Architecture à Deux Niveaux

```
CLIENT
  │
  │ SOAP Request
  ↓
┌─────────────────────────┐
│  AGENCE (Port 8081)     │
│  ┌────────────────────┐ │
│  │ AgenceEndpoint     │ │ ← Serveur SOAP pour le client
│  │  (Serveur)         │ │
│  └──────────┬─────────┘ │
│             ↓            │
│  ┌────────────────────┐ │
│  │ AgenceService      │ │ ← Logique d'agrégation
│  └──────────┬─────────┘ │
│             ↓            │
│  ┌────────────────────┐ │
│  │ HotelSoapClient    │ │ ← Client SOAP vers les hôtels
│  │  (Client)          │ │
│  └──────────┬─────────┘ │
└─────────────┼───────────┘
              │ SOAP Requests (parallèles)
       ┌──────┼──────┬──────┐
       ↓      ↓      ↓      
    PARIS  LYON  MONTPELLIER
```

### 2. Endpoint SOAP (Serveur)

Le `AgenceEndpoint` expose 4 opérations SOAP :

#### a) `ping` - Test de connexion
```java
@PayloadRoot(namespace = "...", localPart = "pingRequest")
public PingResponse ping(@RequestPayload PingRequest request)
```
Permet au client de vérifier que l'agence est opérationnelle.

#### b) `rechercherChambres` - Recherche multi-hôtels
```java
@PayloadRoot(namespace = "...", localPart = "rechercherChambresRequest")
public RechercherChambresResponse rechercherChambres(@RequestPayload RechercherChambresRequest request)
```

**Processus** :
1. Réception de la requête SOAP du client
2. Conversion en `RechercheRequest` DTO
3. Appel à `AgenceService.rechercherChambres()`
4. L'agence interroge **tous les hôtels** en parallèle
5. Agrégation des résultats
6. Conversion en réponse SOAP
7. Envoi au client

#### c) `effectuerReservation` - Réservation dirigée
```java
@PayloadRoot(namespace = "...", localPart = "effectuerReservationRequest")
public EffectuerReservationResponse effectuerReservation(@RequestPayload EffectuerReservationRequest request)
```

**Processus** :
1. Extraction des données du client et de la chambre
2. Identification de l'hôtel (via adresse de la chambre)
3. Appel au `HotelSoapClient` pour l'hôtel spécifique
4. Retour de l'ID de réservation (>0 si succès, =0 si échec)

#### d) `obtenirReservationsParHotel` - Liste des réservations
```java
@PayloadRoot(namespace = "...", localPart = "obtenirReservationsParHotelRequest")
public ObtenirReservationsParHotelResponse obtenirReservationsParHotel(@RequestPayload ObtenirReservationsParHotelRequest request)
```

Interroge un hôtel spécifique pour obtenir toutes ses réservations.

### 3. Service d'Agrégation (AgenceService)

#### Recherche de chambres (`rechercherChambres`)

**Algorithme** :
```
1. Créer une liste vide de résultats
2. Pour chaque hôtel (Paris, Lyon, Montpellier) :
   a. Appeler hotelSoapClient.rechercherChambres(critères)
   b. Convertir les chambres SOAP en ChambreDTO
   c. Ajouter les informations de l'hôtel (nom, adresse)
   d. Ajouter à la liste de résultats
3. Retourner la liste complète
```

**Gestion des erreurs** :
- Si un hôtel ne répond pas, l'agence continue avec les autres
- Les erreurs sont loggées mais ne bloquent pas la recherche

#### Réservation (`effectuerReservation`)

**Algorithme** :
```
1. Extraire l'adresse de l'hôtel depuis la requête
2. Identifier le client SOAP correspondant (Paris, Lyon ou Montpellier)
3. Si hôtel trouvé :
   a. Créer la requête SOAP vers l'hôtel
   b. Appeler hotelSoapClient.effectuerReservation()
   c. Retourner l'ID de réservation
4. Si hôtel non trouvé :
   a. Retourner ID = 0 (échec)
```

#### Liste des réservations par hôtel

Délègue directement la requête au `HotelSoapClient` de l'hôtel concerné.

### 4. Client SOAP (HotelSoapClient)

Le `HotelSoapClient` utilise **Spring WebServiceTemplate** pour communiquer avec les hôtels :

```java
@Service
public class HotelSoapClient {
    
    @Autowired
    @Qualifier("parisWebServiceTemplate")
    private WebServiceTemplate parisTemplate;
    
    @Autowired
    @Qualifier("lyonWebServiceTemplate")
    private WebServiceTemplate lyonTemplate;
    
    @Autowired
    @Qualifier("montpellierWebServiceTemplate")
    private WebServiceTemplate montpellierTemplate;
    
    public List<Chambre> rechercherChambres(String ville, ...) {
        // Construit la requête SOAP
        RechercherChambresRequest request = new RechercherChambresRequest();
        request.setAdresse(ville);
        // ... autres paramètres
        
        // Envoie la requête au bon hôtel
        WebServiceTemplate template = getTemplateForCity(ville);
        RechercherChambresResponse response = 
            (RechercherChambresResponse) template.marshalSendAndReceive(request);
        
        return response.getChambres();
    }
}
```

**Caractéristiques** :
- **3 WebServiceTemplate** : un par hôtel (Paris, Lyon, Montpellier)
- Chaque template est configuré avec l'URL du service hôtel
- Les classes de requête/réponse sont générées depuis les WSDL des hôtels
- Marshall automatique (Java → XML) et unmarshall (XML → Java)

---

## 🚀 Démarrage

### Prérequis

**Les hôtels doivent être démarrés AVANT l'agence** :
```bash
# Terminal 1
cd Hotellerie
mvn spring-boot:run -Dspring-boot.run.profiles=paris

# Terminal 2
cd Hotellerie
mvn spring-boot:run -Dspring-boot.run.profiles=lyon

# Terminal 3
cd Hotellerie
mvn spring-boot:run -Dspring-boot.run.profiles=montpellier
```

Attendre 30-60 secondes que tous les hôtels soient démarrés.

### Démarrage de l'agence

```bash
cd Agence
mvn spring-boot:run
```

### Vérification

```bash
# Vérifier le WSDL de l'agence
curl http://localhost:8081/ws?wsdl

# Vérifier les logs
# L'agence doit afficher :
# - "Agence SOAP démarrée sur le port 8081"
# - Pas d'erreurs de connexion aux hôtels
```

---

## 📡 API SOAP

### Operations disponibles

#### 1. ping

**Requête** :
```xml
<pingRequest xmlns="http://tp1.org/agence/soap"/>
```

**Réponse** :
```xml
<pingResponse>
  <message>Agence SOAP opérationnelle</message>
</pingResponse>
```

#### 2. rechercherChambres

**Requête** :
```xml
<rechercherChambresRequest xmlns="http://tp1.org/agence/soap">
  <adresse>Paris</adresse>
  <dateArrive>2025-12-01</dateArrive>
  <dateDepart>2025-12-05</dateDepart>
  <prixMin>50</prixMin>
  <prixMax>200</prixMax>
  <nbrLits>2</nbrLits>
</rechercherChambresRequest>
```

**Réponse** :
```xml
<rechercherChambresResponse>
  <chambres>
    <chambre>
      <id>101</id>
      <numero>Chambre 101</numero>
      <prix>120.0</prix>
      <nbrLits>2</nbrLits>
      <hotelNom>Hôtel Paris Centre</hotelNom>
      <hotelAdresse>Paris</hotelAdresse>
      <disponible>true</disponible>
      <imageUrl>http://localhost:8080/images/hotelle1.png</imageUrl>
    </chambre>
    <!-- Chambres de tous les hôtels répondant aux critères -->
  </chambres>
</rechercherChambresResponse>
```

#### 3. effectuerReservation

**Requête** :
```xml
<effectuerReservationRequest xmlns="http://tp1.org/agence/soap">
  <clientNom>Dupont</clientNom>
  <clientPrenom>Jean</clientPrenom>
  <numeroCarteBleue>1234567890123456</numeroCarteBleue>
  <chambreId>101</chambreId>
  <dateArrive>2025-12-01</dateArrive>
  <dateDepart>2025-12-05</dateDepart>
  <hotelAdresse>Paris</hotelAdresse>
</effectuerReservationRequest>
```

**Réponse** :
```xml
<effectuerReservationResponse>
  <reservationId>1</reservationId>  <!-- 0 si échec -->
  <message>Réservation effectuée avec succès</message>
</effectuerReservationResponse>
```

#### 4. obtenirReservationsParHotel

**Requête** :
```xml
<obtenirReservationsParHotelRequest xmlns="http://tp1.org/agence/soap">
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

## 🗄️ Objets DTO (Data Transfer Objects)

### ChambreDTO
Représentation interne unifiée d'une chambre (agrège infos chambre + hôtel) :
```java
public class ChambreDTO {
    private int id;              // Numéro de chambre
    private String nom;          // Nom de la chambre
    private double prix;         // Prix par nuit
    private int nbrDeLit;        // Nombre de lits
    private String hotelNom;     // Nom de l'hôtel
    private String hotelAdresse; // Ville de l'hôtel
    private String imageUrl;     // URL de l'image
}
```

### RechercheRequest
Critères de recherche :
```java
public class RechercheRequest {
    private String adresse;      // Ville (obligatoire)
    private String dateArrive;   // Date d'arrivée (obligatoire)
    private String dateDepart;   // Date de départ (obligatoire)
    private Double prixMin;      // Prix min (optionnel)
    private Double prixMax;      // Prix max (optionnel)
    private Integer nbrEtoile;   // Étoiles (optionnel)
    private Integer nbrLits;     // Nombre de lits (optionnel)
}
```

### ReservationRequest / ReservationResponse
Utilisés pour transporter les informations de réservation entre les couches.

---

## ⚙️ Configuration

### application.properties
```properties
# Port du serveur
server.port=8081

# Configuration Spring-WS
spring.ws.path=/ws
spring.ws.servlet.init.wsdl-location=/wsdl/agence.wsdl

# URLs des hôtels
hotel.paris.url=http://localhost:8082/ws
hotel.lyon.url=http://localhost:8083/ws
hotel.montpellier.url=http://localhost:8084/ws

# Timeouts (millisecondes)
soap.connection.timeout=5000
soap.read.timeout=10000
```

### SoapClientConfig.java
Configuration des `WebServiceTemplate` pour chaque hôtel :
```java
@Bean
public WebServiceTemplate parisWebServiceTemplate() {
    WebServiceTemplate template = new WebServiceTemplate();
    template.setDefaultUri("http://localhost:8082/ws");
    template.setMarshaller(marshaller());
    template.setUnmarshaller(marshaller());
    return template;
}
// ... idem pour Lyon et Montpellier
```

---

## 🔄 Flux de Données Complet

### Exemple : Recherche de chambres

```
1. CLIENT envoie :
   <rechercherChambresRequest>
     <adresse>Paris</adresse>
     <dateArrive>2025-12-01</dateArrive>
     <dateDepart>2025-12-05</dateDepart>
   </rechercherChambresRequest>

2. AGENCE reçoit (AgenceEndpoint) :
   - Unmarshall XML → RechercherChambresRequest
   - Conversion en RechercheRequest DTO
   - Appel AgenceService.rechercherChambres()

3. AGENCE interroge les HÔTELS (HotelSoapClient) :
   Requête parallèle à :
   - http://localhost:8082/ws (Paris)
   - http://localhost:8083/ws (Lyon)
   - http://localhost:8084/ws (Montpellier)

4. Chaque HÔTEL répond :
   <rechercherChambresResponse>
     <chambres>
       <chambre>...</chambre>
     </chambres>
   </rechercherChambresResponse>

5. AGENCE agrège :
   - Conversion chambres SOAP → ChambreDTO
   - Ajout infos hôtel (nom, adresse)
   - Fusion dans une liste unique

6. AGENCE répond au CLIENT :
   - Conversion ChambreDTO → Chambre SOAP
   - Marshall Java → XML
   <rechercherChambresResponse>
     <chambres>
       <!-- Toutes les chambres de tous les hôtels -->
     </chambres>
   </rechercherChambresResponse>
```

---

## 🧪 Tests

### Test de connectivité
```bash
# Vérifier que l'agence peut joindre les hôtels
curl http://localhost:8081/ws?wsdl

# Les logs doivent montrer :
# "Connexion réussie à l'hôtel Paris"
# "Connexion réussie à l'hôtel Lyon"
# "Connexion réussie à l'hôtel Montpellier"
```

### Test d'une recherche (avec SoapUI)
1. Importer `http://localhost:8081/ws?wsdl`
2. Créer une requête `rechercherChambres`
3. Remplir les critères
4. Vérifier que la réponse contient des chambres de plusieurs hôtels

---

## 📝 Points Techniques Importants

### Génération des classes JAXB

Les classes sont générées à partir de **4 WSDL** :
- `agence.wsdl` : pour le serveur (endpoint)
- `hotel-paris.wsdl` : pour le client vers Paris
- `hotel-lyon.wsdl` : pour le client vers Lyon
- `hotel-montpellier.wsdl` : pour le client vers Montpellier

Configuration Maven (`pom.xml`) :
```xml
<plugin>
    <groupId>org.jvnet.jaxb2.maven2</groupId>
    <artifactId>maven-jaxb2-plugin</artifactId>
    <executions>
        <execution>
            <goals>
                <goal>generate</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <schemaDirectory>src/main/resources/wsdl</schemaDirectory>
        <generatePackage>org.tp1.agence.soap</generatePackage>
    </configuration>
</plugin>
```

### Isolation des erreurs

Si un hôtel ne répond pas :
```java
try {
    List<Chambre> chambres = hotelSoapClient.rechercherChambres(...);
    // Traiter les résultats
} catch (Exception e) {
    logger.error("Erreur hôtel Paris", e);
    // Continuer avec les autres hôtels
}
```

L'agence reste opérationnelle même si un hôtel est en panne.

### DTOs vs Classes SOAP

- **Classes SOAP** : générées par JAXB, utilisées pour la communication SOAP
- **DTOs** : classes métiers internes, utilisées entre les couches de l'agence
- **Conversion** : `AgenceService` et `AgenceEndpoint` font les conversions

**Avantage** : L'agence peut évoluer sans impacter les contrats SOAP.

---

## 🐛 Résolution de Problèmes

### Erreur : "Connexion refusée" à un hôtel
```
java.net.ConnectException: Connection refused
```
**Cause** : L'hôtel n'est pas démarré ou pas encore prêt.
**Solution** :
1. Vérifier que l'hôtel est bien lancé
2. Attendre 30-60 secondes après le démarrage
3. Vérifier l'URL dans `application.properties`

### Erreur : "Could not find endpoint"
```
org.springframework.ws.client.WebServiceTransportException
```
**Cause** : L'URL du WSDL est incorrecte.
**Solution** : Vérifier les URLs dans `application.properties` et `SoapClientConfig.java`.

### Aucune chambre retournée
**Cause possible** :
- Les hôtels ne sont pas démarrés
- Les critères de recherche ne correspondent à aucune chambre
- Erreur silencieuse (vérifier les logs)

**Solution** : Consulter les logs de l'agence et des hôtels.

### Classes JAXB en conflit
```
Error: Class already defined
```
**Cause** : Plusieurs WSDL définissent les mêmes classes.
**Solution** : Utiliser des packages différents ou des bindings customs dans le `pom.xml`.

---

## 📚 Documentation Complémentaire

- [WSDL Agence](src/main/resources/wsdl/agence.wsdl)
- [Documentation Spring-WS](https://docs.spring.io/spring-ws/docs/current/reference/)
- [README Hotellerie](../Hotellerie/README.md)

---

Retour au [README principal](../README.md)

