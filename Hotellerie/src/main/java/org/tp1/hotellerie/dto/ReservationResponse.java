package org.tp1.hotellerie.dto;

/**
 * DTO pour la réponse suite à une réservation (format REST/JSON)
 */
public class ReservationResponse {

    private Long reservationId;
    private String message;
    private Boolean success;

    // Constructeurs
    public ReservationResponse() {
    }

    public ReservationResponse(Long reservationId, String message, Boolean success) {
        this.reservationId = reservationId;
        this.message = message;
        this.success = success;
    }

    // Constructeur factory pour succès
    public static ReservationResponse success(Long reservationId, String message) {
        return new ReservationResponse(reservationId, message, true);
    }

    // Constructeur factory pour erreur
    public static ReservationResponse error(String message) {
        return new ReservationResponse(null, message, false);
    }

    // Getters et Setters
    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    @Override
    public String toString() {
        return "ReservationResponse{" +
                "reservationId=" + reservationId +
                ", message='" + message + '\'' +
                ", success=" + success +
                '}';
    }
}

