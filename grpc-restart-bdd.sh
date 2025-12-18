#!/bin/bash

# Script pour lancer tous les services gRPC avec reset des bases de données

echo "═══════════════════════════════════════════════════════"
echo "  🚀 DÉMARRAGE COMPLET (avec reset BDD)"
echo "═══════════════════════════════════════════════════════"

# Arrêter les services existants
echo ""
echo "🛑 Arrêt des services existants..."
./arreter-services.sh 2>/dev/null

# Supprimer les bases de données
echo ""
echo "🗑️  Suppression des bases de données H2..."
cd Hotellerie
rm -f data/hotellerie-*.db
rm -f data/hotellerie-*.trace.db
rm -f data/hotellerie-*.lock.db
echo "   ✅ Bases de données supprimées"
cd ..

# Compilation du module commun
echo ""
echo "📦 Compilation du module commun..."
cd commun
mvn clean install -q
if [ $? -ne 0 ]; then
    echo "❌ Erreur lors de la compilation du module commun"
    exit 1
fi
cd ..

# Compilation et lancement des hôtels
echo ""
echo "🏨 Compilation du module Hotellerie..."
cd Hotellerie
mvn clean package -DskipTests -q
if [ $? -ne 0 ]; then
    echo "❌ Erreur lors de la compilation de Hotellerie"
    exit 1
fi

echo "🏨 Lancement de l'Hôtel Paris (HTTP:8092, gRPC:9092)..."
nohup java -jar target/Hotellerie-0.0.1-SNAPSHOT.jar --spring.profiles.active=paris > ../logs/hotel-paris.log 2>&1 &
echo $! > ../logs/hotel-paris.pid
sleep 3

echo "🏨 Lancement de l'Hôtel Lyon (HTTP:8093, gRPC:9093)..."
nohup java -jar target/Hotellerie-0.0.1-SNAPSHOT.jar --spring.profiles.active=lyon > ../logs/hotel-lyon.log 2>&1 &
echo $! > ../logs/hotel-lyon.pid
sleep 3

echo "🏨 Lancement de l'Hôtel Montpellier (HTTP:8094, gRPC:9094)..."
nohup java -jar target/Hotellerie-0.0.1-SNAPSHOT.jar --spring.profiles.active=montpellier > ../logs/hotel-montpellier.log 2>&1 &
echo $! > ../logs/hotel-montpellier.pid
sleep 3

cd ..

# Compilation et lancement des agences
echo ""
echo "🏢 Compilation du module Agence..."
cd Agence
mvn clean package -DskipTests -q
if [ $? -ne 0 ]; then
    echo "❌ Erreur lors de la compilation d'Agence"
    exit 1
fi

echo "🏢 Lancement de l'Agence 1 (HTTP:8091, gRPC:9091)..."
nohup java -jar target/Agence-0.0.1-SNAPSHOT.jar --spring.profiles.active=agence1 > ../logs/agence1.log 2>&1 &
echo $! > ../logs/agence1.pid
sleep 3

echo "🏢 Lancement de l'Agence 2 (HTTP:8095, gRPC:9095)..."
nohup java -jar target/Agence-0.0.1-SNAPSHOT.jar --spring.profiles.active=agence2 > ../logs/agence2.log 2>&1 &
echo $! > ../logs/agence2.pid
sleep 3

cd ..

echo ""
echo "═══════════════════════════════════════════════════════"
echo "  ✅ TOUS LES SERVICES SONT DÉMARRÉS (BDD réinitialisées)"
echo "═══════════════════════════════════════════════════════"
echo ""
echo "⚠️  ATTENTION : Les bases de données ont été réinitialisées"
echo "   - Toutes les réservations précédentes sont supprimées"
echo "   - Les chambres ont été recréées avec les données par défaut"
echo ""
echo "📊 Hôtels gRPC:"
echo "   - Paris:       localhost:9092 (HTTP Images: 8092)"
echo "   - Lyon:        localhost:9093 (HTTP Images: 8093)"
echo "   - Montpellier: localhost:9094 (HTTP Images: 8094)"
echo ""
echo "📊 Agences gRPC:"
echo "   - Agence 1:    localhost:9091 (HTTP: 8091)"
echo "   - Agence 2:    localhost:9095 (HTTP: 8095)"
echo ""
echo "💡 Pour lancer l'interface graphique:"
echo "   ./grpc-client.sh"
echo ""
echo "💡 Pour arrêter les services:"
echo "   ./arreter-services.sh"
echo ""

