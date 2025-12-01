package org.tp1.client.dto;

/**
 * DTO pour les informations de réservation
 */
public class ReservationRequest {
    private String clientNom;
    private String clientPrenom;
    private String clientNumeroCarteBleue;
    private int chambreId;
    private String hotelAdresse;
    private String dateArrive;
    private String dateDepart;

    public ReservationRequest() {
    }

    public ReservationRequest(String clientNom, String clientPrenom, String clientNumeroCarteBleue,
                             int chambreId, String hotelAdresse, String dateArrive, String dateDepart) {
        this.clientNom = clientNom;
        this.clientPrenom = clientPrenom;
        this.clientNumeroCarteBleue = clientNumeroCarteBleue;
        this.chambreId = chambreId;
        this.hotelAdresse = hotelAdresse;
        this.dateArrive = dateArrive;
        this.dateDepart = dateDepart;
    }

    // Getters et Setters
    public String getClientNom() {
        return clientNom;
    }

    public void setClientNom(String clientNom) {
        this.clientNom = clientNom;
    }

    public String getClientPrenom() {
        return clientPrenom;
    }

    public void setClientPrenom(String clientPrenom) {
        this.clientPrenom = clientPrenom;
    }

    public String getClientNumeroCarteBleue() {
        return clientNumeroCarteBleue;
    }

    public void setClientNumeroCarteBleue(String clientNumeroCarteBleue) {
        this.clientNumeroCarteBleue = clientNumeroCarteBleue;
    }

    public int getChambreId() {
        return chambreId;
    }

    public void setChambreId(int chambreId) {
        this.chambreId = chambreId;
    }

    public String getHotelAdresse() {
        return hotelAdresse;
    }

    public void setHotelAdresse(String hotelAdresse) {
        this.hotelAdresse = hotelAdresse;
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
}

