package org.tp1.hotellerie.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tp1.hotellerie.dto.*;
import org.tp1.hotellerie.model.Chambre;
import org.tp1.hotellerie.model.Client;
import org.tp1.hotellerie.model.Hotel;
import org.tp1.hotellerie.model.Reservation;
import org.tp1.hotellerie.service.HotelService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Contrôleur REST pour les services de l'hôtel
 * Expose l'API REST de l'hôtel (remplacement du SOAP Endpoint)
 */
@RestController
@RequestMapping("/api/hotel")
@Tag(name = "Hôtel", description = "API REST pour la gestion de l'hôtel")
public class HotelController {

    @Autowired
    private HotelService hotelService;

    /**
     * GET /api/hotel/info - Retourne les informations de l'hôtel
     */
    @GetMapping("/info")
    @Operation(summary = "Obtenir les informations de l'hôtel", description = "Retourne le nom, l'adresse et les détails de l'hôtel")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Informations récupérées avec succès")
    })
    public ResponseEntity<HotelInfoDTO> getHotelInfo() {
        Hotel hotel = hotelService.getHotel();

        HotelInfoDTO info = new HotelInfoDTO(
            hotel.getNom(),
            hotel.getAdresse(),
            null, // Ville non disponible dans le modèle actuel
            null  // Téléphone non disponible
        );

        return ResponseEntity.ok(info);
    }

    /**
     * POST /api/hotel/chambres/rechercher - Rechercher des chambres disponibles
     */
    @PostMapping("/chambres/rechercher")
    @Operation(summary = "Rechercher des chambres disponibles", description = "Recherche des chambres selon des critères (adresse, dates, prix, étoiles, lits)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Recherche effectuée avec succès"),
        @ApiResponse(responseCode = "400", description = "Paramètres de recherche invalides")
    })
    public ResponseEntity<List<ChambreDTO>> rechercherChambres(
            @RequestBody @Parameter(description = "Critères de recherche") RechercheRequest request) {

        List<Chambre> chambres = hotelService.rechercherChambres(
            request.getAdresse(),
            request.getDateArrive(),
            request.getDateDepart(),
            request.getPrixMin(),
            request.getPrixMax(),
            request.getNbrEtoile(),
            request.getNbrLits()
        );

        // Convertir les Chambres en ChambreDTO
        List<ChambreDTO> chambreDTOs = chambres.stream()
            .map(chambre -> new ChambreDTO(
                chambre.getId(),
                chambre.getNom(),
                chambre.getPrix(),
                chambre.getNbrDeLit(),
                hotelService.getHotel().getType().ordinal() + 1, // Nombre d'étoiles
                true, // Disponible si dans les résultats
                chambre.getImageUrl()
            ))
            .collect(Collectors.toList());

        return ResponseEntity.ok(chambreDTOs);
    }

    /**
     * POST /api/hotel/reservations - Créer une réservation
     */
    @PostMapping("/reservations")
    @Operation(summary = "Créer une réservation", description = "Effectue une réservation pour une chambre")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Réservation créée avec succès"),
        @ApiResponse(responseCode = "400", description = "Données de réservation invalides"),
        @ApiResponse(responseCode = "409", description = "Chambre non disponible")
    })
    public ResponseEntity<ReservationResponse> effectuerReservation(
            @RequestBody @Parameter(description = "Détails de la réservation") ReservationRequest request) {

        // Créer le client
        Client client = new Client(
            request.getNomClient(),
            request.getPrenomClient(),
            request.getNumeroCarteBancaire()
        );

        // Effectuer la réservation (utilise l'ID de la chambre)
        HotelService.ReservationResult result = hotelService.effectuerReservation(
            client,
            request.getChambreId(), // ID de la chambre (Long)
            request.getDateArrive(),
            request.getDateDepart()
        );

        if (result.isSuccess()) {
            ReservationResponse response = ReservationResponse.success(
                (long) result.getReservationId(),
                result.getMessage()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            ReservationResponse response = ReservationResponse.error(result.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
    }

    /**
     * GET /api/hotel/reservations - Lister toutes les réservations
     */
    @GetMapping("/reservations")
    @Operation(summary = "Lister les réservations", description = "Retourne toutes les réservations de l'hôtel")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès")
    })
    public ResponseEntity<List<Reservation>> getReservations() {
        List<Reservation> reservations = hotelService.getReservations();
        return ResponseEntity.ok(reservations);
    }

    /**
     * GET /api/hotel/chambres/reservees - Lister les chambres actuellement réservées
     */
    @GetMapping("/chambres/reservees")
    @Operation(summary = "Lister les chambres réservées", description = "Retourne la liste des chambres qui ont au moins une réservation")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès")
    })
    public ResponseEntity<List<ChambreDTO>> getChambresReservees() {
        List<ChambreDTO> chambresReservees = hotelService.getChambresReservees();
        return ResponseEntity.ok(chambresReservees);
    }

    /**
     * GET /api/hotel/chambres/{id} - Détails d'une chambre
     */
    @GetMapping("/chambres/{id}")
    @Operation(summary = "Obtenir les détails d'une chambre", description = "Retourne les informations détaillées d'une chambre par son ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Chambre trouvée"),
        @ApiResponse(responseCode = "404", description = "Chambre non trouvée")
    })
    public ResponseEntity<ChambreDTO> getChambreById(
            @PathVariable @Parameter(description = "ID de la chambre") Long id) {

        Hotel hotel = hotelService.getHotel();

        for (Chambre chambre : hotel.getListeDesChambres()) {
            if (chambre.getId().equals(id)) {
                ChambreDTO dto = new ChambreDTO(
                    chambre.getId(),
                    chambre.getNom(),
                    chambre.getPrix(),
                    chambre.getNbrDeLit(),
                    hotel.getType().ordinal() + 1,
                    true,
                    chambre.getImageUrl()
                );
                return ResponseEntity.ok(dto);
            }
        }

        return ResponseEntity.notFound().build();
    }
}

