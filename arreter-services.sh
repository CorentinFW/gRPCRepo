#!/bin/bash

# Script pour arrêter tous les services

echo "🛑 Arrêt des services gRPC..."
echo ""

# Arrêter Hotellerie
echo "Arrêt des hôtels..."
pkill -f 'Hotellerie-0.0.1-SNAPSHOT.jar'

# Arrêter Agence
echo "Arrêt des agences..."
pkill -f 'Agence-0.0.1-SNAPSHOT.jar'

sleep 2

echo ""
echo "✅ Tous les services sont arrêtés"
echo ""

