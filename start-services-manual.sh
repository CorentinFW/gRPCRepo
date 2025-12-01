#!/bin/bash

# Script simplifié pour démarrer les services gRPC un par un

echo "═══════════════════════════════════════════════════════"
echo "  🚀 DÉMARRAGE MANUEL DES SERVICES gRPC"
echo "═══════════════════════════════════════════════════════"
echo ""

# Créer le répertoire logs
mkdir -p logs

# Fonction pour attendre qu'un service soit prêt
wait_for_service() {
    local port=$1
    local name=$2
    local pid=$3
    local logfile=$4
    echo -n "Attente du démarrage de $name (port $port)..."
    for i in {1..45}; do
        # Vérifier si le processus est toujours vivant
        if ! kill -0 $pid 2>/dev/null; then
            echo " ✗ PROCESSUS MORT"
            echo ""
            echo "❌ Le processus $name (PID: $pid) s'est arrêté !"
            echo "📋 Dernières lignes du log:"
            tail -20 "$logfile" 2>/dev/null
            echo ""
            return 1
        fi

        # Vérifier si le port est ouvert
        if nc -z localhost $port 2>/dev/null; then
            echo " ✓"
            return 0
        fi
        echo -n "."
        sleep 1
    done
    echo " ✗ TIMEOUT"
    echo ""
    echo "⚠️  Le service n'a pas ouvert le port $port après 45 secondes"
    echo "📋 Dernières lignes du log:"
    tail -20 "$logfile" 2>/dev/null
    echo ""
    return 1
}

# Arrêter les services existants
echo "🛑 Arrêt des services existants..."
pkill -f "Hotellerie-0.0.1-SNAPSHOT.jar" 2>/dev/null
pkill -f "Agence-0.0.1-SNAPSHOT.jar" 2>/dev/null
sleep 2

echo ""
echo "═══════════════════════════════════════════════════════"
echo "  🏨 DÉMARRAGE DES HÔTELS"
echo "═══════════════════════════════════════════════════════"

# Lancer Hôtel Paris
echo ""
echo "▶ Lancement Hôtel Paris (REST:8082, gRPC:9092)..."
cd Hotellerie
java -jar target/Hotellerie-0.0.1-SNAPSHOT.jar --spring.profiles.active=paris > ../logs/hotel-paris.log 2>&1 &
PARIS_PID=$!
echo "  PID: $PARIS_PID"
cd ..
wait_for_service 9092 "Hôtel Paris" $PARIS_PID "logs/hotel-paris.log" || exit 1

# Lancer Hôtel Lyon
echo ""
echo "▶ Lancement Hôtel Lyon (REST:8083, gRPC:9093)..."
cd Hotellerie
java -jar target/Hotellerie-0.0.1-SNAPSHOT.jar --spring.profiles.active=lyon > ../logs/hotel-lyon.log 2>&1 &
LYON_PID=$!
echo "  PID: $LYON_PID"
cd ..
wait_for_service 9093 "Hôtel Lyon" $LYON_PID "logs/hotel-lyon.log" || exit 1

# Lancer Hôtel Montpellier
echo ""
echo "▶ Lancement Hôtel Montpellier (REST:8084, gRPC:9094)..."
cd Hotellerie
java -jar target/Hotellerie-0.0.1-SNAPSHOT.jar --spring.profiles.active=montpellier > ../logs/hotel-montpellier.log 2>&1 &
MONTPELLIER_PID=$!
echo "  PID: $MONTPELLIER_PID"
cd ..
wait_for_service 9094 "Hôtel Montpellier" $MONTPELLIER_PID "logs/hotel-montpellier.log" || exit 1

echo ""
echo "═══════════════════════════════════════════════════════"
echo "  🏢 DÉMARRAGE DES AGENCES"
echo "═══════════════════════════════════════════════════════"

# Lancer Agence 1
echo ""
echo "▶ Lancement Agence 1 (REST:8081, gRPC:9091)..."
cd Agence
java -jar target/Agence-0.0.1-SNAPSHOT.jar --spring.profiles.active=agence1 > ../logs/agence1.log 2>&1 &
AGENCE1_PID=$!
echo "  PID: $AGENCE1_PID"
cd ..
wait_for_service 9091 "Agence 1" $AGENCE1_PID "logs/agence1.log" || exit 1

# Lancer Agence 2
echo ""
echo "▶ Lancement Agence 2 (REST:8085, gRPC:9095)..."
cd Agence
java -jar target/Agence-0.0.1-SNAPSHOT.jar --spring.profiles.active=agence2 > ../logs/agence2.log 2>&1 &
AGENCE2_PID=$!
echo "  PID: $AGENCE2_PID"
cd ..
wait_for_service 9095 "Agence 2" $AGENCE2_PID "logs/agence2.log" || exit 1

echo ""
echo "═══════════════════════════════════════════════════════"
echo "  ✅ TOUS LES SERVICES SONT DÉMARRÉS"
echo "═══════════════════════════════════════════════════════"
echo ""
echo "📊 Services actifs :"
echo "   Hôtel Paris      : PID $PARIS_PID      (gRPC: 9092)"
echo "   Hôtel Lyon       : PID $LYON_PID       (gRPC: 9093)"
echo "   Hôtel Montpellier: PID $MONTPELLIER_PID (gRPC: 9094)"
echo "   Agence 1         : PID $AGENCE1_PID     (gRPC: 9091)"
echo "   Agence 2         : PID $AGENCE2_PID     (gRPC: 9095)"
echo ""
echo "📝 Logs disponibles dans le répertoire ./logs/"
echo ""
echo "💡 Pour lancer l'interface graphique :"
echo "   ./grpc-client.sh"
echo ""
echo "💡 Pour voir les logs en temps réel :"
echo "   tail -f logs/agence1.log"
echo ""
echo "💡 Pour arrêter tous les services :"
echo "   pkill -f 'Hotellerie-0.0.1-SNAPSHOT.jar'"
echo "   pkill -f 'Agence-0.0.1-SNAPSHOT.jar'"
echo ""

