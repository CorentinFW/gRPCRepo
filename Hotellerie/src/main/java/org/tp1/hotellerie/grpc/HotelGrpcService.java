package org.tp1.hotellerie.grpc;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.tp1.commun.grpc.hotel.*;
import org.tp1.hotellerie.model.Chambre;
import org.tp1.hotellerie.model.Client;
import org.tp1.hotellerie.model.Hotel;
import org.tp1.hotellerie.service.HotelService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implémentation du service gRPC pour l'hôtel
 * Remplace le contrôleur REST HotelController
 */
@GrpcService
public class HotelGrpcService extends HotelServiceGrpc.HotelServiceImplBase {

    @Autowired
    private HotelService hotelService;

    /**
     * Obtenir les informations de l'hôtel
     */
    @Override
    public void getHotelInfo(HotelInfoRequest request, StreamObserver<HotelInfoResponse> responseObserver) {
        try {
            Hotel hotel = hotelService.getHotel();

            HotelInfoResponse response = HotelInfoResponse.newBuilder()
                    .setNom(hotel.getNom())
                    .setAdresse(hotel.getAdresse())
                    .setVille(hotel.getVille() != null ? hotel.getVille() : "")
                    .setTelephone(hotel.getTelephone() != null ? hotel.getTelephone() : "")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Erreur lors de la récupération des informations de l'hôtel: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    /**
     * Rechercher des chambres disponibles
     */
    @Override
    public void rechercherChambres(org.tp1.commun.grpc.hotel.RechercheRequest request,
                                   StreamObserver<RechercheResponse> responseObserver) {
        try {
            System.out.println("🔍 [gRPC] Recherche de chambres - critères: " + request);

            // Appeler le service métier existant
            List<Chambre> chambres = hotelService.rechercherChambres(
                    request.getAdresse().isEmpty() ? null : request.getAdresse(),
                    request.getDateArrive().isEmpty() ? null : request.getDateArrive(),
                    request.getDateDepart().isEmpty() ? null : request.getDateDepart(),
                    request.getPrixMin() > 0 ? request.getPrixMin() : null,
                    request.getPrixMax() > 0 ? request.getPrixMax() : null,
                    request.getNbrEtoile() > 0 ? request.getNbrEtoile() : null,
                    request.getNbrLits() > 0 ? request.getNbrLits() : null
            );

            // Récupérer les informations de l'hôtel
            Hotel hotel = hotelService.getHotel();

            // Convertir les chambres en messages gRPC
            List<ChambreMessage> chambreMessages = chambres.stream()
                    .map(chambre -> ChambreMessage.newBuilder()
                            .setId(chambre.getId())
                            .setNom(chambre.getNom())
                            .setPrix(chambre.getPrix())
                            .setNbrDeLit(chambre.getNbrDeLit())
                            .setNbrEtoile(hotel.getType().ordinal() + 1)
                            .setDisponible(true)
                            .setImageUrl(chambre.getImageUrl() != null ? chambre.getImageUrl() : "")
                            .setHotelNom(hotel.getNom())
                            .setHotelAdresse(hotel.getAdresse())
                            .build())
                    .collect(Collectors.toList());

            RechercheResponse response = RechercheResponse.newBuilder()
                    .addAllChambres(chambreMessages)
                    .build();

            System.out.println("✅ [gRPC] " + chambreMessages.size() + " chambres trouvées");

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            System.err.println("❌ [gRPC] Erreur lors de la recherche: " + e.getMessage());
            e.printStackTrace();
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Erreur lors de la recherche de chambres: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    /**
     * Effectuer une réservation
     */
    @Override
    public void effectuerReservation(org.tp1.commun.grpc.hotel.ReservationRequest request,
                                     StreamObserver<org.tp1.commun.grpc.hotel.ReservationResponse> responseObserver) {
        try {
            System.out.println("🏨 [gRPC] Tentative de réservation - chambre: " + request.getChambreId());

            // Créer le client
            Client client = new Client(
                    request.getNomClient(),
                    request.getPrenomClient(),
                    request.getNumeroCarteBancaire()
            );

            // Effectuer la réservation
            HotelService.ReservationResult result = hotelService.effectuerReservation(
                    client,
                    request.getChambreId(),
                    request.getDateArrive(),
                    request.getDateDepart()
            );

            // Construire la réponse gRPC
            org.tp1.commun.grpc.hotel.ReservationResponse response =
                    org.tp1.commun.grpc.hotel.ReservationResponse.newBuilder()
                    .setSuccess(result.isSuccess())
                    .setMessage(result.getMessage())
                    .setReservationId(result.getReservationId())
                    .build();

            if (result.isSuccess()) {
                System.out.println("✅ [gRPC] Réservation effectuée avec succès: " + result.getMessage());
            } else {
                System.out.println("❌ [gRPC] Échec de la réservation: " + result.getMessage());
            }

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            System.err.println("❌ [gRPC] Erreur lors de la réservation: " + e.getMessage());
            e.printStackTrace();
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Erreur lors de la réservation: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    /**
     * Obtenir les chambres réservées
     */
    @Override
    public void getChambresReservees(ChambresReserveesRequest request,
                                     StreamObserver<ChambresReserveesResponse> responseObserver) {
        try {
            System.out.println("📋 [gRPC] Récupération des chambres réservées");

            // Récupérer les chambres réservées via le service métier
            List<org.tp1.hotellerie.dto.ChambreDTO> chambresReservees = hotelService.getChambresReservees();

            // Récupérer les informations de l'hôtel
            Hotel hotel = hotelService.getHotel();

            // Convertir en messages gRPC
            List<ChambreMessage> chambreMessages = chambresReservees.stream()
                    .map(chambre -> ChambreMessage.newBuilder()
                            .setId(chambre.getId())
                            .setNom(chambre.getNom())
                            .setPrix(chambre.getPrix())
                            .setNbrDeLit(chambre.getNbrLits())
                            .setNbrEtoile(chambre.getNbrEtoiles())
                            .setDisponible(false)  // Ces chambres sont réservées
                            .setImageUrl(chambre.getImage() != null ? chambre.getImage() : "")
                            .setHotelNom(hotel.getNom())
                            .setHotelAdresse(hotel.getAdresse())
                            .build())
                    .collect(Collectors.toList());

            ChambresReserveesResponse response = ChambresReserveesResponse.newBuilder()
                    .addAllChambres(chambreMessages)
                    .build();

            System.out.println("✅ [gRPC] " + chambreMessages.size() + " chambres réservées retournées");

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            System.err.println("❌ [gRPC] Erreur lors de la récupération des chambres réservées: " + e.getMessage());
            e.printStackTrace();
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Erreur lors de la récupération des chambres réservées: " + e.getMessage())
                    .asRuntimeException());
        }
    }
}
