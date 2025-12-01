package org.tp1.hotellerie.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_reservation", nullable = false)
    private int numeroReservation;

    @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "chambre_id", nullable = false)
    private Chambre chambre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    @JsonIgnore
    private Hotel hotel;

    @Temporal(TemporalType.DATE)
    @Column(name = "date_arrive", nullable = false)
    private Date dateArrive;

    @Temporal(TemporalType.DATE)
    @Column(name = "date_depart", nullable = false)
    private Date dateDepart;

    // Constructeur par défaut requis par JPA
    public Reservation() {
    }

    public Reservation(int numeroReservation, Client client, Chambre chambre, Date dateArrive, Date dateDepart) {
        this.numeroReservation = numeroReservation;
        this.client = client;
        this.chambre = chambre;
        this.dateArrive = dateArrive;
        this.dateDepart = dateDepart;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getNumeroReservation() {
        return numeroReservation;
    }

    public void setNumeroReservation(int numeroReservation) {
        this.numeroReservation = numeroReservation;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Chambre getChambre() {
        return chambre;
    }

    public void setChambre(Chambre chambre) {
        this.chambre = chambre;
    }

    public Hotel getHotel() {
        return hotel;
    }

    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
    }

    public Date getDateArrive() {
        return dateArrive;
    }

    public void setDateArrive(Date dateArrive) {
        this.dateArrive = dateArrive;
    }

    public Date getDateDepart() {
        return dateDepart;
    }

    public void setDateDepart(Date dateDepart) {
        this.dateDepart = dateDepart;
    }
}
