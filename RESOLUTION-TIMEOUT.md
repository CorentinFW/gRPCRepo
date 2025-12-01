# 🔧 Résolution du problème TIMEOUT au démarrage

## 🎯 Problème rencontré

Lors du lancement de `./start-services-manual.sh`, vous avez obtenu :

```
▶ Lancement Hôtel Paris (REST:8082, gRPC:9092)...
  PID: 43216
Attente du démarrage de Hôtel Paris (port 9092)................................. ✗ TIMEOUT
```

**Cause principale : Les JARs n'ont pas été recompilés avec les nouveaux RPCs `GetChambresReservees`**

---

## ✅ SOLUTION COMPLÈTE

### Étape 1 : Utiliser le script de recompilation automatique

J'ai créé un script qui fait TOUT dans le bon ordre :

```bash
cd /home/corentinfay/Bureau/gRPCRepo
./recompile-all.sh
```

**Ce script va :**
1. ✅ Arrêter tous les services existants
2. ✅ Recompiler `commun` avec les nouveaux RPCs
3. ✅ Recompiler `Hotellerie` avec les nouvelles méthodes
4. ✅ Recompiler `Agence` avec les nouvelles implémentations
5. ✅ Recompiler `Client` avec les nouveaux appels

**⏱️ Temps estimé : 3-4 minutes**

### Étape 2 : Redémarrer les services

Une fois la recompilation terminée :

```bash
./start-services-manual.sh
```

**Cette fois-ci :**
- ✅ Le script attend 45 secondes au lieu de 30
- ✅ Il vérifie que les processus sont vivants
- ✅ Il affiche les logs en cas d'erreur
- ✅ Il s'arrête automatiquement si un service crashe

---

## 🔍 Diagnostic des erreurs

Si vous obtenez encore un TIMEOUT ou PROCESSUS MORT, le script affichera maintenant :

### Exemple 1 : Processus mort (erreur de compilation)
```
Attente du démarrage de Hôtel Paris (port 9092)... ✗ PROCESSUS MORT

❌ Le processus Hôtel Paris (PID: 43216) s'est arrêté !
📋 Dernières lignes du log:
Error: Unable to access jarfile target/Hotellerie-0.0.1-SNAPSHOT.jar
```

**Solution :** Recompiler avec `./recompile-all.sh`

### Exemple 2 : Port déjà utilisé
```
📋 Dernières lignes du log:
***************************
APPLICATION FAILED TO START
***************************

Description:

Web server failed to start. Port 9092 was already in use.
```

**Solution :**
```bash
# Trouver le processus qui utilise le port
lsof -ti:9092

# Le tuer
lsof -ti:9092 | xargs kill -9

# Ou tuer tous les services Java
pkill -f "Hotellerie-0.0.1-SNAPSHOT.jar"
```

### Exemple 3 : Erreur de méthode non implémentée
```
📋 Dernières lignes du log:
java.lang.AbstractMethodError: Method 
org/tp1/hotellerie/grpc/HotelGrpcService.getChambresReservees(...)
is abstract
```

**Solution :** Module `commun` pas recompilé. Relancer `./recompile-all.sh`

---

## 📊 Commandes de vérification manuelle

### Vérifier que les JARs sont bien créés
```bash
ls -lh Hotellerie/target/Hotellerie-0.0.1-SNAPSHOT.jar
ls -lh Agence/target/Agence-0.0.1-SNAPSHOT.jar
ls -lh Client/target/Client-0.0.1-SNAPSHOT.jar
```

### Vérifier que le module commun contient les nouveaux stubs
```bash
jar tf commun/target/commun-0.0.1-SNAPSHOT.jar | grep ChambresReservees
```

**Vous devriez voir :**
```
org/tp1/commun/grpc/hotel/ChambresReserveesRequest.class
org/tp1/commun/grpc/hotel/ChambresReserveesResponse.class
org/tp1/commun/grpc/agence/ChambresReserveesRequest.class
org/tp1/commun/grpc/agence/ChambresReserveesParHotelResponse.class
org/tp1/commun/grpc/agence/HotelChambresReservees.class
```

### Tester le démarrage manuel d'un service

Pour voir les erreurs en direct :

```bash
cd Hotellerie
java -jar target/Hotellerie-0.0.1-SNAPSHOT.jar --spring.profiles.active=paris
```

**Appuyez sur Ctrl+C pour arrêter**

Si vous voyez :
```
gRPC Server started, listening on address: *, port: 9092
```

→ **Le service démarre correctement !**

---

## 🚀 Procédure complète pas à pas

### Option A : Automatique (RECOMMANDÉ)

