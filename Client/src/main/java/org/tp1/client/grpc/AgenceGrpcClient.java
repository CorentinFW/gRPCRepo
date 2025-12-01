package org.tp1.client.grpc;

import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import org.tp1.client.dto.ChambreDTO;
import org.tp1.client.dto.RechercheRequest;
import org.tp1.client.dto.ReservationRequest;
import org.tp1.client.dto.ReservationResponse;
import org.tp1.commun.grpc.agence.*;
import org.tp1.commun.grpc.hotel.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Client gRPC pour communiquer avec une agence
 * Remplace AgenceRestClient
 */
@Service
public class AgenceGrpcClient {

    @GrpcClient("agence1")
    private AgenceServiceGrpc.AgenceServiceBlockingStub agence1Stub;

    @GrpcClient("agence2")
    private AgenceServiceGrpc.AgenceServiceBlockingStub agence2Stub;

    /**
     * Ping une agence pour vérifier sa disponibilité
     */
    public boolean ping(String agenceName) {
        try {
            AgenceServiceGrpc.AgenceServiceBlockingStub stub = getStubForAgence(agenceName);
            if (stub == null) {
                return false;
            }

            PingRequest request = PingRequest.newBuilder().build();
            PingResponse response = stub.ping(request);

            System.out.println("✅ Ping " + agenceName + ": " + response.getMessage());
            return "OK".equals(response.getStatus());

        } catch (StatusRuntimeException e) {
            System.err.println("❌ Erreur ping " + agenceName + ": " + e.getStatus());
            return false;
        }
    }

