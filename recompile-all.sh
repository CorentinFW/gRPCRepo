#!/bin/bash

echo "═══════════════════════════════════════════════════════"
echo "  🔄 RECOMPILATION COMPLÈTE AVEC NOUVEAUX RPCs"
echo "═══════════════════════════════════════════════════════"
echo ""

# Arrêter tous les services existants
echo "🛑 Arrêt des services existants..."
pkill -f "Hotellerie-0.0.1-SNAPSHOT.jar" 2>/dev/null
pkill -f "Agence-0.0.1-SNAPSHOT.jar" 2>/dev/null
sleep 2
echo "✅ Services arrêtés"

echo ""
echo "═══════════════════════════════════════════════════════"
echo "  📦 ÉTAPE 1/4 : Compilation du module COMMUN"
echo "═══════════════════════════════════════════════════════"
cd commun
echo "🔨 mvn clean install..."
mvn clean install -DskipTests
if [ $? -ne 0 ]; then
    echo "❌ ERREUR lors de la compilation du module commun"
    exit 1
fi
echo "✅ Module commun compilé avec les nouveaux RPCs GetChambresReservees"
cd ..

echo ""
echo "═══════════════════════════════════════════════════════"
echo "  📦 ÉTAPE 2/4 : Compilation du module HOTELLERIE"
echo "═══════════════════════════════════════════════════════"
cd Hotellerie
echo "🔨 mvn clean package..."
mvn clean package -DskipTests
if [ $? -ne 0 ]; then
    echo "❌ ERREUR lors de la compilation de Hotellerie"
    exit 1
fi
echo "✅ Module Hotellerie compilé"
cd ..

echo ""
echo "═══════════════════════════════════════════════════════"
echo "  📦 ÉTAPE 3/4 : Compilation du module AGENCE"
echo "═══════════════════════════════════════════════════════"
cd Agence
echo "🔨 mvn clean package..."
mvn clean package -DskipTests
if [ $? -ne 0 ]; then
    echo "❌ ERREUR lors de la compilation d'Agence"
    exit 1
fi
echo "✅ Module Agence compilé"
cd ..

echo ""
echo "═══════════════════════════════════════════════════════"
echo "  📦 ÉTAPE 4/4 : Compilation du module CLIENT"
echo "═══════════════════════════════════════════════════════"
cd Client
echo "🔨 mvn clean package..."
mvn clean package -DskipTests
if [ $? -ne 0 ]; then
    echo "❌ ERREUR lors de la compilation du Client"
    exit 1
fi
echo "✅ Module Client compilé"
cd ..

echo ""
echo "═══════════════════════════════════════════════════════"
echo "  ✅ COMPILATION TERMINÉE AVEC SUCCÈS"
echo "═══════════════════════════════════════════════════════"
echo ""
echo "📊 JARs créés :"
ls -lh Hotellerie/target/*.jar | grep -v original | tail -1
ls -lh Agence/target/*.jar | grep -v original | tail -1
ls -lh Client/target/*.jar | grep -v original | tail -1

echo ""
echo "🚀 Prochaine étape :"
echo "   ./start-services-manual.sh"
echo ""

