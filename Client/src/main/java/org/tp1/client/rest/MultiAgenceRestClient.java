package org.tp1.client.rest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.tp1.client.dto.ChambreDTO;
import org.tp1.client.dto.RechercheRequest;
import org.tp1.client.dto.ReservationRequest;
import org.tp1.client.dto.ReservationResponse;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Client REST qui agrège les résultats de plusieurs agences
 * Permet de voir toutes les chambres, même celles en commun entre agences
 */
@Component
public class MultiAgenceRestClient {

    private final RestTemplate restTemplate;

    @Value("${agence1.url:http://localhost:8081}")
    private String agence1Url;

    @Value("${agence2.url:http://localhost:8085}")
    private String agence2Url;

    private List<String> agenceUrls;

    public MultiAgenceRestClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private void initAgenceUrls() {
        if (agenceUrls == null) {
            agenceUrls = new ArrayList<>();
            agenceUrls.add(agence1Url);
            agenceUrls.add(agence2Url);
        }
    }

    /**
     * Test de connexion aux agences
     */
    public String ping() {
        initAgenceUrls();
        StringBuilder result = new StringBuilder();

        for (String agenceUrl : agenceUrls) {
            try {
                String url = agenceUrl + "/api/agence/ping";
                @SuppressWarnings("unchecked")
                ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    result.append(response.getBody().get("message")).append(" | ");
                }
            } catch (RestClientException e) {
                result.append("[").append(agenceUrl).append(": ERREUR] | ");
            }
        }

