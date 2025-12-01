package org.tp1.hotellerie.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.tp1.hotellerie.model.Chambre;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChambreRepository extends JpaRepository<Chambre, Long> {

    /**
     * Recherche toutes les chambres d'un hôtel
     */
    @Query("SELECT c FROM Chambre c WHERE c.hotel.id = :hotelId")
    List<Chambre> findByHotelId(@Param("hotelId") Long hotelId);

    /**
     * Recherche une chambre par son numéro dans un hôtel spécifique
     */
    @Query("SELECT c FROM Chambre c WHERE c.numeroChambre = :numeroChambre AND c.hotel.id = :hotelId")
    Optional<Chambre> findByNumeroChambreAndHotelId(@Param("numeroChambre") int numeroChambre, @Param("hotelId") Long hotelId);

    /**
     * Recherche les chambres avec un nombre de lits minimum
     */
    @Query("SELECT c FROM Chambre c WHERE c.hotel.id = :hotelId AND c.nbrDeLit >= :nbrLitsMin")
    List<Chambre> findByHotelIdAndNbrDeLitGreaterThanEqual(@Param("hotelId") Long hotelId, @Param("nbrLitsMin") int nbrLitsMin);

    /**
     * Recherche les chambres dans une fourchette de prix
     */
    @Query("SELECT c FROM Chambre c WHERE c.hotel.id = :hotelId AND c.prix BETWEEN :prixMin AND :prixMax")
    List<Chambre> findByHotelIdAndPrixBetween(@Param("hotelId") Long hotelId, @Param("prixMin") float prixMin, @Param("prixMax") float prixMax);

    /**
     * Compte le nombre de chambres d'un hôtel
     */
    long countByHotelId(Long hotelId);
}

