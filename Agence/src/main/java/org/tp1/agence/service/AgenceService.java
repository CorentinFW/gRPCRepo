package org.tp1.agence.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.tp1.agence.dto.ChambreDTO;
import org.tp1.agence.dto.RechercheRequest;
import org.tp1.agence.dto.ReservationRequest;
import org.tp1.agence.dto.ReservationResponse;
import org.tp1.agence.grpc.client.HotelGrpcClient;
import org.tp1.commun.grpc.hotel.ChambreMessage;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service de l'agence qui orchestre les appels gRPC aux hôtels
 */
@Service
public class AgenceService {

    @Autowired
    private HotelGrpcClient hotelGrpcClient;

    @Value("${agence.nom:Agence Inconnue}")
    private String agenceNom;

    @Value("${agence.coefficient:1.0}")
    private float coefficient;

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

        // Créer la requête gRPC
        org.tp1.commun.grpc.hotel.RechercheRequest grpcRequest =
                org.tp1.commun.grpc.hotel.RechercheRequest.newBuilder()
                .setAdresse(request.getAdresse() != null ? request.getAdresse() : "")
                .setDateArrive(request.getDateArrive())
                .setDateDepart(request.getDateDepart())
                .setPrixMin(request.getPrixMin())
                .setPrixMax(request.getPrixMax())
                .setNbrEtoile(request.getNbrEtoile())
                .setNbrLits(request.getNbrLits())
                .build();

        // Interroger tous les hôtels disponibles
        List<String> hotels = getHotelsDisponibles();
        List<ChambreDTO> toutesChambres = new ArrayList<>();

        for (String hotel : hotels) {
            List<ChambreMessage> chambres = hotelGrpcClient.rechercherChambres(hotel, grpcRequest);

            // Convertir les messages gRPC en DTO avec coefficient
            List<ChambreDTO> chambresDTO = chambres.stream()
                    .map(this::convertirChambreMessage)
                    .collect(Collectors.toList());

            toutesChambres.addAll(chambresDTO);
        }

        return toutesChambres;
    }

    /**
     * Convertir un ChambreMessage gRPC en ChambreDTO avec coefficient
     */
    private ChambreDTO convertirChambreMessage(ChambreMessage message) {
        ChambreDTO dto = new ChambreDTO();
        dto.setId((int) message.getId());
        dto.setNom(message.getNom());
        dto.setPrix(message.getPrix() * coefficient);  // Appliquer le coefficient
        dto.setNbrLits(message.getNbrDeLit());
        dto.setHotelNom(message.getHotelNom());
        dto.setHotelAdresse(message.getHotelAdresse());
        dto.setImageUrl(message.getImageUrl());
        dto.setAgenceNom(agenceNom);
        return dto;
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
            // Créer la requête gRPC
            org.tp1.commun.grpc.hotel.ReservationRequest grpcRequest =
                    org.tp1.commun.grpc.hotel.ReservationRequest.newBuilder()
                    .setChambreId(request.getChambreId())
                    .setNomClient(request.getClientNom())
                    .setPrenomClient(request.getClientPrenom())
                    .setNumeroCarteBancaire(request.getClientNumeroCarteBleue() != null ? request.getClientNumeroCarteBleue() : "")
                    .setDateArrive(request.getDateArrive())
                    .setDateDepart(request.getDateDepart())
                    .build();

            // Déterminer le nom de l'hôtel à partir de l'adresse
            String hotelNom = determinerHotelParAdresse(request.getHotelAdresse());

            // Effectuer la réservation via gRPC
            org.tp1.commun.grpc.hotel.ReservationResponse grpcResponse =
                    hotelGrpcClient.effectuerReservation(hotelNom, grpcRequest);

            // Convertir la réponse gRPC en DTO
            ReservationResponse response = new ReservationResponse();
            response.setSuccess(grpcResponse.getSuccess());
            response.setMessage(grpcResponse.getMessage());
            response.setReservationId((int) grpcResponse.getReservationId());
            return response;

        } catch (Exception e) {
            return ReservationResponse.error("Erreur lors de la réservation: " + e.getMessage());
        }
    }

    /**
     * Obtenir la liste des hôtels disponibles
     */
    public List<String> getHotelsDisponibles() {
        // Retourner les hôtels configurés pour cette agence
        List<String> hotels = new ArrayList<>();

        // Vérifier quels stubs sont disponibles en fonction du profil de l'agence
        if (agenceNom.toLowerCase().contains("paris")) {
            hotels.add("Paris");
            hotels.add("Lyon");
        } else if (agenceNom.toLowerCase().contains("sud")) {
            hotels.add("Lyon");
            hotels.add("Montpellier");
        } else {
            // Par défaut, tous les hôtels
            hotels.add("Paris");
            hotels.add("Lyon");
            hotels.add("Montpellier");
        }

        return hotels;
    }

    /**
     * Obtenir toutes les chambres réservées de tous les hôtels
     */
    public Map<String, List<ChambreDTO>> getChambresReservees() {
        Map<String, List<ChambreDTO>> chambresParHotel = new HashMap<>();
        List<String> hotels = getHotelsDisponibles();

        for (String hotel : hotels) {
            List<ChambreMessage> chambres = hotelGrpcClient.getChambresReservees(hotel);

            // Convertir les messages gRPC en DTO
            List<ChambreDTO> chambresDTO = chambres.stream()
                    .map(this::convertirChambreMessage)
                    .collect(Collectors.toList());

            chambresParHotel.put(hotel, chambresDTO);
        }

        return chambresParHotel;
    }

    /**
     * Déterminer le nom de l'hôtel à partir de son adresse
     */
    private String determinerHotelParAdresse(String adresse) {
        if (adresse == null) {
            return "Paris"; // Par défaut
        }

        String lowerAdresse = adresse.toLowerCase();
        if (lowerAdresse.contains("paris")) {
            return "Paris";
        } else if (lowerAdresse.contains("lyon")) {
            return "Lyon";
        } else if (lowerAdresse.contains("montpellier")) {
            return "Montpellier";
        }

        return "Paris"; // Par défaut
    }
}

