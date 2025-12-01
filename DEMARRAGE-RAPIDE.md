# 🚀 Guide de Démarrage Rapide - gRPC

## ❗ IMPORTANT : Ordre de démarrage

Pour que l'interface graphique fonctionne, vous DEVEZ démarrer les services dans cet ordre :

1. **D'abord les 3 hôtels** (Paris, Lyon, Montpellier)
2. **Ensuite les 2 agences** (Agence1, Agence2)
3. **Enfin le client** (Interface graphique)

---

## 🎯 Méthode 1 : Script automatique (Recommandé)

```bash
cd /home/corentinfay/Bureau/gRPCRepo
./start-services-manual.sh
```

Ce script va :
- Arrêter les anciens services
- Démarrer les 3 hôtels
- Attendre qu'ils soient prêts
- Démarrer les 2 agences
- Attendre qu'elles soient prêtes
- Afficher les PIDs de tous les services

**Temps total : ~30-40 secondes**

---

## 🎯 Méthode 2 : Démarrage manuel (étape par étape)

### Étape 1 : Démarrer les hôtels

Ouvrez 3 terminaux et lancez :

**Terminal 1 - Hôtel Paris :**
```bash
cd /home/corentinfay/Bureau/gRPCRepo/Hotellerie
java -jar target/Hotellerie-0.0.1-SNAPSHOT.jar --spring.profiles.active=paris
```
Attendez de voir : `Started HotellerieApplication` et `gRPC Server started, listening on address: *, port: 9092`

**Terminal 2 - Hôtel Lyon :**
```bash
cd /home/corentinfay/Bureau/gRPCRepo/Hotellerie
java -jar target/Hotellerie-0.0.1-SNAPSHOT.jar --spring.profiles.active=lyon
```
Attendez de voir : `gRPC Server started, listening on address: *, port: 9093`

**Terminal 3 - Hôtel Montpellier :**
```bash
cd /home/corentinfay/Bureau/gRPCRepo/Hotellerie
java -jar target/Hotellerie-0.0.1-SNAPSHOT.jar --spring.profiles.active=montpellier
```
Attendez de voir : `gRPC Server started, listening on address: *, port: 9094`

### Étape 2 : Démarrer les agences

Ouvrez 2 nouveaux terminaux et lancez :

**Terminal 4 - Agence 1 :**
```bash
cd /home/corentinfay/Bureau/gRPCRepo/Agence
java -jar target/Agence-0.0.1-SNAPSHOT.jar --spring.profiles.active=agence1
```
Attendez de voir : `gRPC Server started, listening on address: *, port: 9091`

**Terminal 5 - Agence 2 :**
```bash
cd /home/corentinfay/Bureau/gRPCRepo/Agence
java -jar target/Agence-0.0.1-SNAPSHOT.jar --spring.profiles.active=agence2
```
Attendez de voir : `gRPC Server started, listening on address: *, port: 9095`

### Étape 3 : Lancer l'interface graphique

Dans un 6ème terminal :

```bash
cd /home/corentinfay/Bureau/gRPCRepo/Client
java -Djava.awt.headless=false -jar target/Client-0.0.1-SNAPSHOT.jar
```

L'interface devrait maintenant afficher :
```
✓ Connexion établie: 2 agence(s) disponible(s): agence1, agence2
```

---

## 🔍 Vérification que tout fonctionne

### Vérifier les ports ouverts

```bash
netstat -tulpn | grep -E ":(8081|8082|8083|8084|8085|9091|9092|9093|9094|9095)"
```

Vous devriez voir 10 lignes (5 REST + 5 gRPC).

### Vérifier les processus

```bash
ps aux | grep java | grep -E "Hotellerie|Agence"
```

Vous devriez voir 5 processus Java.

### Tester manuellement avec grpcurl (optionnel)

Si vous avez `grpcurl` installé :

```bash
# Tester l'hôtel Paris
grpcurl -plaintext localhost:9092 list

# Tester l'agence 1
grpcurl -plaintext localhost:9091 list
```

---

## ❌ Dépannage

### Problème : "Aucune agence disponible"

**Cause :** Les agences ne sont pas démarrées OU les hôtels ne sont pas démarrés.

**Solution :**
1. Vérifiez que les 3 hôtels sont démarrés en premier
2. Puis vérifiez que les 2 agences sont démarrées
3. Relancez le client

### Problème : "Port already in use"

**Cause :** Un service est déjà en cours d'exécution sur le port.

**Solution :**
```bash
# Arrêter tous les services
pkill -f "Hotellerie-0.0.1-SNAPSHOT.jar"
pkill -f "Agence-0.0.1-SNAPSHOT.jar"

# Ou tuer un port spécifique
lsof -ti:9091 | xargs kill -9
```

### Problème : "Connection refused"

**Cause :** Les agences ne peuvent pas se connecter aux hôtels.

**Solution :**
1. Vérifiez que les 3 hôtels sont bien démarrés
2. Vérifiez les logs : `tail -f logs/agence1.log`
3. Cherchez des erreurs de connexion gRPC

### Voir les logs en temps réel

```bash
# Hôtels
tail -f logs/hotel-paris.log
tail -f logs/hotel-lyon.log
tail -f logs/hotel-montpellier.log

# Agences
tail -f logs/agence1.log
tail -f logs/agence2.log
```

---

## 🛑 Arrêter tous les services

```bash
# Méthode 1 : Par nom de JAR
pkill -f "Hotellerie-0.0.1-SNAPSHOT.jar"
pkill -f "Agence-0.0.1-SNAPSHOT.jar"

# Méthode 2 : Par port
lsof -ti:9091,9092,9093,9094,9095 | xargs kill -9

# Méthode 3 : Tous les processus Java (ATTENTION !)
pkill java
```

---

## 📊 Ports utilisés

| Service | Port REST | Port gRPC |
|---------|-----------|-----------|
| Hôtel Paris | 8082 | 9092 |
| Hôtel Lyon | 8083 | 9093 |
| Hôtel Montpellier | 8084 | 9094 |
| Agence 1 | 8081 | 9091 |
| Agence 2 | 8085 | 9095 |

---

## ✅ Test rapide dans l'interface

Une fois tous les services démarrés et l'interface ouverte :

1. Cliquez sur "Rechercher des chambres"
2. Entrez des dates (ex: 2025-01-01 à 2025-01-05)
3. Cliquez sur "Rechercher"
4. Vous devriez voir **20 chambres** disponibles

Si vous voyez les chambres, félicitations ! Le système gRPC fonctionne parfaitement ! 🎉

---

## 💡 Conseil

Pour éviter de devoir gérer 5 terminaux, utilisez le script `start-services-manual.sh` qui lance tout en arrière-plan et affiche les logs dans `./logs/`.

