package org.tp1.client.grpc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tp1.client.dto.ChambreDTO;
import org.tp1.client.dto.RechercheRequest;
import org.tp1.client.dto.ReservationRequest;
import org.tp1.client.dto.ReservationResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Service pour gérer plusieurs agences via gRPC
 * Remplace MultiAgenceRestClient
 */
@Service
public class MultiAgenceGrpcClient {

    @Autowired
    private AgenceGrpcClient agenceGrpcClient;

    private final List<String> agences = Arrays.asList("agence1", "agence2");

    /**
     * Rechercher des chambres dans toutes les agences
     */
    public List<ChambreDTO> rechercherChambres(RechercheRequest request) {
        List<ChambreDTO> toutesLesChambres = new ArrayList<>();

        System.out.println("🔍 [Multi-Agence gRPC] Recherche dans toutes les agences...");

        for (String agence : agences) {
            try {
                List<ChambreDTO> chambres = agenceGrpcClient.rechercherChambres(agence, request);
                toutesLesChambres.addAll(chambres);
            } catch (Exception e) {
                System.err.println("❌ Erreur lors de la recherche dans " + agence + ": " + e.getMessage());
            }
        }

        System.out.println("✅ [Multi-Agence gRPC] Total: " + toutesLesChambres.size() + " chambres trouvées");
        return toutesLesChambres;
    }

    /**
     * Effectuer une réservation via une agence
     * L'agence est déterminée automatiquement selon l'adresse de l'hôtel
     */
    public ReservationResponse effectuerReservation(ReservationRequest request) {
        // Essayer avec la première agence, puis la deuxième si échec
        for (String agence : agences) {
            try {
                ReservationResponse response = agenceGrpcClient.effectuerReservation(agence, request);
                if (response.isSuccess()) {
                    return response;
                }
            } catch (Exception e) {
                System.err.println("❌ Erreur lors de la réservation via " + agence + ": " + e.getMessage());
            }
        }

        return new ReservationResponse(0, "Échec de la réservation dans toutes les agences", false);
    }

    /**
     * Vérifier la disponibilité des agences
     */
    public List<String> getAgencesDisponibles() {
        List<String> disponibles = new ArrayList<>();

        for (String agence : agences) {
            if (agenceGrpcClient.ping(agence)) {
                disponibles.add(agence);
            }
        }

        return disponibles;
    }

    /**
     * Ping toutes les agences et retourner un message de statut
     */
    public String ping() {
        List<String> disponibles = getAgencesDisponibles();
        if (disponibles.isEmpty()) {
            return "Aucune agence disponible";
        }
        return disponibles.size() + " agence(s) disponible(s): " + String.join(", ", disponibles);
    }

    /**
     * Obtenir la liste des hôtels disponibles
     * Note: Cette méthode n'est pas encore implémentée en gRPC
     */
    public List<String> getHotelsDisponibles() {
        // Pour l'instant, retourner une liste fixe
        List<String> hotels = new ArrayList<>();
        hotels.add("Hotel Paris");
        hotels.add("Hotel Lyon");
        hotels.add("Hotel Montpellier");
        return hotels;
    }

    /**
     * Obtenir les chambres réservées de tous les hôtels
     */
    public java.util.Map<String, List<ChambreDTO>> getChambresReservees() {
        java.util.Map<String, List<ChambreDTO>> toutesLesChambres = new java.util.HashMap<>();

        System.out.println("📋 [Multi-Agence gRPC] Récupération des chambres réservées...");

        // Récupérer les chambres réservées de la première agence disponible
        for (String agence : agences) {
            try {
                java.util.Map<String, List<ChambreDTO>> chambres = agenceGrpcClient.getChambresReservees(agence);

                // Fusionner les résultats
                for (java.util.Map.Entry<String, List<ChambreDTO>> entry : chambres.entrySet()) {
                    String hotelNom = entry.getKey();
                    List<ChambreDTO> chambresHotel = entry.getValue();

                    if (toutesLesChambres.containsKey(hotelNom)) {
                        // Ajouter à la liste existante
                        toutesLesChambres.get(hotelNom).addAll(chambresHotel);
                    } else {
                        // Créer une nouvelle liste
                        toutesLesChambres.put(hotelNom, new ArrayList<>(chambresHotel));
                    }
                }

                // Retourner dès qu'on a des résultats (pas besoin de toutes les agences)
                if (!toutesLesChambres.isEmpty()) {
                    break;
                }
            } catch (Exception e) {
                System.err.println("❌ Erreur lors de la récupération dans " + agence + ": " + e.getMessage());
            }
        }

        System.out.println("✅ [Multi-Agence gRPC] Total: " + toutesLesChambres.size() + " hôtel(s) avec des réservations");
        return toutesLesChambres;
    }
}
