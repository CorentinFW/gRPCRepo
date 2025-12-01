package org.tp1.agence.client;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.tp1.agence.dto.ChambreDTO;
import org.tp1.agence.dto.RechercheRequest;
import org.tp1.agence.dto.ReservationRequest;
import org.tp1.agence.dto.ReservationResponse;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Client REST pour communiquer avec un hôtel via son API REST
 * Remplace l'ancien HotelSoapClient
 */
@Component
public class HotelRestClient {

    private final RestTemplate restTemplate;

    public HotelRestClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Obtenir les informations d'un hôtel
     */
    public Map<String, Object> getHotelInfo(String hotelBaseUrl) {
        try {
            String url = hotelBaseUrl + "/api/hotel/info";
            
            @SuppressWarnings("unchecked")
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            
            return Collections.emptyMap();
        } catch (RestClientException e) {
            System.err.println("❌ Erreur lors de la récupération des infos de l'hôtel " + hotelBaseUrl + ": " + e.getMessage());
            return Collections.emptyMap();
        }
    }

    /**
     * Rechercher des chambres disponibles dans un hôtel
     */
    public List<ChambreDTO> rechercherChambres(String hotelBaseUrl, RechercheRequest request) {
        try {
            String url = hotelBaseUrl + "/api/hotel/chambres/rechercher";
            
            // Préparer les headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Créer la requête avec body et headers
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
            System.err.println("❌ Erreur lors de la recherche de chambres à " + hotelBaseUrl + ": " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Effectuer une réservation dans un hôtel
     */
    public ReservationResponse effectuerReservation(String hotelBaseUrl, ReservationRequest request) {
        try {
            String url = hotelBaseUrl + "/api/hotel/reservations";
            
            // Préparer les headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Convertir ReservationRequest de l'agence vers le format attendu par l'hôtel
            Map<String, Object> hotelRequest = Map.of(
                "chambreId", request.getChambreId(),
                "dateArrive", request.getDateArrive(),
                "dateDepart", request.getDateDepart(),
                "nomClient", request.getClientNom(),
                "prenomClient", request.getClientPrenom(),
                "numeroCarteBancaire", request.getClientNumeroCarteBleue()
            );
            
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(hotelRequest, headers);
            
            // Appel POST
            ResponseEntity<ReservationResponse> response = restTemplate.postForEntity(
                url, 
                requestEntity, 
                ReservationResponse.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            
            return ReservationResponse.error("Erreur lors de la réservation");
            
        } catch (RestClientException e) {
            System.err.println("❌ Erreur lors de la réservation à " + hotelBaseUrl + ": " + e.getMessage());
            return ReservationResponse.error("Erreur de communication avec l'hôtel: " + e.getMessage());
        }
    }

    /**
     * Obtenir toutes les réservations d'un hôtel
     */
    public List<Object> getReservations(String hotelBaseUrl) {
        try {
            String url = hotelBaseUrl + "/api/hotel/reservations";
            
            ResponseEntity<Object[]> response = restTemplate.getForEntity(url, Object[].class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return Arrays.asList(response.getBody());
            }
            
            return Collections.emptyList();
            
        } catch (RestClientException e) {
            System.err.println("❌ Erreur lors de la récupération des réservations de " + hotelBaseUrl + ": " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Obtenir les chambres réservées d'un hôtel
     */
    public List<ChambreDTO> getChambresReservees(String hotelBaseUrl) {
        try {
            String url = hotelBaseUrl + "/api/hotel/chambres/reservees";

            ResponseEntity<ChambreDTO[]> response = restTemplate.getForEntity(url, ChambreDTO[].class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return Arrays.asList(response.getBody());
            }

            return Collections.emptyList();

        } catch (RestClientException e) {
            System.err.println("❌ Erreur lors de la récupération des chambres réservées de " + hotelBaseUrl + ": " + e.getMessage());
            return Collections.emptyList();
        }
    }
}

