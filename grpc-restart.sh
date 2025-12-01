#!/bin/bash

# Script pour lancer tous les services gRPC
echo "═══════════════════════════════════════════════════════"
echo "  🚀 DÉMARRAGE DES SERVICES gRPC"
echo "═══════════════════════════════════════════════════════"

# Arrêter les services existants
echo ""
echo "🛑 Arrêt des services existants..."
./arreter-services.sh 2>/dev/null

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

echo "🏨 Lancement de l'Hôtel Paris (REST:8082, gRPC:9092)..."
nohup java -jar target/Hotellerie-0.0.1-SNAPSHOT.jar --spring.profiles.active=paris > ../logs/hotel-paris.log 2>&1 &
echo $! > ../logs/hotel-paris.pid
sleep 3

echo "🏨 Lancement de l'Hôtel Lyon (REST:8083, gRPC:9093)..."
nohup java -jar target/Hotellerie-0.0.1-SNAPSHOT.jar --spring.profiles.active=lyon > ../logs/hotel-lyon.log 2>&1 &
echo $! > ../logs/hotel-lyon.pid
sleep 3

echo "🏨 Lancement de l'Hôtel Montpellier (REST:8084, gRPC:9094)..."
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

echo "🏢 Lancement de l'Agence 1 (REST:8081, gRPC:9091)..."
nohup java -jar target/Agence-0.0.1-SNAPSHOT.jar --spring.profiles.active=agence1 > ../logs/agence1.log 2>&1 &
echo $! > ../logs/agence1.pid
sleep 3

echo "🏢 Lancement de l'Agence 2 (REST:8085, gRPC:9095)..."
nohup java -jar target/Agence-0.0.1-SNAPSHOT.jar --spring.profiles.active=agence2 > ../logs/agence2.log 2>&1 &
echo $! > ../logs/agence2.pid
sleep 3

cd ..

echo ""
echo "═══════════════════════════════════════════════════════"
echo "  ✅ TOUS LES SERVICES gRPC SONT DÉMARRÉS"
echo "═══════════════════════════════════════════════════════"
echo ""
echo "📊 Hôtels gRPC:"
echo "   - Paris:       localhost:9092 (REST: 8082)"
echo "   - Lyon:        localhost:9093 (REST: 8083)"
echo "   - Montpellier: localhost:9094 (REST: 8084)"
echo ""
echo "📊 Agences gRPC:"
echo "   - Agence 1:    localhost:9091 (REST: 8081)"
echo "   - Agence 2:    localhost:9095 (REST: 8085)"
echo ""
echo "💡 Pour lancer l'interface graphique:"
echo "   ./grpc-client.sh"
echo ""
echo "💡 Pour arrêter les services:"
echo "   ./arreter-services.sh"
echo ""

