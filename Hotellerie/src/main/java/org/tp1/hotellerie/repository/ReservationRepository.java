package org.tp1.hotellerie.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.tp1.hotellerie.model.Reservation;

import java.util.Date;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /**
     * Recherche toutes les réservations d'un hôtel
     */
    @Query("SELECT r FROM Reservation r WHERE r.hotel.id = :hotelId")
    List<Reservation> findByHotelId(@Param("hotelId") Long hotelId);

    /**
     * Recherche toutes les réservations d'une chambre spécifique
     */
    @Query("SELECT r FROM Reservation r WHERE r.chambre.id = :chambreId")
    List<Reservation> findByChambreId(@Param("chambreId") Long chambreId);

    /**
     * Recherche les réservations qui se chevauchent avec les dates données
     */
    @Query("SELECT r FROM Reservation r WHERE r.chambre.id = :chambreId " +
           "AND ((r.dateArrive <= :dateDepart AND r.dateDepart >= :dateArrive))")
    List<Reservation> findOverlappingReservations(
        @Param("chambreId") Long chambreId,
        @Param("dateArrive") Date dateArrive,
        @Param("dateDepart") Date dateDepart
    );

    /**
     * Recherche les réservations d'un client
     */
    @Query("SELECT r FROM Reservation r WHERE r.client.id = :clientId")
    List<Reservation> findByClientId(@Param("clientId") Long clientId);

    /**
     * Recherche les réservations d'un hôtel pour une période donnée
     */
    @Query("SELECT r FROM Reservation r WHERE r.hotel.id = :hotelId " +
           "AND r.dateArrive >= :dateDebut AND r.dateDepart <= :dateFin")
    List<Reservation> findByHotelIdAndDateRange(
        @Param("hotelId") Long hotelId,
        @Param("dateDebut") Date dateDebut,
        @Param("dateFin") Date dateFin
    );

    /**
     * Compte le nombre de réservations d'un hôtel
     */
    long countByHotelId(Long hotelId);

    /**
     * Compte le nombre de réservations d'une chambre
     */
    long countByChambreId(Long chambreId);
}

