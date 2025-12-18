# 🏨 Système de Réservation d'Hôtel - gRPC

> **Architecture** : gRPC + Spring Boot + H2 + Swing  
> **Statut** : ✅ 100% Fonctionnel

---

## 🚀 DÉMARRAGE RAPIDE

### Démarrage Normal (Conserve les Données)
```bash
./grpc-restart.sh    # Redémarre les services (garde la BDD)
./grpc-client.sh     # Lance l'interface graphique
```

### Démarrage avec Reset BDD (Données Effacées)
```bash
./grpc-restart-bdd.sh    # Redémarre tout + supprime les BDD
./grpc-client.sh         # Lance l'interface graphique
```

### Arrêter les Services
```bash
./arreter-services.sh
```

---

## 📋 Prérequis

- ✅ Java 8+
- ✅ Maven 3.6+
- ✅ Ubuntu/Linux avec interface graphique

---

## 🎯 Architecture

```
CLIENT GUI (Interface Swing)
    ↓ gRPC
AGENCES (2 instances)
 - Agence 1 : gRPC 9091
 - Agence 2 : gRPC 9095
    ↓ gRPC
HÔTELS (3 instances + BDD H2)
 - Paris       : gRPC 9092 | HTTP 8092 (images)
 - Lyon        : gRPC 9093 | HTTP 8093 (images)
 - Montpellier : gRPC 9094 | HTTP 8094 (images)
```

---

## 🎮 Utilisation de l'Interface

### Démarrage

```bash
./start-system-maven.sh
```

**Le script démarre automatiquement :**
- Les 3 hôtels (Paris, Lyon, Montpellier)
- Les 2 agences (Agence 1, Agence 2)
- L'interface graphique Swing

### Recherche de Chambres

1. Remplir le formulaire (ville, dates, critères)
2. Cliquer sur "🔍 Rechercher"
3. Les résultats apparaissent dans le tableau

**Exemple :**
- Ville : Lyon
- Dates : 2025-12-01 → 2025-12-05
- **Résultat : 10 chambres**

### Afficher les Images

**Cliquer sur l'icône 🖼 dans le tableau**

→ Une fenêtre s'ouvre avec l'image de la chambre en grand format !

### Réservation

1. Sélectionner une chambre dans le tableau
2. Double-cliquer ou bouton "📝 Réserver"
3. Remplir le formulaire client
4. Valider

### Arrêter le Système

```bash
./arreter-services.sh
```

### Menus

- **Fichier** → Quitter
- **Actions** → Rechercher (Ctrl+R), Réserver (Ctrl+B), Voir réservations (Ctrl+V)
- **Aide** → À propos

---

## ✨ Fonctionnalités

✅ **Communication gRPC** entre tous les services  
✅ **Recherche de chambres** avec critères (dates, prix, lits)  
✅ **Réservation** avec validation  
✅ **Historique** des réservations  
✅ **Images** des chambres qui s'affichent  
✅ **Base de données H2** persistante par hôtel  
✅ **Interface Swing** intuitive  

---

## 📦 Structure du Projet

```
gRPCRepo/
├── commun/           # Fichiers .proto (hotel.proto, agence.proto)
├── Hotellerie/       # Service Hôtel (3 instances)
├── Agence/           # Service Agence (2 instances)
├── Client/           # Interface graphique Swing
└── logs/             # Fichiers de logs
```

---

## 🛠️ Scripts Disponibles

| Script | Description |
|--------|-------------|
| `grpc-restart.sh` | Redémarre les services (conserve les données) |
| `grpc-restart-bdd.sh` | Redémarre tout + reset des BDD |
| `grpc-client.sh` | Lance l'interface graphique |
| `arreter-services.sh` | Arrête tous les services |

---

## 🎯 Différence entre les Scripts

### grpc-restart.sh
- ✅ Recompile le code
- ✅ Redémarre les services
- ✅ **Conserve les réservations** en base
- ℹ️ Utiliser pour développement normal

### grpc-restart-bdd.sh  
- ✅ Recompile le code
- ✅ **Supprime les bases de données**
- ✅ Redémarre les services
- ✅ Recrée les chambres par défaut
- ⚠️ **Toutes les réservations sont perdues**
- ℹ️ Utiliser pour repartir à zéro

---

**Projet terminé avec succès ! 🎉**
