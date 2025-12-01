# 🏨 Système de Réservation Multi-Agences - gRPC

## 🎯 Architecture gRPC

```
CLIENT GUI (Interface Swing)
      │
      ├─ gRPC ──> AGENCE 1 (Paris Voyages - gRPC:9091, REST:8081)
      │           ├─ gRPC ──> Hôtel Paris (gRPC:9092, REST:8082)
      │           └─ gRPC ──> Hôtel Lyon (gRPC:9093, REST:8083)
      │
      └─ gRPC ──> AGENCE 2 (Sud Réservations - gRPC:9095, REST:8085)
                  ├─ gRPC ──> Hôtel Lyon (gRPC:9093, REST:8083) [Partagé]
                  └─ gRPC ──> Hôtel Montpellier (gRPC:9094, REST:8084)
```

## 🚀 DÉMARRAGE RAPIDE

### Option 1 : Lancement complet (recommandé)
```bash
./grpc-restart.sh
```

Attend ~60 secondes, puis lance l'interface graphique :
```bash
./grpc-client.sh
```

### Option 2 : Lancement manuel des services
```bash
# 1. Compiler le module commun
cd commun && mvn clean install && cd ..

# 2. Lancer les hôtels
cd Hotellerie && mvn clean package -DskipTests
java -jar target/Hotellerie-0.0.1-SNAPSHOT.jar --spring.profiles.active=paris &
java -jar target/Hotellerie-0.0.1-SNAPSHOT.jar --spring.profiles.active=lyon &
java -jar target/Hotellerie-0.0.1-SNAPSHOT.jar --spring.profiles.active=montpellier &
cd ..

# 3. Lancer les agences
cd Agence && mvn clean package -DskipTests
java -jar target/Agence-0.0.1-SNAPSHOT.jar --spring.profiles.active=agence1 &
java -jar target/Agence-0.0.1-SNAPSHOT.jar --spring.profiles.active=agence2 &
cd ..

# 4. Lancer le client GUI
cd Client && mvn clean package -DskipTests
java -Djava.awt.headless=false -jar target/Client-0.0.1-SNAPSHOT.jar
```

### Arrêter les services
```bash
./arreter-services.sh
```

## 📋 Prérequis

- ✅ Java 8+
- ✅ Maven 3.6+
- ✅ Ubuntu avec interface graphique (ou tout OS avec X11)
- ✅ Ports disponibles : 8081-8085 (REST) et 9091-9095 (gRPC)

## 🔧 Structure du Projet

### Module `commun`
Contient les définitions Protocol Buffers (`.proto`) et les classes Java générées :
- `hotel.proto` : Service HotelService avec messages pour recherche et réservation
- `agence.proto` : Service AgenceService qui agrège plusieurs hôtels

### Module `Hotellerie`
- **Serveur gRPC** : `HotelGrpcService` (implémente `HotelServiceGrpc`)
- **Ports gRPC** : Paris (9092), Lyon (9093), Montpellier (9094)
- **Base de données** : H2 pour persistence des chambres et réservations
- **Serveur REST** : Toujours disponible pour compatibilité

### Module `Agence`
- **Serveur gRPC** : `AgenceGrpcService` (implémente `AgenceServiceGrpc`)
- **Client gRPC** : `HotelGrpcClient` (communique avec les hôtels)
- **Ports gRPC** : Agence1 (9091), Agence2 (9095)
- **Serveur REST** : Toujours disponible pour compatibilité

### Module `Client`
- **Client gRPC** : `AgenceGrpcClient` (communique avec les agences)
- **Interface Swing** : `ClientGUI` utilise le client gRPC
- **Configuration** : `application.properties` avec adresses des agences

## 🎨 Fonctionnalités

### Via l'interface graphique
1. **Rechercher des chambres** : Critères (dates, prix, étoiles, lits)
2. **Réserver une chambre** : Sélectionner une chambre et remplir les infos client
3. **Voir les réservations** : Afficher toutes les réservations
4. **Hôtels disponibles** : Liste des hôtels connectés

### Données initiales
- **Hôtel Paris** : 5 chambres (60€ - 200€)
- **Hôtel Lyon** : 5 chambres (50€ - 150€)
- **Hôtel Montpellier** : 5 chambres (45€ - 140€)
- **Total** : 20 chambres disponibles au démarrage

## 📡 Configuration gRPC

### Fichiers de configuration

**Hotellerie** (`application-paris.properties`) :
```properties
grpc.server.port=9092
```

**Agence** (`application-agence1.properties`) :
```properties
grpc.server.port=9091
grpc.client.hotel-paris.address=static://localhost:9092
grpc.client.hotel-paris.negotiationType=PLAINTEXT
grpc.client.hotel-lyon.address=static://localhost:9093
grpc.client.hotel-lyon.negotiationType=PLAINTEXT
```

**Client** (`application.properties`) :
```properties
grpc.client.agence1.address=static://localhost:9091
grpc.client.agence1.negotiationType=PLAINTEXT
grpc.client.agence2.address=static://localhost:9095
grpc.client.agence2.negotiationType=PLAINTEXT
```

## 🧪 Tests

### Tester la connexion gRPC
Les logs confirment les connexions gRPC :
```
✅ [gRPC] Hotel Paris démarré sur le port 9092
✅ [gRPC] Agence 1 démarrée sur le port 9091
🔍 [gRPC Client] Recherche via agence1
✅ [gRPC Client] 20 chambres trouvées via agence1
```

### Vérifier les logs
```bash
tail -f logs/hotel-paris.log
tail -f logs/agence1.log
```

## 🔄 Migration REST → gRPC

### Changements principaux
1. **Module commun** : Ajout des fichiers `.proto` et génération automatique des classes
2. **Dépendances** : Ajout de `grpc-server-spring-boot-starter` et `grpc-client-spring-boot-starter`
3. **Services** : Implémentation des `ImplBase` générés par protobuf
4. **Clients** : Utilisation de `@GrpcClient` pour injection des stubs
5. **Configuration** : Ports gRPC et adresses dans `application.properties`

### Compatibilité
Les endpoints REST sont conservés pour assurer une migration progressive.

## 📚 Technologies Utilisées

- **Spring Boot 2.7.18** : Framework d'application
- **gRPC 1.42.1** : Communication RPC haute performance
- **Protocol Buffers 3.19.2** : Sérialisation des messages
- **grpc-spring-boot-starter 2.14.0** : Intégration Spring Boot + gRPC
- **H2 Database** : Base de données embarquée
- **Swing** : Interface graphique

## 🐛 Dépannage

### Les services ne démarrent pas
```bash
# Vérifier les ports occupés
netstat -tulpn | grep -E ':(8081|8082|8083|8084|8085|9091|9092|9093|9094|9095)'

# Libérer les ports si nécessaire
./arreter-services.sh
```

### Erreurs de connexion gRPC
```bash
# Vérifier que le module commun est installé
cd commun && mvn clean install

# Recompiler tous les modules
./grpc-restart.sh
```

### Interface graphique ne s'affiche pas
```bash
# Vérifier que X11 fonctionne
echo $DISPLAY

# Lancer avec option headless désactivée
java -Djava.awt.headless=false -jar Client/target/Client-0.0.1-SNAPSHOT.jar
```

## 📝 Logs

Les logs sont sauvegardés dans le répertoire `logs/` :
- `hotel-paris.log`, `hotel-lyon.log`, `hotel-montpellier.log`
- `agence1.log`, `agence2.log`

## 🎓 Auteur

Projet de démonstration - Transformation REST vers gRPC avec Spring Boot

