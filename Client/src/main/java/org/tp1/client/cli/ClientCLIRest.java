package org.tp1.client.cli;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tp1.client.dto.ChambreDTO;
import org.tp1.client.dto.ReservationResponse;
import org.tp1.client.rest.MultiAgenceRestClient;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Interface CLI pour interagir avec l'agence de réservation via REST
 */
@Component
public class ClientCLIRest {

    @Autowired
    private MultiAgenceRestClient agenceRestClient;

    private Scanner scanner;
    private List<ChambreDTO> dernieresChambres;

    // Couleurs ANSI
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String CYAN = "\u001B[36m";
    private static final String BOLD = "\u001B[1m";

    public void run() {
        scanner = new Scanner(System.in);

        afficherBanniere();

        // Test de connexion à l'agence
        System.out.print("Connexion à l'agence REST... ");
        try {
            String message = agenceRestClient.ping();
            System.out.println(GREEN + "✓ Connecté - " + message + RESET);
        } catch (Exception e) {
            System.out.println(RED + "✗ Échec - L'agence n'est pas disponible" + RESET);
            System.out.println("Assurez-vous que l'agence est démarrée sur le port 8081");
            System.out.println("Erreur: " + e.getMessage());
            return;
        }

        // Boucle principale
        boolean continuer = true;
        while (continuer) {
            afficherMenu();
            int choix = lireChoix();

            switch (choix) {
                case 1:
                    rechercherChambres();
                    break;
                case 2:
                    effectuerReservation();
                    break;
                case 3:
                    afficherDernieresChambres();
                    break;
                case 4:
                    afficherHotelsDisponibles();
                    break;
                case 5:
                    afficherChambresReservees();
                    break;
                case 6:
                    System.out.println("\n" + CYAN + "Au revoir !" + RESET);
                    continuer = false;
                    break;
                default:
                    System.out.println(RED + "Choix invalide" + RESET);
            }
        }

        scanner.close();
    }

    private void afficherBanniere() {
        System.out.println(CYAN + BOLD);
        System.out.println("╔═══════════════════════════════════════════════════╗");
        System.out.println("║                                                   ║");
        System.out.println("║   SYSTÈME DE RÉSERVATION - CLIENT MULTI-AGENCES  ║");
        System.out.println("║                                                   ║");
        System.out.println("╚═══════════════════════════════════════════════════╝");
        System.out.println(RESET);
    }

    private void afficherMenu() {
        System.out.println("\n" + BOLD + "═══ MENU PRINCIPAL ═══" + RESET);
        System.out.println("1. " + BLUE + "Rechercher des chambres" + RESET);
        System.out.println("2. " + GREEN + "Effectuer une réservation" + RESET);
        System.out.println("3. " + YELLOW + "Afficher les dernières chambres trouvées" + RESET);
        System.out.println("4. " + CYAN + "Afficher les hôtels disponibles" + RESET);
        System.out.println("5. " + RED + "Afficher les chambres réservées par hôtel" + RESET);
        System.out.println("6. " + RED + "Quitter" + RESET);
        System.out.print("\n" + BOLD + "Votre choix: " + RESET);
    }

