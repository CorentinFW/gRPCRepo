package org.tp1.hotellerie.dto;


/**
 * DTO pour les informations d'une chambre d'hôtel (format REST/JSON)
 */
public class ChambreDTO {

    private Long id;
    private String nom;
    private Float prix;

    private Integer nbrLits;
    private Integer nbrEtoiles;
    private Boolean disponible;
    private String image;

    // Constructeurs
    public ChambreDTO() {
    }

    public ChambreDTO(Long id, String nom, Float prix, Integer nbrLits, Integer nbrEtoiles, Boolean disponible, String image) {
        this.id = id;
        this.nom = nom;
        this.prix = prix;
        this.nbrLits = nbrLits;
        this.nbrEtoiles = nbrEtoiles;
        this.disponible = disponible;
        this.image = image;
    }

    // Getters et Setters
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

    public Float getPrix() {
        return prix;
    }

    public void setPrix(Float prix) {
        this.prix = prix;
    }

    public Integer getNbrLits() {
        return nbrLits;
    }

    public void setNbrLits(Integer nbrLits) {
        this.nbrLits = nbrLits;
    }

    public Integer getNbrEtoiles() {
        return nbrEtoiles;
    }

    public void setNbrEtoiles(Integer nbrEtoiles) {
        this.nbrEtoiles = nbrEtoiles;
    }

    public Boolean getDisponible() {
        return disponible;
    }

    public void setDisponible(Boolean disponible) {
        this.disponible = disponible;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public String toString() {
        return "ChambreDTO{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prix=" + prix +
                ", nbrLits=" + nbrLits +
                ", nbrEtoiles=" + nbrEtoiles +
                ", disponible=" + disponible +
                ", image='" + image + '\'' +
                '}';
    }
}

