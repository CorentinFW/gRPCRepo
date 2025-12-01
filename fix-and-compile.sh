#!/bin/bash

echo "═══════════════════════════════════════════════════════"
echo "  🔧 CORRECTION + RECOMPILATION RAPIDE"
echo "═══════════════════════════════════════════════════════"
echo ""

cd /home/corentinfay/Bureau/gRPCRepo

echo "✅ Correction déjà appliquée : getImageUrl() → getImage()"
echo ""

echo "📦 Compilation de Hotellerie uniquement..."
cd Hotellerie
mvn clean package -DskipTests

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Hotellerie compilé avec succès !"
    echo ""
    echo "📦 Compilation d'Agence..."
    cd ../Agence
    mvn clean package -DskipTests

    if [ $? -eq 0 ]; then
        echo ""
        echo "✅ Agence compilé avec succès !"
        echo ""
        echo "📦 Compilation de Client..."
        cd ../Client
        mvn clean package -DskipTests

        if [ $? -eq 0 ]; then
            echo ""
            echo "═══════════════════════════════════════════════════════"
            echo "  ✅ TOUT EST COMPILÉ AVEC SUCCÈS !"
            echo "═══════════════════════════════════════════════════════"
            echo ""
            echo "🚀 Prochaine étape :"
            echo "   ./start-services-manual.sh"
            echo ""
        else
            echo "❌ Erreur lors de la compilation du Client"
            exit 1
        fi
    else
        echo "❌ Erreur lors de la compilation d'Agence"
        exit 1
    fi
else
    echo "❌ Erreur lors de la compilation de Hotellerie"
    exit 1
fi