    private int lireChoix() {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void rechercherChambres() {
        System.out.println("\n" + BOLD + BLUE + "═══ RECHERCHE DE CHAMBRES ═══" + RESET);

        // Adresse
        System.out.print("Adresse (ville/rue) [optionnel]: ");
        String adresse = scanner.nextLine().trim();

        // Date d'arrivée
        System.out.print("Date d'arrivée (YYYY-MM-DD): ");
        String dateArrive = scanner.nextLine().trim();

        // Date de départ
        System.out.print("Date de départ (YYYY-MM-DD): ");
        String dateDepart = scanner.nextLine().trim();

        // Prix minimum
        System.out.print("Prix minimum [optionnel, Enter pour ignorer]: ");
        String prixMinStr = scanner.nextLine().trim();
        Float prixMin = prixMinStr.isEmpty() ? null : Float.parseFloat(prixMinStr);

        // Prix maximum
        System.out.print("Prix maximum [optionnel, Enter pour ignorer]: ");
        String prixMaxStr = scanner.nextLine().trim();
        Float prixMax = prixMaxStr.isEmpty() ? null : Float.parseFloat(prixMaxStr);

        // Nombre d'étoiles
        System.out.print("Nombre d'étoiles (1-6) [optionnel, Enter pour ignorer]: ");
        String etoilesStr = scanner.nextLine().trim();
        Integer nbrEtoile = etoilesStr.isEmpty() ? null : Integer.parseInt(etoilesStr);

        // Nombre de lits
        System.out.print("Nombre de lits minimum [optionnel, Enter pour ignorer]: ");
        String litsStr = scanner.nextLine().trim();
        Integer nbrLits = litsStr.isEmpty() ? null : Integer.parseInt(litsStr);

        // Effectuer la recherche
        System.out.println("\n" + YELLOW + "Recherche en cours..." + RESET);
        try {
            List<ChambreDTO> chambres = agenceRestClient.rechercherChambres(
                adresse, dateArrive, dateDepart, prixMin, prixMax, nbrEtoile, nbrLits
            );

            dernieresChambres = chambres;

            if (chambres.isEmpty()) {
                System.out.println(YELLOW + "Aucune chambre trouvée avec ces critères." + RESET);
            } else {
                System.out.println(GREEN + "\n✓ " + chambres.size() + " chambre(s) trouvée(s):\n" + RESET);
                afficherChambres(chambres);
            }
        } catch (Exception e) {
            System.out.println(RED + "✗ Erreur lors de la recherche: " + e.getMessage() + RESET);
        }
    }

    private void afficherChambres(List<ChambreDTO> chambres) {
        int index = 1;
        for (ChambreDTO chambre : chambres) {
            System.out.println(BOLD + "─── Chambre " + index++ + " ───" + RESET);
            System.out.println("  🏨 Hôtel: " + CYAN + chambre.getHotelNom() + RESET);
            System.out.println("  📍 Adresse: " + chambre.getHotelAdresse());

            // Afficher l'agence si disponible
            if (chambre.getAgenceNom() != null && !chambre.getAgenceNom().isEmpty()) {
                System.out.println("  🏢 Agence: " + YELLOW + chambre.getAgenceNom() + RESET);
            }

            System.out.println("  🚪 Chambre: " + BLUE + chambre.getNom() + RESET + " (ID: " + chambre.getId() + ")");
            System.out.println("  💰 Prix: " + GREEN + String.format("%.2f", chambre.getPrix()) + " €" + RESET);
            System.out.println("  🛏️  Lits: " + chambre.getNbrLits());

            // Afficher l'URL de l'image si disponible
            if (chambre.getImageUrl() != null && !chambre.getImageUrl().isEmpty()) {
                System.out.println("  🖼️  Image: " + YELLOW + chambre.getImageUrl() + RESET);
            }

            System.out.println();
        }
    }

    private void effectuerReservation() {
        System.out.println("\n" + BOLD + GREEN + "═══ EFFECTUER UNE RÉSERVATION ═══" + RESET);

        if (dernieresChambres == null || dernieresChambres.isEmpty()) {
            System.out.println(YELLOW + "Veuillez d'abord effectuer une recherche de chambres (option 1)." + RESET);
            return;
        }

        // Afficher les chambres disponibles
        System.out.println("Chambres disponibles:");
        afficherChambres(dernieresChambres);

        // Sélectionner une chambre
        System.out.print("Numéro de la chambre à réserver (1-" + dernieresChambres.size() + "): ");
        int numeroChambre = lireChoix();
        if (numeroChambre < 1 || numeroChambre > dernieresChambres.size()) {
            System.out.println(RED + "Numéro de chambre invalide." + RESET);
            return;
        }

        ChambreDTO chambreChoisie = dernieresChambres.get(numeroChambre - 1);

        // Informations client
        System.out.println("\n" + BOLD + "Informations client:" + RESET);
        System.out.print("Nom: ");
        String nom = scanner.nextLine().trim();

        System.out.print("Prénom: ");
        String prenom = scanner.nextLine().trim();

        System.out.print("Numéro de carte bancaire: ");
        String numeroCarte = scanner.nextLine().trim();

        // Dates
        System.out.print("Date d'arrivée (YYYY-MM-DD): ");
        String dateArrive = scanner.nextLine().trim();

        System.out.print("Date de départ (YYYY-MM-DD): ");
        String dateDepart = scanner.nextLine().trim();

        // Confirmation
        System.out.println("\n" + BOLD + "Récapitulatif:" + RESET);
        System.out.println("  Hôtel: " + chambreChoisie.getHotelNom());
        System.out.println("  Chambre: " + chambreChoisie.getNom());
        System.out.println("  Prix: " + chambreChoisie.getPrix() + " €");
        System.out.println("  Client: " + prenom + " " + nom);
        System.out.println("  Dates: " + dateArrive + " → " + dateDepart);

        System.out.print("\nConfirmer la réservation ? (o/n): ");
        String confirmation = scanner.nextLine().trim().toLowerCase();

        if (!confirmation.equals("o") && !confirmation.equals("oui")) {
            System.out.println(YELLOW + "Réservation annulée." + RESET);
            return;
        }

        // Effectuer la réservation
        System.out.println("\n" + YELLOW + "Réservation en cours..." + RESET);
        try {
            ReservationResponse response = agenceRestClient.effectuerReservation(
                nom, prenom, numeroCarte,
                chambreChoisie.getId(),
                chambreChoisie.getHotelAdresse(),
                dateArrive, dateDepart,
                chambreChoisie.getAgenceNom()  // Passer le nom de l'agence
            );

            if (response.isSuccess()) {
                System.out.println(GREEN + "\n✓ Réservation effectuée avec succès!" + RESET);
                System.out.println("  ID de réservation: " + response.getReservationId());
                System.out.println("  Message: " + response.getMessage());
            } else {
                System.out.println(RED + "\n✗ Échec de la réservation" + RESET);
                System.out.println("  Message: " + response.getMessage());
            }
        } catch (Exception e) {
            System.out.println(RED + "✗ Erreur lors de la réservation: " + e.getMessage() + RESET);
        }
    }

    private void afficherDernieresChambres() {
        if (dernieresChambres == null || dernieresChambres.isEmpty()) {
            System.out.println(YELLOW + "\nAucune recherche effectuée. Utilisez l'option 1 d'abord." + RESET);
        } else {
            System.out.println("\n" + BOLD + YELLOW + "═══ DERNIÈRES CHAMBRES TROUVÉES ═══" + RESET);
            System.out.println(dernieresChambres.size() + " chambre(s):\n");
            afficherChambres(dernieresChambres);
        }
    }

    private void afficherHotelsDisponibles() {
        System.out.println("\n" + BOLD + CYAN + "═══ HÔTELS DISPONIBLES ═══" + RESET);
        System.out.println(YELLOW + "Récupération de la liste des hôtels..." + RESET);

        try {
            List<String> hotels = agenceRestClient.getHotelsDisponibles();

            if (hotels.isEmpty()) {
                System.out.println(YELLOW + "Aucun hôtel disponible pour le moment." + RESET);
            } else {
                System.out.println(GREEN + "\n✓ " + hotels.size() + " hôtel(s) disponible(s):\n" + RESET);
                int index = 1;
                for (String hotel : hotels) {
                    System.out.println("  " + index++ + ". 🏨 " + CYAN + hotel + RESET);
                }
            }
        } catch (Exception e) {
            System.out.println(RED + "✗ Erreur lors de la récupération des hôtels: " + e.getMessage() + RESET);
        }
    }

    private void afficherChambresReservees() {
        System.out.println("\n" + BOLD + RED + "═══ CHAMBRES RÉSERVÉES PAR HÔTEL ═══" + RESET);
        System.out.println(YELLOW + "Récupération des chambres réservées..." + RESET);

        try {
            Map<String, List<ChambreDTO>> chambresReserveesParHotel = agenceRestClient.getChambresReservees();

            if (chambresReserveesParHotel.isEmpty()) {
                System.out.println(YELLOW + "\nAucune chambre réservée dans aucun hôtel." + RESET);
                return;
            }

            int totalChambres = 0;
            for (Map.Entry<String, List<ChambreDTO>> entry : chambresReserveesParHotel.entrySet()) {
                String hotelNom = entry.getKey();
                List<ChambreDTO> chambres = entry.getValue();

                System.out.println("\n" + BOLD + "🏨 " + CYAN + hotelNom + RESET);
                System.out.println("─".repeat(50));

                if (chambres.isEmpty()) {
                    System.out.println("  " + YELLOW + "Aucune chambre réservée" + RESET);
                } else {
                    for (ChambreDTO chambre : chambres) {
                        System.out.println("  🚪 " + chambre.getNom() + " (ID: " + chambre.getId() + ")");
                        System.out.println("     💰 Prix: " + GREEN + String.format("%.2f", chambre.getPrix()) + " €" + RESET);
                        System.out.println("     🛏️  Lits: " + chambre.getNbrLits());

                        // Afficher l'agence si disponible
                        if (chambre.getAgenceNom() != null && !chambre.getAgenceNom().isEmpty()) {
                            System.out.println("     🏢 Agence: " + YELLOW + chambre.getAgenceNom() + RESET);
                        }

                        // Afficher l'URL de l'image si disponible
                        if (chambre.getImageUrl() != null && !chambre.getImageUrl().isEmpty()) {
                            System.out.println("     🖼️  Image: " + YELLOW + chambre.getImageUrl() + RESET);
                        }

                        System.out.println();
                    }
                    totalChambres += chambres.size();
                }
            }

            System.out.println("\n" + GREEN + "✓ Total: " + totalChambres + " chambre(s) réservée(s)" + RESET);

        } catch (Exception e) {
            System.out.println(RED + "✗ Erreur lors de la récupération des chambres réservées: " + e.getMessage() + RESET);
        }
    }
}

