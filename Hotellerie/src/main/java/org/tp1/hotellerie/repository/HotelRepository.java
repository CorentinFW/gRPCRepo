package org.tp1.hotellerie.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.tp1.hotellerie.model.Hotel;

import java.util.Optional;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {
    
    /**
     * Recherche un hôtel par son nom
     */
    Optional<Hotel> findByNom(String nom);
    
    /**
     * Recherche un hôtel par son adresse
     */
    Optional<Hotel> findByAdresse(String adresse);
    
    /**
     * Recherche un hôtel par son nom et son adresse
     */
    Optional<Hotel> findByNomAndAdresse(String nom, String adresse);
    
    /**
     * Vérifie si un hôtel existe avec ce nom
     */
    boolean existsByNom(String nom);
    
    /**
     * Compte le nombre total de chambres d'un hôtel
     */
    @Query("SELECT COUNT(c) FROM Chambre c WHERE c.hotel.id = :hotelId")
    long countChambresInHotel(Long hotelId);
    
    /**
     * Compte le nombre total de réservations d'un hôtel
     */
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.hotel.id = :hotelId")
    long countReservationsInHotel(Long hotelId);
}