    /**
     * Rechercher des chambres via une agence
     */
    public List<ChambreDTO> rechercherChambres(String agenceName, RechercheRequest rechercheRequest) {
        try {
            AgenceServiceGrpc.AgenceServiceBlockingStub stub = getStubForAgence(agenceName);
            if (stub == null) {
                System.err.println("❌ Stub non trouvé pour l'agence: " + agenceName);
                return new ArrayList<>();
            }

            // Convertir RechercheRequest DTO vers Proto
            org.tp1.commun.grpc.hotel.RechercheRequest protoRequest =
                    org.tp1.commun.grpc.hotel.RechercheRequest.newBuilder()
                    .setAdresse(rechercheRequest.getAdresse() != null ? rechercheRequest.getAdresse() : "")
                    .setDateArrive(rechercheRequest.getDateArrive() != null ? rechercheRequest.getDateArrive() : "")
                    .setDateDepart(rechercheRequest.getDateDepart() != null ? rechercheRequest.getDateDepart() : "")
                    .setPrixMin(rechercheRequest.getPrixMin())
                    .setPrixMax(rechercheRequest.getPrixMax())
                    .setNbrEtoile(rechercheRequest.getNbrEtoile())
                    .setNbrLits(rechercheRequest.getNbrLits())
                    .build();

            System.out.println("🔍 [Client gRPC] Recherche via " + agenceName);
            RechercheResponse response = stub.rechercherChambres(protoRequest);

            // Convertir Proto ChambreMessage vers DTO ChambreDTO
            List<ChambreDTO> chambres = response.getChambresList().stream()
                    .map(this::convertToChambreDTO)
                    .collect(Collectors.toList());

            System.out.println("✅ [Client gRPC] " + chambres.size() + " chambres trouvées via " + agenceName);
            return chambres;

        } catch (StatusRuntimeException e) {
            System.err.println("❌ [Client gRPC] Erreur lors de la recherche via " + agenceName + ": " + e.getStatus());
            return new ArrayList<>();
        } catch (Exception e) {
            System.err.println("❌ [Client gRPC] Erreur inattendue: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Effectuer une réservation via une agence
     */
    public ReservationResponse effectuerReservation(String agenceName, ReservationRequest reservationRequest) {
        try {
            AgenceServiceGrpc.AgenceServiceBlockingStub stub = getStubForAgence(agenceName);
            if (stub == null) {
                return new ReservationResponse(0, "Agence non disponible: " + agenceName, false);
            }

            // Convertir ReservationRequest DTO vers Proto
            AgenceReservationRequest protoRequest = AgenceReservationRequest.newBuilder()
                    .setHotelAdresse(reservationRequest.getHotelAdresse() != null ? reservationRequest.getHotelAdresse() : "")
                    .setChambreId(reservationRequest.getChambreId())
                    .setDateArrive(reservationRequest.getDateArrive() != null ? reservationRequest.getDateArrive() : "")
                    .setDateDepart(reservationRequest.getDateDepart() != null ? reservationRequest.getDateDepart() : "")
                    .setNomClient(reservationRequest.getClientNom() != null ? reservationRequest.getClientNom() : "")
                    .setPrenomClient(reservationRequest.getClientPrenom() != null ? reservationRequest.getClientPrenom() : "")
                    .setNumeroCarteBancaire(reservationRequest.getClientNumeroCarteBleue() != null ? reservationRequest.getClientNumeroCarteBleue() : "")
                    .build();

            System.out.println("🏨 [Client gRPC] Réservation via " + agenceName);
            org.tp1.commun.grpc.hotel.ReservationResponse protoResponse = stub.effectuerReservation(protoRequest);

            // Convertir Proto ReservationResponse vers DTO - ordre: (int id, String message, boolean success)
            ReservationResponse response = new ReservationResponse(
                    (int) protoResponse.getReservationId(),
                    protoResponse.getMessage(),
                    protoResponse.getSuccess()
            );

            if (response.isSuccess()) {
                System.out.println("✅ [Client gRPC] Réservation réussie via " + agenceName);
            } else {
                System.out.println("❌ [Client gRPC] Échec de la réservation via " + agenceName + ": " + response.getMessage());
            }

            return response;

        } catch (StatusRuntimeException e) {
            System.err.println("❌ [Client gRPC] Erreur gRPC lors de la réservation: " + e.getStatus());
            return new ReservationResponse(0, "Erreur de communication: " + e.getStatus().getDescription(), false);
        } catch (Exception e) {
            System.err.println("❌ [Client gRPC] Erreur inattendue: " + e.getMessage());
            e.printStackTrace();
            return new ReservationResponse(0, "Erreur inattendue: " + e.getMessage(), false);
        }
    }

    /**
     * Obtenir le stub approprié selon le nom de l'agence
     */
    private AgenceServiceGrpc.AgenceServiceBlockingStub getStubForAgence(String agenceName) {
        if (agenceName == null) {
            return null;
        }

        if (agenceName.toLowerCase().contains("1") || agenceName.toLowerCase().contains("paris")) {
            return agence1Stub;
        } else if (agenceName.toLowerCase().contains("2") || agenceName.toLowerCase().contains("sud")) {
            return agence2Stub;
        }

        return null;
    }

    /**
     * Convertir un ChambreMessage (Proto) vers ChambreDTO
     */
    private ChambreDTO convertToChambreDTO(ChambreMessage chambre) {
        ChambreDTO dto = new ChambreDTO();
        dto.setId((int) chambre.getId());
        dto.setNom(chambre.getNom());
        dto.setPrix(chambre.getPrix());
        dto.setNbrLits(chambre.getNbrDeLit());
        dto.setImageUrl(chambre.getImageUrl());
        dto.setHotelNom(chambre.getHotelNom());
        dto.setHotelAdresse(chambre.getHotelAdresse());
        return dto;
    }

    /**
     * Obtenir les chambres réservées via une agence
     */
    public java.util.Map<String, List<ChambreDTO>> getChambresReservees(String agenceName) {
        try {
            AgenceServiceGrpc.AgenceServiceBlockingStub stub = getStubForAgence(agenceName);
            if (stub == null) {
                System.err.println("❌ Stub non trouvé pour l'agence: " + agenceName);
                return new java.util.HashMap<>();
            }

            System.out.println("📋 [Client gRPC] Récupération des chambres réservées via " + agenceName);

            org.tp1.commun.grpc.agence.ChambresReserveesRequest request =
                    org.tp1.commun.grpc.agence.ChambresReserveesRequest.newBuilder().build();

            org.tp1.commun.grpc.agence.ChambresReserveesParHotelResponse response =
                    stub.getChambresReservees(request);

            // Convertir la réponse en Map<String, List<ChambreDTO>>
            java.util.Map<String, List<ChambreDTO>> chambresParHotel = new java.util.HashMap<>();

            for (org.tp1.commun.grpc.agence.HotelChambresReservees hotelChambres : response.getHotelsList()) {
                String hotelNom = hotelChambres.getHotelNom();
                List<ChambreDTO> chambres = hotelChambres.getChambresList().stream()
                        .map(this::convertToChambreDTO)
                        .collect(java.util.stream.Collectors.toList());

                chambresParHotel.put(hotelNom, chambres);
            }

            System.out.println("✅ [Client gRPC] Chambres réservées récupérées pour " + chambresParHotel.size() + " hôtel(s)");
            return chambresParHotel;

        } catch (StatusRuntimeException e) {
            System.err.println("❌ [Client gRPC] Erreur lors de la récupération des chambres réservées: " + e.getStatus());
            return new java.util.HashMap<>();
        } catch (Exception e) {
            System.err.println("❌ [Client gRPC] Erreur inattendue: " + e.getMessage());
            e.printStackTrace();
            return new java.util.HashMap<>();
        }
    }
}
