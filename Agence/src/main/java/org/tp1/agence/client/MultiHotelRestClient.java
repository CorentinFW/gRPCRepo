package org.tp1.agence.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.tp1.agence.dto.ChambreDTO;
import org.tp1.agence.dto.RechercheRequest;
import org.tp1.agence.dto.ReservationRequest;
import org.tp1.agence.dto.ReservationResponse;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Client REST qui interroge plusieurs hôtels en parallèle
 * Remplace l'ancien MultiHotelSoapClient
 */
@Component
public class MultiHotelRestClient {

    @Autowired
    private HotelRestClient hotelRestClient;

    @Value("${agence.nom:Agence Inconnue}")
    private String agenceNom;

    @Value("${agence.coefficient:1.0}")
    private float agenceCoefficient;

    @Value("${hotel.paris.url:#{null}}")
    private String hotelParisUrl;

    @Value("${hotel.lyon.url:#{null}}")
    private String hotelLyonUrl;

    @Value("${hotel.montpellier.url:#{null}}")
    private String hotelMontpellierUrl;

    private List<String> hotelUrls = new ArrayList<>();

    @PostConstruct
    public void init() {
        // Initialiser la liste des URLs des hôtels (seulement ceux configurés)
        if (hotelParisUrl != null && !hotelParisUrl.isEmpty()) {
            hotelUrls.add(hotelParisUrl);
        }
        if (hotelLyonUrl != null && !hotelLyonUrl.isEmpty()) {
            hotelUrls.add(hotelLyonUrl);
        }
        if (hotelMontpellierUrl != null && !hotelMontpellierUrl.isEmpty()) {
            hotelUrls.add(hotelMontpellierUrl);
        }

        System.out.println("═══════════════════════════════════════════");
        System.out.println("  " + agenceNom + " - Configuration REST");
        System.out.println("  Coefficient de prix: " + agenceCoefficient);
        System.out.println("  Nombre d'hôtels: " + hotelUrls.size());
        if (hotelParisUrl != null) System.out.println("  - Hôtel Paris: " + hotelParisUrl);
        if (hotelLyonUrl != null) System.out.println("  - Hôtel Lyon: " + hotelLyonUrl);
        if (hotelMontpellierUrl != null) System.out.println("  - Hôtel Montpellier: " + hotelMontpellierUrl);
        System.out.println("═══════════════════════════════════════════");
    }

    /**
     * Recherche des chambres dans tous les hôtels en parallèle
     */
    public List<ChambreDTO> rechercherChambres(RechercheRequest request) {
        System.out.println("🔍 Recherche dans " + hotelUrls.size() + " hôtels...");

        // Créer des tâches asynchrones pour chaque hôtel
        List<CompletableFuture<List<ChambreDTO>>> futures = hotelUrls.stream()
            .map(hotelUrl -> CompletableFuture.supplyAsync(() -> {
                try {
                    List<ChambreDTO> chambres = hotelRestClient.rechercherChambres(hotelUrl, request);

                    if (!chambres.isEmpty()) {
                        // Récupérer les infos de l'hôtel pour enrichir les chambres
                        Map<String, Object> hotelInfo = hotelRestClient.getHotelInfo(hotelUrl);
                        String hotelNom = (String) hotelInfo.get("nom");
                        String hotelAdresse = (String) hotelInfo.get("adresse");

                        // Enrichir chaque chambre avec les infos de l'hôtel
                        for (ChambreDTO chambre : chambres) {
                            if (hotelNom != null) chambre.setHotelNom(hotelNom);
                            if (hotelAdresse != null) chambre.setHotelAdresse(hotelAdresse);

                            // Appliquer le coefficient de prix de l'agence
                            chambre.setPrix(chambre.getPrix() * agenceCoefficient);

                            // Ajouter le nom de l'agence
                            chambre.setAgenceNom(agenceNom);
                        }

                        System.out.println("✓ [" + hotelUrl + "] Trouvé " + chambres.size() + " chambre(s)");
                    } else {
                        System.out.println("○ [" + hotelUrl + "] Aucune chambre disponible");
                    }

                    return chambres;
                } catch (Exception e) {
                    System.err.println("✗ [" + hotelUrl + "] Erreur: " + e.getMessage());
                    return new ArrayList<ChambreDTO>();
                }
            }))
            .collect(Collectors.toList());

        // Attendre que toutes les tâches se terminent et agréger les résultats
        List<ChambreDTO> toutesLesChambres = futures.stream()
            .map(CompletableFuture::join)
            .flatMap(List::stream)
            .collect(Collectors.toList());

        System.out.println("✅ Total: " + toutesLesChambres.size() + " chambre(s) disponible(s)");

        return toutesLesChambres;
    }

