package org.tp1.hotellerie.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Gestionnaire {
    private List<Hotel> listeDHotel = new ArrayList<>();

    public List<Hotel> getListeDHotel() {
        return listeDHotel;
    }

    // Recherche une chambre selon des critères (implémentation minimale pour l’instant)
    public Chambre recherche(String adresse, String dateArrive, String dateDepart,
                             float prixMax, float prixMin, int nbrEtoile, int client) {
        Date arrive = parseDate(dateArrive);
        Date depart = parseDate(dateDepart);
        if (arrive == null || depart == null || !arrive.before(depart)) {
            return null;
        }
//test des critères
        for (Hotel hotel : listeDHotel) {
            if (hotel == null) continue;
            if (adresse != null && !adresse.trim().isEmpty()) {
                if (hotel.getAdresse() == null || !hotel.getAdresse().toLowerCase().contains(adresse.toLowerCase())) {
                    continue;
                }
            }
            if (nbrEtoile >= 1 && nbrEtoile <= 6) {
                if (hotel.getType() == null || hotel.getType().ordinal() + 1 != nbrEtoile) {
                    continue;
                }
            }

            for (Chambre chambre : hotel.getListeDesChambres()) {
                if (chambre == null) continue;
                if (prixMin > 0 && chambre.getPrix() < prixMin) continue;
                if (prixMax > 0 && chambre.getPrix() > prixMax) continue;
                if (client > 0 && chambre.getNbrDeLit() < client) continue;

                boolean disponible = true;
                for (Reservation r : hotel.getListeReservation()) {
                    if (r == null) continue;
                    if (r.getChambre() == null || r.getDateArrive() == null || r.getDateDepart() == null) continue;
                    if (r.getChambre().getId() != chambre.getId()) continue;
                    if (datesChevauchent(arrive, depart, r.getDateArrive(), r.getDateDepart())) {
                        disponible = false;
                        break;
                    }
                }
                if (disponible) {
                    return chambre;
                }
            }
        }
        return null;
    }

    // Ajoute une réservation en déléguant à l'hôtel ciblé
    public void rajouteReservation(Hotel hotel, Reservation reservation) {
        if (hotel != null && reservation != null) {
            hotel.ajoutReservation(reservation);
        }
    }

    private Date parseDate(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setLenient(false);
        try {
            return sdf.parse(s);
        } catch (ParseException e) {
            return null;
        }
    }

    private boolean datesChevauchent(Date d1Start, Date d1End, Date d2Start, Date d2End) {
        return d1Start.before(d2End) && d2Start.before(d1End);
    }
}
