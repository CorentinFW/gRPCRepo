package org.tp1.client.dto;

/**
 * DTO pour les critères de recherche de chambres
 */
public class RechercheRequest {
    private String adresse;
    private String dateArrive;
    private String dateDepart;
    private float prixMax;
    private float prixMin;
    private int nbrEtoile;
    private int nbrLits;

    public RechercheRequest() {
    }

    public RechercheRequest(String adresse, String dateArrive, String dateDepart,
                           float prixMin, float prixMax, int nbrEtoile, int nbrLits) {
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

    public float getPrixMax() {
        return prixMax;
    }

    public void setPrixMax(float prixMax) {
        this.prixMax = prixMax;
    }

    public float getPrixMin() {
        return prixMin;
    }

    public void setPrixMin(float prixMin) {
        this.prixMin = prixMin;
    }

    public int getNbrEtoile() {
        return nbrEtoile;
    }

    public void setNbrEtoile(int nbrEtoile) {
        this.nbrEtoile = nbrEtoile;
    }

    public int getNbrLits() {
        return nbrLits;
    }

    public void setNbrLits(int nbrLits) {
        this.nbrLits = nbrLits;
    }
}