    /**
     * Effectuer une réservation dans l'hôtel correspondant à l'adresse
     */
    public ReservationResponse effectuerReservation(ReservationRequest request) {
        String hotelAdresse = request.getHotelAdresse();

        System.out.println("🏨 Réservation pour l'hôtel: " + hotelAdresse);

        // Trouver l'URL de l'hôtel correspondant
        String targetHotelUrl = null;

        for (String hotelUrl : hotelUrls) {
            try {
                Map<String, Object> hotelInfo = hotelRestClient.getHotelInfo(hotelUrl);
                String adresse = (String) hotelInfo.get("adresse");

                if (adresse != null && adresse.equalsIgnoreCase(hotelAdresse)) {
                    targetHotelUrl = hotelUrl;
                    break;
                }
            } catch (Exception e) {
                System.err.println("Erreur lors de la vérification de l'hôtel " + hotelUrl + ": " + e.getMessage());
            }
        }

        if (targetHotelUrl == null) {
            System.err.println("❌ Hôtel non trouvé pour l'adresse: " + hotelAdresse);
            return ReservationResponse.error("Hôtel non trouvé");
        }

        // Effectuer la réservation
        System.out.println("→ Envoi de la réservation à " + targetHotelUrl);
        return hotelRestClient.effectuerReservation(targetHotelUrl, request);
    }

    /**
     * Obtenir la liste des URLs des hôtels disponibles
     */
    public List<String> getHotelsDisponibles() {
        List<String> hotelNames = new ArrayList<>();

        for (String hotelUrl : hotelUrls) {
            try {
                Map<String, Object> hotelInfo = hotelRestClient.getHotelInfo(hotelUrl);
                String nom = (String) hotelInfo.get("nom");
                if (nom != null) {
                    hotelNames.add(nom);
                } else {
                    hotelNames.add(hotelUrl);
                }
            } catch (Exception e) {
                hotelNames.add(hotelUrl + " (non accessible)");
            }
        }

        return hotelNames;
    }

    /**
     * Obtenir toutes les chambres réservées de tous les hôtels
     */
    public Map<String, List<ChambreDTO>> getChambresReservees() {
        System.out.println("🔍 Récupération des chambres réservées dans " + hotelUrls.size() + " hôtels...");

        Map<String, List<ChambreDTO>> chambresReserveesParHotel = new HashMap<>();

        for (String hotelUrl : hotelUrls) {
            try {
                // Récupérer les infos de l'hôtel
                Map<String, Object> hotelInfo = hotelRestClient.getHotelInfo(hotelUrl);
                String hotelNom = (String) hotelInfo.get("nom");
                String hotelAdresse = (String) hotelInfo.get("adresse");

                if (hotelNom == null) {
                    hotelNom = hotelUrl;
                }

                // Récupérer les chambres réservées
                List<ChambreDTO> chambres = hotelRestClient.getChambresReservees(hotelUrl);

                // Enrichir avec les infos de l'hôtel
                for (ChambreDTO chambre : chambres) {
                    chambre.setHotelNom(hotelNom);
                    if (hotelAdresse != null) {
                        chambre.setHotelAdresse(hotelAdresse);
                    }

                    // Appliquer le coefficient de prix de l'agence
                    chambre.setPrix(chambre.getPrix() * agenceCoefficient);

                    // Ajouter le nom de l'agence
                    chambre.setAgenceNom(agenceNom);
                }

                chambresReserveesParHotel.put(hotelNom, chambres);

                if (!chambres.isEmpty()) {
                    System.out.println("✓ [" + hotelNom + "] " + chambres.size() + " chambre(s) réservée(s)");
                } else {
                    System.out.println("○ [" + hotelNom + "] Aucune chambre réservée");
                }

            } catch (Exception e) {
                System.err.println("✗ [" + hotelUrl + "] Erreur: " + e.getMessage());
                chambresReserveesParHotel.put(hotelUrl, new ArrayList<>());
            }
        }

        return chambresReserveesParHotel;
    }
}

