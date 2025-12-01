package org.tp1.agence.grpc.server;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.tp1.agence.grpc.client.HotelGrpcClient;
import org.tp1.commun.grpc.agence.*;
import org.tp1.commun.grpc.hotel.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Implémentation du service gRPC pour l'agence
 * L'agence agrège les résultats de plusieurs hôtels
 */
@GrpcService
public class AgenceGrpcService extends AgenceServiceGrpc.AgenceServiceImplBase {

    @Autowired
    private HotelGrpcClient hotelGrpcClient;

    @Value("${agence.nom:Agence}")
    private String agenceName;

    @Value("${grpc.client.hotel-paris.address:}")
    private String hotelParisAddress;

    @Value("${grpc.client.hotel-lyon.address:}")
    private String hotelLyonAddress;

    @Value("${grpc.client.hotel-montpellier.address:}")
    private String hotelMontpellierAddress;

    /**
     * Ping pour vérifier que l'agence est opérationnelle
     */
    @Override
    public void ping(PingRequest request, StreamObserver<PingResponse> responseObserver) {
        try {
            PingResponse response = PingResponse.newBuilder()
                    .setMessage("Agence gRPC opérationnelle: " + agenceName)
                    .setStatus("OK")
                    .setTimestamp(System.currentTimeMillis())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Erreur lors du ping: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    /**
     * Rechercher des chambres dans tous les hôtels partenaires
     */
    @Override
    public void rechercherChambres(RechercheRequest request,
                                   StreamObserver<RechercheResponse> responseObserver) {
        try {
            System.out.println("🔍 [Agence gRPC] Recherche de chambres - critères: " + request);

            List<ChambreMessage> toutesLesChambres = new ArrayList<>();

            // Rechercher dans tous les hôtels disponibles
            List<String> hotels = getAvailableHotels();

            for (String hotelName : hotels) {
                List<ChambreMessage> chambres = hotelGrpcClient.rechercherChambres(hotelName, request);
                toutesLesChambres.addAll(chambres);
            }

            RechercheResponse response = RechercheResponse.newBuilder()
                    .addAllChambres(toutesLesChambres)
                    .build();

            System.out.println("✅ [Agence gRPC] Total: " + toutesLesChambres.size() + " chambres trouvées");

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            System.err.println("❌ [Agence gRPC] Erreur lors de la recherche: " + e.getMessage());
            e.printStackTrace();
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Erreur lors de la recherche de chambres: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    /**
     * Effectuer une réservation dans un hôtel spécifique
     */
    @Override
    public void effectuerReservation(AgenceReservationRequest request,
                                     StreamObserver<org.tp1.commun.grpc.hotel.ReservationResponse> responseObserver) {
        try {
            System.out.println("🏨 [Agence gRPC] Tentative de réservation - chambre: " +
                             request.getChambreId() + " à " + request.getHotelAdresse());

            // Construire la requête pour l'hôtel
            org.tp1.commun.grpc.hotel.ReservationRequest hotelRequest =
                    org.tp1.commun.grpc.hotel.ReservationRequest.newBuilder()
                    .setChambreId(request.getChambreId())
                    .setDateArrive(request.getDateArrive())
                    .setDateDepart(request.getDateDepart())
                    .setNomClient(request.getNomClient())
                    .setPrenomClient(request.getPrenomClient())
                    .setNumeroCarteBancaire(request.getNumeroCarteBancaire())
                    .build();

            // Déterminer l'hôtel cible selon l'adresse
            String hotelName = getHotelNameFromAddress(request.getHotelAdresse());

            if (hotelName == null) {
                org.tp1.commun.grpc.hotel.ReservationResponse errorResponse =
                        org.tp1.commun.grpc.hotel.ReservationResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage("Hôtel non trouvé pour l'adresse: " + request.getHotelAdresse())
                        .build();

                responseObserver.onNext(errorResponse);
                responseObserver.onCompleted();
                return;
            }

            // Effectuer la réservation via le client gRPC
            org.tp1.commun.grpc.hotel.ReservationResponse response =
                    hotelGrpcClient.effectuerReservation(hotelName, hotelRequest);

            if (response.getSuccess()) {
                System.out.println("✅ [Agence gRPC] Réservation effectuée avec succès");
            } else {
                System.out.println("❌ [Agence gRPC] Échec de la réservation: " + response.getMessage());
            }

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            System.err.println("❌ [Agence gRPC] Erreur lors de la réservation: " + e.getMessage());
            e.printStackTrace();
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Erreur lors de la réservation: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    /**
     * Obtenir la liste des hôtels disponibles pour cette agence
     */
    private List<String> getAvailableHotels() {
        List<String> hotels = new ArrayList<>();

        if (hotelParisAddress != null && !hotelParisAddress.isEmpty()) {
            hotels.add("paris");
        }
        if (hotelLyonAddress != null && !hotelLyonAddress.isEmpty()) {
            hotels.add("lyon");
        }
        if (hotelMontpellierAddress != null && !hotelMontpellierAddress.isEmpty()) {
            hotels.add("montpellier");
        }

        return hotels;
    }

    /**
     * Déterminer le nom de l'hôtel à partir de l'adresse
     */
    private String getHotelNameFromAddress(String address) {
        if (address == null) {
            return null;
        }

        String lowerAddress = address.toLowerCase();
        if (lowerAddress.contains("paris")) {
            return "paris";
        } else if (lowerAddress.contains("lyon")) {
            return "lyon";
        } else if (lowerAddress.contains("montpellier")) {
            return "montpellier";
        }

        return null;
    }

    /**
     * Obtenir les chambres réservées de tous les hôtels
     */
    @Override
    public void getChambresReservees(org.tp1.commun.grpc.agence.ChambresReserveesRequest request,
                                     StreamObserver<ChambresReserveesParHotelResponse> responseObserver) {
        try {
            System.out.println("📋 [Agence gRPC] Récupération des chambres réservées de tous les hôtels");

            List<HotelChambresReservees> hotelsList = new ArrayList<>();

            // Récupérer les chambres réservées de chaque hôtel disponible
            List<String> hotels = getAvailableHotels();

            for (String hotelName : hotels) {
                try {
                    List<ChambreMessage> chambres = hotelGrpcClient.getChambresReservees(hotelName);

                    if (!chambres.isEmpty()) {
                        // Obtenir le nom de l'hôtel depuis la première chambre
                        String hotelNom = chambres.get(0).getHotelNom();

                        HotelChambresReservees hotelChambres = HotelChambresReservees.newBuilder()
                                .setHotelNom(hotelNom)
                                .addAllChambres(chambres)
                                .build();

                        hotelsList.add(hotelChambres);
                    }
                } catch (Exception e) {
                    System.err.println("❌ [Agence gRPC] Erreur pour l'hôtel " + hotelName + ": " + e.getMessage());
                }
            }

            ChambresReserveesParHotelResponse response = ChambresReserveesParHotelResponse.newBuilder()
                    .addAllHotels(hotelsList)
                    .build();

            System.out.println("✅ [Agence gRPC] Chambres réservées récupérées pour " + hotelsList.size() + " hôtel(s)");

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            System.err.println("❌ [Agence gRPC] Erreur lors de la récupération des chambres réservées: " + e.getMessage());
            e.printStackTrace();
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Erreur lors de la récupération des chambres réservées: " + e.getMessage())
                    .asRuntimeException());
        }
    }
}
