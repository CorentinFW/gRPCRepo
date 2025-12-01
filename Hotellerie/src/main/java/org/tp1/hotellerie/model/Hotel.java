package org.tp1.hotellerie.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "hotels")
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String adresse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Reservation> listeReservation = new ArrayList<>();

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Chambre> listeDesChambres = new ArrayList<>();

    // Constructeur par défaut requis par JPA
    public Hotel() {
    }

    // Constructeur pratique
    public Hotel(String nom, String adresse, Type type) {
        this.nom = nom;
        this.adresse = adresse;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public List<Reservation> getListeReservation() {
        return listeReservation;
    }

    public void setListeReservation(List<Reservation> listeReservation) {
        this.listeReservation = listeReservation;
    }

    public int getNombreReservations() {
        return listeReservation.size();
    }

    public int getNombreChambres() {
        return listeDesChambres.size();
    }

    public List<Chambre> getListeDesChambres() {
        return listeDesChambres;
    }

    public void setListeDesChambres(List<Chambre> listeDesChambres) {
        this.listeDesChambres = listeDesChambres;
    }

    public void ajoutReservation(Reservation reservation) {
        if (reservation != null) {
            this.listeReservation.add(reservation);
            reservation.setHotel(this);
        }
    }

    public void ajoutChambre(Chambre chambre) {
        if (chambre != null) {
            this.listeDesChambres.add(chambre);
            chambre.setHotel(this);
        }
    }

}
