package org.tp1.agence.dto;

public class ReservationResponse {
    private int reservationId;
    private String message;
    private boolean success;

    public ReservationResponse() {
    }

    public ReservationResponse(int reservationId, String message, boolean success) {
        this.reservationId = reservationId;
        this.message = message;
        this.success = success;
    }

    // Méthode factory pour créer une réponse de succès
    public static ReservationResponse success(int reservationId, String message) {
        return new ReservationResponse(reservationId, message, true);
    }

    // Méthode factory pour créer une réponse d'erreur
    public static ReservationResponse error(String message) {
        return new ReservationResponse(0, message, false);
    }

    public int getReservationId() {
        return reservationId;
    }

    public void setReservationId(int reservationId) {
        this.reservationId = reservationId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}

