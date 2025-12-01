#!/bin/bash

# Script pour lancer le client GUI avec gRPC
echo "═══════════════════════════════════════════════════════"
echo "  🖥️  LANCEMENT DU CLIENT GUI (gRPC)"
echo "═══════════════════════════════════════════════════════"

cd Client

# Vérifier si le module est déjà compilé
if [ ! -f "target/Client-0.0.1-SNAPSHOT.jar" ]; then
    echo "📦 Compilation du module Client..."
    mvn clean package -DskipTests -q
    if [ $? -ne 0 ]; then
        echo "❌ Erreur lors de la compilation du Client"
        exit 1
    fi
fi

echo ""
echo "🚀 Lancement de l'interface graphique..."
echo ""

# Lancer le client avec la GUI
java -Djava.awt.headless=false -jar target/Client-0.0.1-SNAPSHOT.jar

cd ..

