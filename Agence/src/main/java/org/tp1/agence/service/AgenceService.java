package org.tp1.agence.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tp1.agence.client.MultiHotelRestClient;
import org.tp1.agence.dto.ChambreDTO;
import org.tp1.agence.dto.RechercheRequest;
import org.tp1.agence.dto.ReservationRequest;
import org.tp1.agence.dto.ReservationResponse;

import java.util.List;
import java.util.Map;

/**
 * Service de l'agence qui orchestre les appels REST aux hôtels
 */
@Service
public class AgenceService {

    @Autowired
    private MultiHotelRestClient multiHotelRestClient;

    /**
     * Recherche des chambres disponibles dans tous les hôtels
     */
    public List<ChambreDTO> rechercherChambres(RechercheRequest request) {
        // Validation des paramètres
        if (request.getDateArrive() == null || request.getDateArrive().isEmpty()) {
            throw new IllegalArgumentException("La date d'arrivée est obligatoire");
        }
        if (request.getDateDepart() == null || request.getDateDepart().isEmpty()) {
            throw new IllegalArgumentException("La date de départ est obligatoire");
        }

        // Appel au client REST pour interroger les hôtels
        return multiHotelRestClient.rechercherChambres(request);
    }

    /**
     * Effectue une réservation dans un hôtel
     */
    public ReservationResponse effectuerReservation(ReservationRequest request) {
        // Validation des paramètres
        if (request.getClientNom() == null || request.getClientNom().isEmpty()) {
            return ReservationResponse.error("Le nom du client est obligatoire");
        }
        if (request.getClientPrenom() == null || request.getClientPrenom().isEmpty()) {
            return ReservationResponse.error("Le prénom du client est obligatoire");
        }
        if (request.getChambreId() <= 0) {
            return ReservationResponse.error("L'ID de la chambre est invalide");
        }

        try {
            // Appel au client REST pour effectuer la réservation
            return multiHotelRestClient.effectuerReservation(request);
        } catch (Exception e) {
            return ReservationResponse.error("Erreur lors de la réservation: " + e.getMessage());
        }
    }

    /**
     * Obtenir la liste des hôtels disponibles
     */
    public List<String> getHotelsDisponibles() {
        return multiHotelRestClient.getHotelsDisponibles();
    }

    /**
     * Obtenir toutes les chambres réservées de tous les hôtels
     */
    public Map<String, List<ChambreDTO>> getChambresReservees() {
        return multiHotelRestClient.getChambresReservees();
    }
}

