package org.tp1.hotellerie.dto;

/**
 * DTO pour les critères de recherche de chambres (format REST/JSON)
 */
public class RechercheRequest {

    private String adresse;
    private String dateArrive;
    private String dateDepart;

    private Float prixMin;
    private Float prixMax;
    private Integer nbrEtoile;
    private Integer nbrLits;

    // Constructeurs
    public RechercheRequest() {
    }

    public RechercheRequest(String adresse, String dateArrive, String dateDepart, Float prixMin, Float prixMax, Integer nbrEtoile, Integer nbrLits) {
        this.adresse = adresse;
        this.dateArrive = dateArrive;
        this.dateDepart = dateDepart;
        this.prixMin = prixMin;
        this.prixMax = prixMax;
        this.nbrEtoile = nbrEtoile;
        this.nbrLits = nbrLits;
    }

    // Getters et Setters
    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getDateArrive() {
        return dateArrive;
    }

    public void setDateArrive(String dateArrive) {
        this.dateArrive = dateArrive;
    }

    public String getDateDepart() {
        return dateDepart;
    }

    public void setDateDepart(String dateDepart) {
        this.dateDepart = dateDepart;
    }

    public Float getPrixMin() {
        return prixMin;
    }

    public void setPrixMin(Float prixMin) {
        this.prixMin = prixMin;
    }

    public Float getPrixMax() {
        return prixMax;
    }

    public void setPrixMax(Float prixMax) {
        this.prixMax = prixMax;
    }

    public Integer getNbrEtoile() {
        return nbrEtoile;
    }

    public void setNbrEtoile(Integer nbrEtoile) {
        this.nbrEtoile = nbrEtoile;
    }

    public Integer getNbrLits() {
        return nbrLits;
    }

    public void setNbrLits(Integer nbrLits) {
        this.nbrLits = nbrLits;
    }

    @Override
    public String toString() {
        return "RechercheRequest{" +
                "adresse='" + adresse + '\'' +
                ", dateArrive='" + dateArrive + '\'' +
                ", dateDepart='" + dateDepart + '\'' +
                ", prixMin=" + prixMin +
                ", prixMax=" + prixMax +
                ", nbrEtoile=" + nbrEtoile +
                ", nbrLits=" + nbrLits +
                '}';
    }
}