        return result.length() > 0 ? result.toString() : "Aucune agence disponible";
    }

    /**
     * Rechercher des chambres disponibles dans TOUTES les agences
     * Retourne TOUTES les chambres, y compris les doublons (même chambre proposée par plusieurs agences)
     */
    public List<ChambreDTO> rechercherChambres(String adresse, String dateArrive, String dateDepart,
                                               Float prixMin, Float prixMax, Integer nbrEtoile, Integer nbrLits) {
        initAgenceUrls();

        System.out.println("🔍 Recherche dans " + agenceUrls.size() + " agences en parallèle...");

        // Créer des tâches asynchrones pour chaque agence
        List<CompletableFuture<List<ChambreDTO>>> futures = agenceUrls.stream()
            .<CompletableFuture<List<ChambreDTO>>>map(agenceUrl -> CompletableFuture.supplyAsync(() -> {
                try {
                    String url = agenceUrl + "/api/agence/chambres/rechercher";

                    // Créer la requête
                    RechercheRequest request = new RechercheRequest();
                    request.setAdresse(adresse != null ? adresse : "");
                    request.setDateArrive(dateArrive);
                    request.setDateDepart(dateDepart);
                    request.setPrixMin(prixMin != null ? prixMin : 0);
                    request.setPrixMax(prixMax != null ? prixMax : 0);
                    request.setNbrEtoile(nbrEtoile != null ? nbrEtoile : 0);
                    request.setNbrLits(nbrLits != null ? nbrLits : 0);

                    // Préparer les headers
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);

                    HttpEntity<RechercheRequest> requestEntity = new HttpEntity<>(request, headers);

                    // Appel POST
                    ResponseEntity<ChambreDTO[]> response = restTemplate.postForEntity(
                        url,
                        requestEntity,
                        ChambreDTO[].class
                    );

                    if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                        List<ChambreDTO> chambres = Arrays.asList(response.getBody());
                        System.out.println("✓ [" + agenceUrl + "] Trouvé " + chambres.size() + " chambre(s)");
                        return chambres;
                    }

                    return Collections.<ChambreDTO>emptyList();

                } catch (RestClientException e) {
                    System.err.println("✗ [" + agenceUrl + "] Erreur: " + e.getMessage());
                    return Collections.<ChambreDTO>emptyList();
                }
            }))
            .collect(Collectors.toList());

        // Attendre que toutes les tâches se terminent et agréger les résultats
        // On garde TOUS les résultats, même les doublons
        List<ChambreDTO> toutesLesChambres = futures.stream()
            .map(CompletableFuture::join)
            .flatMap(List::stream)
            .collect(Collectors.toList());

        System.out.println("✅ Total: " + toutesLesChambres.size() + " chambre(s) disponible(s) (avec doublons)");

        return toutesLesChambres;
    }

    /**
     * Effectuer une réservation
     * On choisit l'agence en fonction de l'agenceNom dans la chambre
     */
    public ReservationResponse effectuerReservation(String clientNom, String clientPrenom,
                                                   String numeroCarteBancaire, int chambreId,
                                                   String hotelAdresse, String dateArrive, String dateDepart,
                                                   String agenceNom) {
        initAgenceUrls();

        // Trouver l'URL de l'agence correspondante
        String targetAgenceUrl = null;
        for (String agenceUrl : agenceUrls) {
            try {
                String url = agenceUrl + "/api/agence/ping";
                @SuppressWarnings("unchecked")
                ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    String message = (String) response.getBody().get("message");
                    // Vérifier si c'est la bonne agence
                    if (message != null && message.contains(agenceNom)) {
                        targetAgenceUrl = agenceUrl;
                        break;
                    }
                }
            } catch (RestClientException e) {
                // Ignorer et continuer
            }
        }

        // Si on n'a pas trouvé l'agence par son nom, utiliser la première disponible
        if (targetAgenceUrl == null) {
            targetAgenceUrl = agenceUrls.get(0);
        }

        try {
            String url = targetAgenceUrl + "/api/agence/reservations";

            // Créer la requête
            ReservationRequest request = new ReservationRequest(
                clientNom,
                clientPrenom,
                numeroCarteBancaire,
                chambreId,
                hotelAdresse,
                dateArrive,
                dateDepart
            );

            // Préparer les headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<ReservationRequest> requestEntity = new HttpEntity<>(request, headers);

            // Appel POST
            ResponseEntity<ReservationResponse> response = restTemplate.postForEntity(
                url,
                requestEntity,
                ReservationResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }

            ReservationResponse errorResponse = new ReservationResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Erreur lors de la réservation");
            return errorResponse;

        } catch (RestClientException e) {
            System.err.println("❌ Erreur lors de la réservation: " + e.getMessage());
            ReservationResponse errorResponse = new ReservationResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Erreur: " + e.getMessage());
            return errorResponse;
        }
    }

    /**
     * Obtenir la liste des hôtels disponibles de toutes les agences
     */
    @SuppressWarnings("unchecked")
    public List<String> getHotelsDisponibles() {
        initAgenceUrls();
        Set<String> hotels = new HashSet<>();

        for (String agenceUrl : agenceUrls) {
            try {
                String url = agenceUrl + "/api/agence/hotels";
                ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    List<String> agenceHotels = (List<String>) response.getBody().get("hotels");
                    hotels.addAll(agenceHotels);
                }
            } catch (RestClientException e) {
                System.err.println("❌ Erreur lors de la récupération des hôtels: " + e.getMessage());
            }
        }

        return new ArrayList<>(hotels);
    }

    /**
     * Obtenir toutes les chambres réservées de toutes les agences
     */
    @SuppressWarnings("unchecked")
    public Map<String, List<ChambreDTO>> getChambresReservees() {
        initAgenceUrls();
        Map<String, List<ChambreDTO>> allChambres = new HashMap<>();
        // Map pour tracker les chambres déjà ajoutées (clé: hotelNom + chambreId)
        Set<String> chambresVues = new HashSet<>();

        for (String agenceUrl : agenceUrls) {
            try {
                String url = agenceUrl + "/api/agence/chambres/reservees";
                ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    Map<String, Object> body = response.getBody();

                    // Convertir les objets Map en ChambreDTO
                    for (Map.Entry<String, Object> entry : body.entrySet()) {
                        String hotelNom = entry.getKey();
                        List<Map<String, Object>> chambresData = (List<Map<String, Object>>) entry.getValue();

                        // Si l'hôtel existe déjà, ajouter les chambres, sinon créer une nouvelle liste
                        if (!allChambres.containsKey(hotelNom)) {
                            allChambres.put(hotelNom, new ArrayList<>());
                        }

                        for (Map<String, Object> chambreData : chambresData) {
                            int chambreId = ((Number) chambreData.get("id")).intValue();

                            // Créer une clé unique pour cette chambre
                            String cle = hotelNom + "_" + chambreId;

                            // Vérifier si cette chambre n'a pas déjà été ajoutée
                            if (!chambresVues.contains(cle)) {
                                ChambreDTO chambre = new ChambreDTO();
                                chambre.setId(chambreId);
                                chambre.setNom((String) chambreData.get("nom"));
                                chambre.setPrix(((Number) chambreData.get("prix")).floatValue());
                                chambre.setNbrLits(((Number) chambreData.get("nbrLits")).intValue());
                                chambre.setHotelNom((String) chambreData.get("hotelNom"));
                                chambre.setHotelAdresse((String) chambreData.get("hotelAdresse"));

                                if (chambreData.containsKey("imageUrl")) {
                                    chambre.setImageUrl((String) chambreData.get("imageUrl"));
                                }
                                if (chambreData.containsKey("agenceNom")) {
                                    chambre.setAgenceNom((String) chambreData.get("agenceNom"));
                                }

                                allChambres.get(hotelNom).add(chambre);
                                chambresVues.add(cle); // Marquer cette chambre comme vue
                            }
                        }
                    }
                }
            } catch (RestClientException e) {
                System.err.println("❌ Erreur lors de la récupération des chambres réservées: " + e.getMessage());
            }
        }

        return allChambres;
    }
}

