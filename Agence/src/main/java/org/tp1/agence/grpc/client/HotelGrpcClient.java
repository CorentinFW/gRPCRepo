package org.tp1.agence.grpc.client;

import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import org.tp1.commun.grpc.hotel.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Client gRPC pour communiquer avec un hôtel
 * Remplace HotelRestClient
 */
@Service
public class HotelGrpcClient {

    @GrpcClient("hotel-paris")
    private HotelServiceGrpc.HotelServiceBlockingStub hotelParisStub;

    @GrpcClient("hotel-lyon")
    private HotelServiceGrpc.HotelServiceBlockingStub hotelLyonStub;

    @GrpcClient("hotel-montpellier")
    private HotelServiceGrpc.HotelServiceBlockingStub hotelMontpellierStub;

    /**
     * Rechercher des chambres dans un hôtel spécifique via gRPC
     */
    public List<ChambreMessage> rechercherChambres(String hotelName, RechercheRequest request) {
        try {
            HotelServiceGrpc.HotelServiceBlockingStub stub = getStubForHotel(hotelName);
            if (stub == null) {
                System.err.println("❌ Stub non trouvé pour l'hôtel: " + hotelName);
                return new ArrayList<>();
            }

            System.out.println("🔍 [gRPC Client] Recherche dans l'hôtel: " + hotelName);
            RechercheResponse response = stub.rechercherChambres(request);
            System.out.println("✅ [gRPC Client] " + response.getChambresCount() + " chambres trouvées dans " + hotelName);

            return response.getChambresList();

        } catch (StatusRuntimeException e) {
            System.err.println("❌ [gRPC Client] Erreur lors de la recherche dans " + hotelName + ": " + e.getStatus());
            return new ArrayList<>();
        } catch (Exception e) {
            System.err.println("❌ [gRPC Client] Erreur inattendue lors de la recherche dans " + hotelName + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Effectuer une réservation dans un hôtel spécifique via gRPC
     */
    public org.tp1.commun.grpc.hotel.ReservationResponse effectuerReservation(
            String hotelName,
            org.tp1.commun.grpc.hotel.ReservationRequest request) {
        try {
            HotelServiceGrpc.HotelServiceBlockingStub stub = getStubForHotel(hotelName);
            if (stub == null) {
                return org.tp1.commun.grpc.hotel.ReservationResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage("Hôtel non trouvé: " + hotelName)
                        .build();
            }

            System.out.println("🏨 [gRPC Client] Réservation dans l'hôtel: " + hotelName);
            org.tp1.commun.grpc.hotel.ReservationResponse response = stub.effectuerReservation(request);

            if (response.getSuccess()) {
                System.out.println("✅ [gRPC Client] Réservation réussie dans " + hotelName);
            } else {
                System.out.println("❌ [gRPC Client] Échec de la réservation dans " + hotelName + ": " + response.getMessage());
            }

            return response;

        } catch (StatusRuntimeException e) {
            System.err.println("❌ [gRPC Client] Erreur gRPC lors de la réservation dans " + hotelName + ": " + e.getStatus());
            return org.tp1.commun.grpc.hotel.ReservationResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Erreur de communication avec l'hôtel: " + e.getStatus().getDescription())
                    .build();
        } catch (Exception e) {
            System.err.println("❌ [gRPC Client] Erreur inattendue lors de la réservation: " + e.getMessage());
            return org.tp1.commun.grpc.hotel.ReservationResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Erreur inattendue: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Obtenir le stub gRPC approprié selon le nom de l'hôtel
     */
    private HotelServiceGrpc.HotelServiceBlockingStub getStubForHotel(String hotelName) {
        if (hotelName == null) {
            return null;
        }

        String lowerName = hotelName.toLowerCase();
        if (lowerName.contains("paris")) {
            return hotelParisStub;
        } else if (lowerName.contains("lyon")) {
            return hotelLyonStub;
        } else if (lowerName.contains("montpellier")) {
            return hotelMontpellierStub;
        }

        return null;
    }

    /**
     * Obtenir le stub selon l'adresse de l'hôtel
     */
    public HotelServiceGrpc.HotelServiceBlockingStub getStubByAddress(String address) {
        if (address == null) {
            return null;
        }

        String lowerAddress = address.toLowerCase();
        if (lowerAddress.contains("paris")) {
            return hotelParisStub;
        } else if (lowerAddress.contains("lyon")) {
            return hotelLyonStub;
        } else if (lowerAddress.contains("montpellier")) {
            return hotelMontpellierStub;
        }

        return null;
    }

    /**
     * Obtenir les chambres réservées d'un hôtel via gRPC
     */
    public List<ChambreMessage> getChambresReservees(String hotelName) {
        try {
            HotelServiceGrpc.HotelServiceBlockingStub stub = getStubForHotel(hotelName);
            if (stub == null) {
                System.err.println("❌ Stub non trouvé pour l'hôtel: " + hotelName);
                return new ArrayList<>();
            }

            System.out.println("📋 [gRPC Client] Récupération des chambres réservées de: " + hotelName);

            ChambresReserveesRequest request = ChambresReserveesRequest.newBuilder().build();
            ChambresReserveesResponse response = stub.getChambresReservees(request);

            System.out.println("✅ [gRPC Client] " + response.getChambresCount() + " chambres réservées dans " + hotelName);

            return response.getChambresList();

        } catch (StatusRuntimeException e) {
            System.err.println("❌ [gRPC Client] Erreur lors de la récupération des chambres réservées dans " + hotelName + ": " + e.getStatus());
            return new ArrayList<>();
        } catch (Exception e) {
            System.err.println("❌ [gRPC Client] Erreur inattendue: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
