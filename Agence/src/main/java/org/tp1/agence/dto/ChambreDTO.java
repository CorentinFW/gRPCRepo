package org.tp1.agence.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ChambreDTO {
    private int id;
    private String nom;
    private float prix;
    private int nbrLits;  // Renommé de nbrDeLit
    private String hotelNom;
    private String hotelAdresse;

    @JsonProperty("image")  // Les hôtels utilisent "image", on mappe vers "imageUrl"
    private String imageUrl;

    private String agenceNom;  // Nom de l'agence qui propose cette chambre

    public ChambreDTO() {
    }

    public ChambreDTO(int id, String nom, float prix, int nbrLits, String hotelNom, String hotelAdresse) {
        this.id = id;
        this.nom = nom;
        this.prix = prix;
        this.nbrLits = nbrLits;
        this.hotelNom = hotelNom;
        this.hotelAdresse = hotelAdresse;
    }

    public ChambreDTO(int id, String nom, float prix, int nbrLits, String hotelNom, String hotelAdresse, String imageUrl) {
        this.id = id;
        this.nom = nom;
        this.prix = prix;
        this.nbrLits = nbrLits;
        this.hotelNom = hotelNom;
        this.hotelAdresse = hotelAdresse;
        this.imageUrl = imageUrl;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public int getNbrLits() {
        return nbrLits;
    }

    public void setNbrLits(int nbrLits) {
        this.nbrLits = nbrLits;
    }

    public String getHotelNom() {
        return hotelNom;
    }

    public void setHotelNom(String hotelNom) {
        this.hotelNom = hotelNom;
    }

    public String getHotelAdresse() {
        return hotelAdresse;
    }

    public void setHotelAdresse(String hotelAdresse) {
        this.hotelAdresse = hotelAdresse;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getAgenceNom() {
        return agenceNom;
    }

    public void setAgenceNom(String agenceNom) {
        this.agenceNom = agenceNom;
    }
}

