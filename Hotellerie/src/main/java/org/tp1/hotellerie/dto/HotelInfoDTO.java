package org.tp1.hotellerie.dto;

/**
 * DTO pour les informations générales de l'hôtel (format REST/JSON)
 */
public class HotelInfoDTO {

    private String nom;
    private String adresse;
    private String ville;
    private String telephone;

    // Constructeurs
    public HotelInfoDTO() {
    }

    public HotelInfoDTO(String nom, String adresse, String ville, String telephone) {
        this.nom = nom;
        this.adresse = adresse;
        this.ville = ville;
        this.telephone = telephone;
    }

    // Getters et Setters
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

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    @Override
    public String toString() {
        return "HotelInfoDTO{" +
                "nom='" + nom + '\'' +
                ", adresse='" + adresse + '\'' +
                ", ville='" + ville + '\'' +
                ", telephone='" + telephone + '\'' +
                '}';
    }
}

