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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Client REST pour communiquer avec l'Agence
 * Remplace l'ancien AgenceSoapClient
 */
@Component
public class AgenceRestClient {

    private final RestTemplate restTemplate;

    @Value("${agence.url:http://localhost:8081}")
    private String agenceUrl;

    public AgenceRestClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Test de connexion à l'agence
     */
    public String ping() {
        try {
            String url = agenceUrl + "/api/agence/ping";

            @SuppressWarnings("unchecked")
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return (String) response.getBody().get("message");
            }

            return "Agence non disponible";

        } catch (RestClientException e) {
            System.err.println("❌ Erreur lors du ping de l'agence: " + e.getMessage());
            return "Erreur: " + e.getMessage();
        }
    }

    /**
     * Rechercher des chambres disponibles
     */
    public List<ChambreDTO> rechercherChambres(String adresse, String dateArrive, String dateDepart,
                                               Float prixMin, Float prixMax, Integer nbrEtoile, Integer nbrLits) {
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
                return Arrays.asList(response.getBody());
            }

            return Collections.emptyList();

        } catch (RestClientException e) {
            System.err.println("❌ Erreur lors de la recherche de chambres: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Effectuer une réservation
     */
    public ReservationResponse effectuerReservation(String clientNom, String clientPrenom,
                                                   String numeroCarteBancaire, int chambreId,
                                                   String hotelAdresse, String dateArrive, String dateDepart) {
        try {
            String url = agenceUrl + "/api/agence/reservations";

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
     * Obtenir la liste des hôtels disponibles
     */
    @SuppressWarnings("unchecked")
    public List<String> getHotelsDisponibles() {
        try {
            String url = agenceUrl + "/api/agence/hotels";

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return (List<String>) response.getBody().get("hotels");
            }

            return Collections.emptyList();

        } catch (RestClientException e) {
            System.err.println("❌ Erreur lors de la récupération des hôtels: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Obtenir toutes les chambres réservées de tous les hôtels
     */
    @SuppressWarnings("unchecked")
    public Map<String, List<ChambreDTO>> getChambresReservees() {
        try {
            String url = agenceUrl + "/api/agence/chambres/reservees";

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                Map<String, List<ChambreDTO>> result = new java.util.HashMap<>();

                // Convertir les objets Map en ChambreDTO
                for (Map.Entry<String, Object> entry : body.entrySet()) {
                    String hotelNom = entry.getKey();
                    List<Map<String, Object>> chambresData = (List<Map<String, Object>>) entry.getValue();

                    List<ChambreDTO> chambres = new ArrayList<>();
                    for (Map<String, Object> chambreData : chambresData) {
                        ChambreDTO chambre = new ChambreDTO();
                        chambre.setId(((Number) chambreData.get("id")).intValue());
                        chambre.setNom((String) chambreData.get("nom"));
                        chambre.setPrix(((Number) chambreData.get("prix")).floatValue());
                        chambre.setNbrLits(((Number) chambreData.get("nbrLits")).intValue());
                        chambre.setHotelNom((String) chambreData.get("hotelNom"));
                        chambre.setHotelAdresse((String) chambreData.get("hotelAdresse"));
                        chambre.setImageUrl((String) chambreData.get("imageUrl"));
                        chambres.add(chambre);
                    }

                    result.put(hotelNom, chambres);
                }

                return result;
            }

            return Collections.emptyMap();

        } catch (RestClientException e) {
            System.err.println("❌ Erreur lors de la récupération des chambres réservées: " + e.getMessage());
            return Collections.emptyMap();
        }
    }
}

