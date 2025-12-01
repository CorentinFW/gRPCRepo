package org.tp1.agence.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tp1.agence.dto.ChambreDTO;
import org.tp1.agence.dto.RechercheRequest;
import org.tp1.agence.dto.ReservationRequest;
import org.tp1.agence.dto.ReservationResponse;
import org.tp1.agence.service.AgenceService;

import java.util.List;
import java.util.Map;

/**
 * Contrôleur REST pour les services de l'agence
 * Expose l'API REST de l'agence (remplacement du SOAP Endpoint)
 * L'agence agrège les résultats de plusieurs hôtels
 */
@RestController
@RequestMapping("/api/agence")
@Tag(name = "Agence", description = "API REST pour l'agence de réservation")
public class AgenceController {

    @Autowired
    private AgenceService agenceService;

    /**
     * GET /api/agence/ping - Vérifier que l'agence est opérationnelle
     */
    @GetMapping("/ping")
    @Operation(summary = "Ping", description = "Vérifier que l'agence fonctionne correctement")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Agence opérationnelle")
    })
    public ResponseEntity<Map<String, String>> ping() {
        return ResponseEntity.ok(Map.of(
            "message", "Agence REST opérationnelle",
            "status", "OK",
            "timestamp", String.valueOf(System.currentTimeMillis())
        ));
    }

    /**
     * POST /api/agence/chambres/rechercher - Rechercher des chambres dans tous les hôtels
     */
    @PostMapping("/chambres/rechercher")
    @Operation(summary = "Rechercher des chambres",
               description = "Recherche des chambres disponibles dans tous les hôtels partenaires")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Recherche effectuée avec succès"),
        @ApiResponse(responseCode = "400", description = "Paramètres de recherche invalides")
    })
    public ResponseEntity<List<ChambreDTO>> rechercherChambres(
            @RequestBody @Parameter(description = "Critères de recherche") RechercheRequest request) {

        System.out.println("🔍 Recherche de chambres avec critères: " + request);

        List<ChambreDTO> chambres = agenceService.rechercherChambres(request);

        System.out.println("✅ " + chambres.size() + " chambres trouvées dans tous les hôtels");

        return ResponseEntity.ok(chambres);
    }

    /**
     * POST /api/agence/reservations - Effectuer une réservation dans un hôtel
     */
    @PostMapping("/reservations")
    @Operation(summary = "Effectuer une réservation",
               description = "Crée une réservation dans l'hôtel sélectionné")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Réservation créée avec succès"),
        @ApiResponse(responseCode = "400", description = "Données de réservation invalides"),
        @ApiResponse(responseCode = "409", description = "Chambre non disponible")
    })
    public ResponseEntity<ReservationResponse> effectuerReservation(
            @RequestBody @Parameter(description = "Détails de la réservation") ReservationRequest request) {

        System.out.println("🏨 Tentative de réservation: chambre " + request.getChambreId() +
                         " à " + request.getHotelAdresse());

        ReservationResponse response = agenceService.effectuerReservation(request);

        if (response.isSuccess()) {
            System.out.println("✅ Réservation effectuée avec succès: " + response.getMessage());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            System.out.println("❌ Échec de la réservation: " + response.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
    }

    /**
     * GET /api/agence/reservations/{hotelNom} - Obtenir les réservations d'un hôtel spécifique
     */
    @GetMapping("/reservations/{hotelNom}")
    @Operation(summary = "Obtenir les réservations par hôtel",
               description = "Retourne toutes les réservations d'un hôtel spécifique")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Réservations récupérées"),
        @ApiResponse(responseCode = "404", description = "Hôtel non trouvé")
    })
    public ResponseEntity<Object> getReservationsParHotel(
            @PathVariable @Parameter(description = "Nom de l'hôtel") String hotelNom) {

        System.out.println("📋 Récupération des réservations pour l'hôtel: " + hotelNom);

        // TODO: Implémenter dans AgenceService
        return ResponseEntity.ok(Map.of(
            "message", "Endpoint en développement",
            "hotel", hotelNom
        ));
    }

    /**
     * GET /api/agence/chambres/reservees - Obtenir toutes les chambres réservées de tous les hôtels
     */
    @GetMapping("/chambres/reservees")
    @Operation(summary = "Liste des chambres réservées",
               description = "Retourne la liste de toutes les chambres réservées dans tous les hôtels")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès")
    })
    public ResponseEntity<Map<String, List<ChambreDTO>>> getChambresReservees() {
        System.out.println("🔍 Récupération des chambres réservées dans tous les hôtels...");

        Map<String, List<ChambreDTO>> chambresReserveesParHotel = agenceService.getChambresReservees();

        int total = chambresReserveesParHotel.values().stream()
            .mapToInt(List::size)
            .sum();

        System.out.println("✅ Total: " + total + " chambre(s) réservée(s)");

        return ResponseEntity.ok(chambresReserveesParHotel);
    }

    /**
     * GET /api/agence/hotels - Obtenir la liste des hôtels partenaires
     */
    @GetMapping("/hotels")
    @Operation(summary = "Liste des hôtels",
               description = "Retourne la liste de tous les hôtels partenaires")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès")
    })
    public ResponseEntity<Map<String, Object>> getHotels() {
        List<String> hotels = agenceService.getHotelsDisponibles();
        return ResponseEntity.ok(Map.of(
            "hotels", hotels,
            "count", hotels.size()
        ));
    }
}

