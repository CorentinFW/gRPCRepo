package org.tp1.agence.dto;

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