```bash
cd /home/corentinfay/Bureau/gRPCRepo

# 1. Recompiler tout
./recompile-all.sh

# Attendre la fin (3-4 minutes)

# 2. Lancer les services
./start-services-manual.sh

# Attendre ~30-45 secondes

# 3. Lancer l'interface
./grpc-client.sh
```

### Option B : Manuel (pour déboguer)

```bash
cd /home/corentinfay/Bureau/gRPCRepo

# 1. Arrêter les services
pkill -f "Hotellerie-0.0.1-SNAPSHOT.jar"
pkill -f "Agence-0.0.1-SNAPSHOT.jar"
sleep 2

# 2. Recompiler commun
cd commun
mvn clean install -DskipTests
cd ..

# 3. Recompiler et tester Hotellerie
cd Hotellerie
mvn clean package -DskipTests

# Tester le démarrage
java -jar target/Hotellerie-0.0.1-SNAPSHOT.jar --spring.profiles.active=paris

# Si ça fonctionne (vous voyez "gRPC Server started"), Ctrl+C et continuer
cd ..

# 4. Recompiler Agence
cd Agence
mvn clean package -DskipTests
cd ..

# 5. Recompiler Client
cd Client
mvn clean package -DskipTests
cd ..

# 6. Utiliser le script de démarrage
./start-services-manual.sh
```

---

## 🔑 Points clés à retenir

### ✅ Ordre de compilation CRUCIAL

```
1. COMMUN       (génère les stubs)
      ↓
2. HOTELLERIE   (utilise les stubs)
      ↓
3. AGENCE       (utilise les stubs)
      ↓
4. CLIENT       (utilise les stubs)
```

**Si vous compilez dans le mauvais ordre, ça ne marchera PAS !**

### ✅ Le script `recompile-all.sh` fait tout automatiquement

- Arrête les services
- Compile dans le bon ordre
- Vérifie les erreurs à chaque étape
- S'arrête si une erreur se produit

### ✅ Le script amélioré `start-services-manual.sh`

- Attend 45 secondes au lieu de 30
- Vérifie que le processus est vivant
- Affiche les logs en cas d'erreur
- S'arrête automatiquement si un service crashe

---

## 📝 Logs utiles

### Voir les logs en temps réel

```bash
# Hôtel Paris
tail -f logs/hotel-paris.log

# Hôtel Lyon
tail -f logs/hotel-lyon.log

# Hôtel Montpellier
tail -f logs/hotel-montpellier.log

# Agence 1
tail -f logs/agence1.log

# Agence 2
tail -f logs/agence2.log
```

### Chercher les erreurs dans tous les logs

```bash
grep -i "error\|exception\|failed" logs/*.log
```

---

## ❓ FAQ

### Q1 : "Le script recompile-all.sh est bloqué à l'étape 1"

**R :** Maven télécharge les dépendances. Attendez 2-3 minutes la première fois.

### Q2 : "J'ai une erreur 'BUILD FAILURE' lors de la compilation"

**R :** Vérifiez les erreurs affichées. Souvent c'est :
- Un fichier .proto mal formé
- Une méthode Java qui n'existe pas
- Un import manquant

### Q3 : "Les services démarrent mais l'interface ne les voit pas"

**R :** 
1. Vérifiez que les services affichent "gRPC Server started"
2. Vérifiez les ports avec : `netstat -tulpn | grep 909`
3. Relancez l'interface graphique

### Q4 : "Dois-je recompiler à chaque fois ?"

**R :** Non, seulement après avoir modifié :
- Les fichiers `.proto` (module commun)
- Le code Java des services gRPC
- Les dépendances dans `pom.xml`

---

## 🎯 Résumé

**Pour résoudre le TIMEOUT :**

```bash
# UNE SEULE COMMANDE !
./recompile-all.sh && ./start-services-manual.sh
```

**Puis dans un autre terminal :**

```bash
./grpc-client.sh
```

**C'est tout !** 🎉

---

## ✅ Checklist finale

Avant de tester l'interface :

- [ ] `./recompile-all.sh` terminé sans erreur
- [ ] Message "✅ COMPILATION TERMINÉE AVEC SUCCÈS"
- [ ] `./start-services-manual.sh` terminé sans TIMEOUT
- [ ] Message "✅ TOUS LES SERVICES SONT DÉMARRÉS"
- [ ] 5 processus Java en cours : `ps aux | grep java | grep SNAPSHOT | wc -l` → doit afficher `5`
- [ ] Interface graphique affiche "2 agence(s) disponible(s)"

**Si tous les points sont cochés → La fonctionnalité "Voir les réservations" est opérationnelle !** 🚀

