package org.tp1.hotellerie.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

@Entity
@Table(name = "chambres")
public class Chambre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_chambre", nullable = false)
    private int numeroChambre;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private float prix;

    @Column(name = "nbr_de_lit", nullable = false)
    private int nbrDeLit;

    @Column(name = "image_url")
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    @JsonIgnore
    private Hotel hotel;

    // Constructeur par défaut requis par JPA
    public Chambre() {
    }

    public Chambre(int numeroChambre, String nom, float prix, int nbrDeLit) {
        this.numeroChambre = numeroChambre;
        this.nom = nom;
        this.prix = prix;
        this.nbrDeLit = nbrDeLit;
    }

    public Chambre(int numeroChambre, String nom, float prix, int nbrDeLit, String imageUrl) {
        this.numeroChambre = numeroChambre;
        this.nom = nom;
        this.prix = prix;
        this.nbrDeLit = nbrDeLit;
        this.imageUrl = imageUrl;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getNumeroChambre() {
        return numeroChambre;
    }

    public void setNumeroChambre(int numeroChambre) {
        this.numeroChambre = numeroChambre;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public float getPrix() {
        return prix;
    }

    public void setPrix(float prix) {
        this.prix = prix;
    }

    public int getNbrDeLit() {
        return nbrDeLit;
    }

    public void setNbrDeLit(int nbrDeLit) {
        this.nbrDeLit = nbrDeLit;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Hotel getHotel() {
        return hotel;
    }

    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
    }
}
