package org.tp1.client.dto;

/**
 * DTO pour la réponse suite à une réservation
 */
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

    // Getters et Setters
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

    @Override
    public String toString() {
        return "ReservationResponse{" +
                "reservationId=" + reservationId +
                ", message='" + message + '\'' +
                ", success=" + success +
                '}';
    }
}

