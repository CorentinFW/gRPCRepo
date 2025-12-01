package org.tp1.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO pour les informations d'une chambre (côté client)
 */
public class ChambreDTO {
    private int id;
    private String nom;
    private float prix;
    private int nbrLits;  // Renommé de nbrDeLit
    private String hotelNom;
    private String hotelAdresse;

    @JsonProperty("image")  // Le JSON utilise "image" au lieu de "imageUrl"
    private String imageUrl;

    private String agenceNom;  // Nom de l'agence qui propose cette chambre

    public ChambreDTO() {
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

    @Override
    public String toString() {
        return "ChambreDTO{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prix=" + prix +
                ", nbrLits=" + nbrLits +
                ", hotelNom='" + hotelNom + '\'' +
                ", hotelAdresse='" + hotelAdresse + '\'' +
                ", agenceNom='" + agenceNom + '\'' +
                '}';
    }
}

