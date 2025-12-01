package org.tp1.hotellerie.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.tp1.hotellerie.model.Client;

import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    /**
     * Recherche un client par son nom et prénom
     */
    Optional<Client> findByNomAndPrenom(String nom, String prenom);

    /**
     * Recherche un client par son numéro de carte bleue
     */
    Optional<Client> findByNumeroCarteBleue(String numeroCarteBleue);

    /**
     * Compte le nombre de réservations d'un client
     */
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.client.id = :clientId")
    long countReservationsForClient(@Param("clientId") Long clientId);
}

