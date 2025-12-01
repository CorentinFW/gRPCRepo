package org.tp1.client.gui;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tp1.client.dto.ChambreDTO;
import org.tp1.client.dto.ReservationResponse;
import org.tp1.client.rest.MultiAgenceRestClient;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * Interface graphique Swing pour le client de réservation multi-agences
 */
@Component
public class ClientGUI {

    @Autowired
    private MultiAgenceRestClient agenceRestClient;

    private List<ChambreDTO> dernieresChambres = new ArrayList<>();

    // Composants UI
    private JFrame frame;
    private JTable tableChambres;
    private DefaultTableModel tableModel;
    private JTextField txtAdresse, txtDateArrivee, txtDateDepart;
    private JTextField txtPrixMin, txtPrixMax, txtNbrEtoiles, txtNbrLits;
    private JTextArea txtConsole;
    private JLabel lblStatus;

    public void run() {
        SwingUtilities.invokeLater(() -> {
            createAndShowGUI();
            testConnexion();
        });
    }

    private void createAndShowGUI() {
        frame = new JFrame("Système de Réservation Multi-Agences");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800);
        frame.setLocationRelativeTo(null);

        // Menu bar
        createMenuBar();

        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top: Search panel
        mainPanel.add(createSearchPanel(), BorderLayout.NORTH);

        // Center: Results table
        mainPanel.add(createTablePanel(), BorderLayout.CENTER);

        // Bottom: Console and status
        mainPanel.add(createBottomPanel(), BorderLayout.SOUTH);

        frame.add(mainPanel);
        frame.setVisible(true);

        log("✓ Interface graphique chargée");
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // Menu Fichier
        JMenu menuFichier = new JMenu("Fichier");
        JMenuItem itemQuitter = new JMenuItem("Quitter");
        itemQuitter.addActionListener(e -> System.exit(0));
        menuFichier.add(itemQuitter);

        // Menu Actions
        JMenu menuActions = new JMenu("Actions");

        JMenuItem itemRecherche = new JMenuItem("Rechercher des chambres");
        itemRecherche.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));
        itemRecherche.addActionListener(e -> rechercherChambres());

        JMenuItem itemReserver = new JMenuItem("Réserver une chambre");
        itemReserver.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_DOWN_MASK));
        itemReserver.addActionListener(e -> reserverChambre());

        JMenuItem itemReservations = new JMenuItem("Voir les réservations");
        itemReservations.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK));
        itemReservations.addActionListener(e -> afficherReservations());

        JMenuItem itemHotels = new JMenuItem("Hôtels disponibles");
        itemHotels.addActionListener(e -> afficherHotels());

        menuActions.add(itemRecherche);
        menuActions.add(itemReserver);
        menuActions.addSeparator();
        menuActions.add(itemReservations);
        menuActions.add(itemHotels);

        // Menu Aide
        JMenu menuAide = new JMenu("Aide");
        JMenuItem itemAPropos = new JMenuItem("À propos");
        itemAPropos.addActionListener(e -> afficherAPropos());
        menuAide.add(itemAPropos);

        menuBar.add(menuFichier);
        menuBar.add(menuActions);
        menuBar.add(menuAide);

        frame.setJMenuBar(menuBar);
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Recherche de Chambres"));

        // Grid pour les champs
        JPanel gridPanel = new JPanel(new GridLayout(4, 4, 5, 5));

        // Ligne 1
        gridPanel.add(new JLabel("Adresse (ville):"));
        txtAdresse = new JTextField();
        gridPanel.add(txtAdresse);

        gridPanel.add(new JLabel("Date arrivée (YYYY-MM-DD):"));
        txtDateArrivee = new JTextField();
        gridPanel.add(txtDateArrivee);

        // Ligne 2
        gridPanel.add(new JLabel("Date départ (YYYY-MM-DD):"));
        txtDateDepart = new JTextField();
        gridPanel.add(txtDateDepart);

        gridPanel.add(new JLabel("Prix min:"));
        txtPrixMin = new JTextField();
        gridPanel.add(txtPrixMin);

        // Ligne 3
        gridPanel.add(new JLabel("Prix max:"));
        txtPrixMax = new JTextField();
        gridPanel.add(txtPrixMax);

        gridPanel.add(new JLabel("Nombre d'étoiles:"));
        txtNbrEtoiles = new JTextField();
        gridPanel.add(txtNbrEtoiles);

        // Ligne 4
        gridPanel.add(new JLabel("Nombre de lits min:"));
        txtNbrLits = new JTextField();
        gridPanel.add(txtNbrLits);

        gridPanel.add(new JLabel("")); // Spacer
        gridPanel.add(new JLabel("")); // Spacer

        panel.add(gridPanel, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnRechercher = new JButton("🔍 Rechercher");
        btnRechercher.addActionListener(e -> rechercherChambres());

        JButton btnEffacer = new JButton("🗑 Effacer");
        btnEffacer.addActionListener(e -> effacerRecherche());

        btnPanel.add(btnEffacer);
        btnPanel.add(btnRechercher);

        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Résultats de Recherche"));

        // Table
        String[] columns = {"ID", "Chambre", "Hôtel", "Adresse", "Agence", "Prix (€)", "Lits", "Image"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableChambres = new JTable(tableModel);
        tableChambres.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableChambres.setRowHeight(25);
        tableChambres.getColumnModel().getColumn(0).setPreferredWidth(50);
        tableChambres.getColumnModel().getColumn(1).setPreferredWidth(150);
        tableChambres.getColumnModel().getColumn(2).setPreferredWidth(150);
        tableChambres.getColumnModel().getColumn(3).setPreferredWidth(200);
        tableChambres.getColumnModel().getColumn(4).setPreferredWidth(150);
        tableChambres.getColumnModel().getColumn(5).setPreferredWidth(80);
        tableChambres.getColumnModel().getColumn(6).setPreferredWidth(50);
        tableChambres.getColumnModel().getColumn(7).setPreferredWidth(150);

        // Click handling
        tableChambres.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tableChambres.rowAtPoint(e.getPoint());
                int col = tableChambres.columnAtPoint(e.getPoint());

                if (row >= 0 && col == 7) {
                    // Clic sur la colonne Image (index 7)
                    afficherImage(row);
                } else if (e.getClickCount() == 2 && row >= 0) {
                    // Double-clic pour réserver
                    reserverChambre();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tableChambres);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Action buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnReserver = new JButton("📝 Réserver la chambre sélectionnée");
        btnReserver.addActionListener(e -> reserverChambre());
        btnPanel.add(btnReserver);

        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));

        // Console
        JPanel consolePanel = new JPanel(new BorderLayout());
        consolePanel.setBorder(BorderFactory.createTitledBorder("Console"));

        txtConsole = new JTextArea(8, 50);
        txtConsole.setEditable(false);
        txtConsole.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollConsole = new JScrollPane(txtConsole);
        scrollConsole.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        consolePanel.add(scrollConsole, BorderLayout.CENTER);

        panel.add(consolePanel, BorderLayout.CENTER);

        // Status bar
        lblStatus = new JLabel("Prêt");
        lblStatus.setBorder(BorderFactory.createEtchedBorder());
        panel.add(lblStatus, BorderLayout.SOUTH);

        return panel;
    }

    private void testConnexion() {
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return agenceRestClient.ping();
            }

            @Override
            protected void done() {
                try {
                    String message = get();
                    log("✓ Connexion établie: " + message);
                    setStatus("✓ Connecté aux agences");
                } catch (Exception e) {
                    log("✗ Erreur de connexion: " + e.getMessage());
                    setStatus("✗ Déconnecté");
                    JOptionPane.showMessageDialog(frame,
                        "Impossible de se connecter aux agences.\nVérifiez que les services sont démarrés.",
                        "Erreur de connexion",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void rechercherChambres() {
        String adresse = txtAdresse.getText().trim();
        String dateArrivee = txtDateArrivee.getText().trim();
        String dateDepart = txtDateDepart.getText().trim();

        // Validation des dates
        if (dateArrivee.isEmpty() || dateDepart.isEmpty()) {
            JOptionPane.showMessageDialog(frame,
                "Les dates d'arrivée et de départ sont obligatoires.\n\n" +
                "Format attendu: YYYY-MM-DD\n" +
                "Exemple: 2025-12-01",
                "Dates manquantes",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Vérifier que la date d'arrivée est avant la date de départ
        try {
            if (dateArrivee.compareTo(dateDepart) >= 0) {
                JOptionPane.showMessageDialog(frame,
                    "La date d'arrivée doit être AVANT la date de départ !\n\n" +
                    "Date d'arrivée: " + dateArrivee + "\n" +
                    "Date de départ: " + dateDepart + "\n\n" +
                    "Veuillez corriger les dates.",
                    "Dates invalides",
                    JOptionPane.ERROR_MESSAGE);
                log("✗ Dates invalides: arrivée=" + dateArrivee + " >= départ=" + dateDepart);
                return;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame,
                "Format de date invalide.\n\n" +
                "Format attendu: YYYY-MM-DD\n" +
                "Exemple: 2025-12-01",
                "Format invalide",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        Float prixMin = parseFloat(txtPrixMin.getText());
        Float prixMax = parseFloat(txtPrixMax.getText());
        Integer nbrEtoiles = parseInt(txtNbrEtoiles.getText());
        Integer nbrLits = parseInt(txtNbrLits.getText());

        log("🔍 Recherche de chambres...");
        log("   Critères: adresse=" + (adresse.isEmpty() ? "aucune" : adresse) +
            ", dates=" + dateArrivee + " → " + dateDepart);
        setStatus("Recherche en cours...");

        new SwingWorker<List<ChambreDTO>, Void>() {
            @Override
            protected List<ChambreDTO> doInBackground() throws Exception {
                log("   Appel du client REST...");
                List<ChambreDTO> result = agenceRestClient.rechercherChambres(
                    adresse, dateArrivee, dateDepart,
                    prixMin, prixMax, nbrEtoiles, nbrLits
                );
                log("   Réponse reçue: " + result.size() + " chambre(s)");
                return result;
            }

            @Override
            protected void done() {
                try {
                    List<ChambreDTO> chambres = get();
                    dernieresChambres = chambres;
                    afficherChambres(chambres);
                    if (chambres.isEmpty()) {
                        log("⚠ Aucune chambre trouvée");
                        log("   Vérifiez que les services backend sont démarrés:");
                        log("   - Hôtels sur ports 8082, 8083, 8084");
                        log("   - Agences sur ports 8081, 8085");
                    } else {
                        log("✓ " + chambres.size() + " chambre(s) trouvée(s)");
                    }
                    setStatus(chambres.size() + " résultat(s)");
                } catch (Exception e) {
                    log("✗ Erreur lors de la recherche: " + e.getMessage());
                    log("   Cause probable: Services backend non démarrés");
                    setStatus("Erreur de recherche");

                    String message = "Erreur lors de la recherche:\n" + e.getMessage() +
                                   "\n\nCause probable:\n" +
                                   "Les services backend ne sont pas démarrés.\n\n" +
                                   "Solution:\n" +
                                   "Lancez: ./start-system-complete-gui.sh\n" +
                                   "depuis un autre terminal";

                    JOptionPane.showMessageDialog(frame,
                        message,
                        "Erreur - Services non démarrés",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void afficherChambres(List<ChambreDTO> chambres) {
        tableModel.setRowCount(0);

        for (ChambreDTO chambre : chambres) {
            Object[] row = {
                chambre.getId(),
                chambre.getNom(),
                chambre.getHotelNom(),
                chambre.getHotelAdresse(),
                chambre.getAgenceNom(),
                String.format("%.2f", chambre.getPrix()),
                chambre.getNbrLits(),
                chambre.getImageUrl() != null ? "🖼" : ""
            };
            tableModel.addRow(row);
        }
    }

    private void afficherImage(int row) {
        if (row < 0 || row >= dernieresChambres.size()) {
            return;
        }

        ChambreDTO chambre = dernieresChambres.get(row);

        if (chambre.getImageUrl() == null || chambre.getImageUrl().isEmpty()) {
            JOptionPane.showMessageDialog(frame,
                "Aucune image disponible pour cette chambre.",
                "Pas d'image",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        log("🖼 Chargement de l'image: " + chambre.getImageUrl());

        new SwingWorker<ImageIcon, Void>() {
            @Override
            protected ImageIcon doInBackground() throws Exception {
                try {
                    // Télécharger l'image depuis l'URL
                    java.net.URL url = new java.net.URL(chambre.getImageUrl());
                    java.awt.image.BufferedImage image = javax.imageio.ImageIO.read(url);

                    if (image == null) {
                        return null;
                    }

                    // Redimensionner l'image (max 800x600 pour l'affichage)
                    int maxWidth = 800;
                    int maxHeight = 600;

                    int width = image.getWidth();
                    int height = image.getHeight();

                    double scale = Math.min(
                        (double) maxWidth / width,
                        (double) maxHeight / height
                    );

                    if (scale < 1.0) {
                        int newWidth = (int) (width * scale);
                        int newHeight = (int) (height * scale);

                        java.awt.Image scaledImage = image.getScaledInstance(
                            newWidth, newHeight, java.awt.Image.SCALE_SMOOTH
                        );

                        return new ImageIcon(scaledImage);
                    }

                    return new ImageIcon(image);

                } catch (Exception e) {
                    throw e;
                }
            }

            @Override
            protected void done() {
                try {
                    ImageIcon imageIcon = get();

                    if (imageIcon == null) {
                        log("✗ Impossible de charger l'image");
                        JOptionPane.showMessageDialog(frame,
                            "Impossible de charger l'image.\n\n" +
                            "URL: " + chambre.getImageUrl(),
                            "Erreur de chargement",
                            JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    log("✓ Image chargée avec succès");

                    // Créer une fenêtre pour afficher l'image
                    JDialog dialog = new JDialog(frame, "Image - " + chambre.getNom(), true);
                    dialog.setLayout(new BorderLayout());

                    // Panel avec l'image
                    JLabel imageLabel = new JLabel(imageIcon);
                    imageLabel.setHorizontalAlignment(JLabel.CENTER);
                    JScrollPane scrollPane = new JScrollPane(imageLabel);

                    // Informations sur la chambre
                    JPanel infoPanel = new JPanel(new GridLayout(5, 2, 10, 5));
                    infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                    infoPanel.add(new JLabel("Chambre:"));
                    infoPanel.add(new JLabel(chambre.getNom()));

                    infoPanel.add(new JLabel("Hôtel:"));
                    infoPanel.add(new JLabel(chambre.getHotelNom()));

                    infoPanel.add(new JLabel("Adresse:"));
                    infoPanel.add(new JLabel(chambre.getHotelAdresse()));

                    infoPanel.add(new JLabel("Prix:"));
                    infoPanel.add(new JLabel(String.format("%.2f €", chambre.getPrix())));

                    infoPanel.add(new JLabel("Lits:"));
                    infoPanel.add(new JLabel(chambre.getNbrLits() + " lit(s)"));

                    // Bouton Fermer
                    JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                    JButton btnClose = new JButton("Fermer");
                    btnClose.addActionListener(e -> dialog.dispose());
                    btnPanel.add(btnClose);

                    // Ajouter les composants
                    dialog.add(infoPanel, BorderLayout.NORTH);
                    dialog.add(scrollPane, BorderLayout.CENTER);
                    dialog.add(btnPanel, BorderLayout.SOUTH);

                    // Configurer la fenêtre
                    dialog.pack();
                    dialog.setLocationRelativeTo(frame);
                    dialog.setVisible(true);

                } catch (Exception e) {
                    log("✗ Erreur lors du chargement de l'image: " + e.getMessage());
                    JOptionPane.showMessageDialog(frame,
                        "Erreur lors du chargement de l'image:\n" + e.getMessage() + "\n\n" +
                        "URL: " + chambre.getImageUrl(),
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void reserverChambre() {
        int selectedRow = tableChambres.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame,
                "Veuillez sélectionner une chambre dans le tableau.",
                "Aucune sélection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (dernieresChambres.isEmpty() || selectedRow >= dernieresChambres.size()) {
            JOptionPane.showMessageDialog(frame,
                "Erreur: chambre invalide.",
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        ChambreDTO chambre = dernieresChambres.get(selectedRow);

        // Dialog de réservation
        JPanel panel = new JPanel(new GridLayout(6, 2, 5, 5));

        JTextField txtNom = new JTextField();
        JTextField txtPrenom = new JTextField();
        JTextField txtCarte = new JTextField();
        JTextField txtDateArr = new JTextField(txtDateArrivee.getText());
        JTextField txtDateDep = new JTextField(txtDateDepart.getText());

        panel.add(new JLabel("Nom:"));
        panel.add(txtNom);
        panel.add(new JLabel("Prénom:"));
        panel.add(txtPrenom);
        panel.add(new JLabel("N° Carte:"));
        panel.add(txtCarte);
        panel.add(new JLabel("Date arrivée:"));
        panel.add(txtDateArr);
        panel.add(new JLabel("Date départ:"));
        panel.add(txtDateDep);
        panel.add(new JLabel("Prix total:"));
        panel.add(new JLabel(String.format("%.2f €", chambre.getPrix())));

        int result = JOptionPane.showConfirmDialog(frame, panel,
            "Réservation - " + chambre.getNom() + " (" + chambre.getHotelNom() + ")",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String nom = txtNom.getText().trim();
            String prenom = txtPrenom.getText().trim();
            String carte = txtCarte.getText().trim();
            String dateArr = txtDateArr.getText().trim();
            String dateDep = txtDateDep.getText().trim();

            if (nom.isEmpty() || prenom.isEmpty() || carte.isEmpty() ||
                dateArr.isEmpty() || dateDep.isEmpty()) {
                JOptionPane.showMessageDialog(frame,
                    "Tous les champs sont obligatoires.",
                    "Champs manquants",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            effectuerReservation(chambre, nom, prenom, carte, dateArr, dateDep);
        }
    }

    private void effectuerReservation(ChambreDTO chambre, String nom, String prenom,
                                     String carte, String dateArr, String dateDep) {
        log("📝 Réservation en cours...");
        setStatus("Réservation en cours...");

        new SwingWorker<ReservationResponse, Void>() {
            @Override
            protected ReservationResponse doInBackground() throws Exception {
                return agenceRestClient.effectuerReservation(
                    nom, prenom, carte,
                    chambre.getId(),
                    chambre.getHotelAdresse(),
                    dateArr, dateDep,
                    chambre.getAgenceNom()
                );
            }

            @Override
            protected void done() {
                try {
                    ReservationResponse response = get();
                    if (response.isSuccess()) {
                        log("✓ Réservation confirmée: " + response.getReservationId());
                        setStatus("Réservation réussie");
                        JOptionPane.showMessageDialog(frame,
                            "Réservation confirmée!\n\n" +
                            "ID: " + response.getReservationId() + "\n" +
                            "Message: " + response.getMessage(),
                            "Succès",
                            JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        log("✗ Réservation échouée: " + response.getMessage());
                        setStatus("Réservation échouée");
                        JOptionPane.showMessageDialog(frame,
                            "La réservation a échoué:\n" + response.getMessage(),
                            "Échec",
                            JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    log("✗ Erreur: " + e.getMessage());
                    setStatus("Erreur");
                    JOptionPane.showMessageDialog(frame,
                        "Erreur lors de la réservation:\n" + e.getMessage(),
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void afficherReservations() {
        log("📊 Récupération des réservations...");
        setStatus("Chargement des réservations...");

        new SwingWorker<Map<String, List<ChambreDTO>>, Void>() {
            @Override
            protected Map<String, List<ChambreDTO>> doInBackground() throws Exception {
                return agenceRestClient.getChambresReservees();
            }

            @Override
            protected void done() {
                try {
                    Map<String, List<ChambreDTO>> reservations = get();

                    StringBuilder sb = new StringBuilder();
                    sb.append("═══ CHAMBRES RÉSERVÉES PAR HÔTEL ═══\n\n");

                    int total = 0;
                    for (Map.Entry<String, List<ChambreDTO>> entry : reservations.entrySet()) {
                        sb.append("🏨 ").append(entry.getKey()).append("\n");
                        for (int i = 0; i < 50; i++) sb.append("─");
                        sb.append("\n");

                        if (entry.getValue().isEmpty()) {
                            sb.append("  Aucune réservation\n");
                        } else {
                            for (ChambreDTO chambre : entry.getValue()) {
                                sb.append(String.format("  🚪 %s (ID: %d) - %.2f € - %d lit(s)\n",
                                    chambre.getNom(), chambre.getId(),
                                    chambre.getPrix(), chambre.getNbrLits()));
                                total++;
                            }
                        }
                        sb.append("\n");
                    }

                    sb.append("✓ Total: ").append(total).append(" réservation(s)");

                    JTextArea textArea = new JTextArea(sb.toString());
                    textArea.setEditable(false);
                    textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
                    JScrollPane scrollPane = new JScrollPane(textArea);
                    scrollPane.setPreferredSize(new Dimension(600, 400));

                    JOptionPane.showMessageDialog(frame,
                        scrollPane,
                        "Réservations",
                        JOptionPane.INFORMATION_MESSAGE);

                    log("✓ " + total + " réservation(s) affichée(s)");
                    setStatus("Prêt");
                } catch (Exception e) {
                    log("✗ Erreur: " + e.getMessage());
                    setStatus("Erreur");
                    JOptionPane.showMessageDialog(frame,
                        "Erreur lors de la récupération:\n" + e.getMessage(),
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void afficherHotels() {
        log("🏨 Récupération de la liste des hôtels...");

        new SwingWorker<List<String>, Void>() {
            @Override
            protected List<String> doInBackground() throws Exception {
                return agenceRestClient.getHotelsDisponibles();
            }

            @Override
            protected void done() {
                try {
                    List<String> hotels = get();

                    StringBuilder sb = new StringBuilder();
                    sb.append("═══ HÔTELS DISPONIBLES ═══\n\n");

                    for (int i = 0; i < hotels.size(); i++) {
                        sb.append(String.format("%d. 🏨 %s\n", i + 1, hotels.get(i)));
                    }

                    JTextArea textArea = new JTextArea(sb.toString());
                    textArea.setEditable(false);
                    textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

                    JOptionPane.showMessageDialog(frame,
                        new JScrollPane(textArea),
                        "Hôtels Disponibles",
                        JOptionPane.INFORMATION_MESSAGE);

                    log("✓ " + hotels.size() + " hôtel(s) disponible(s)");
                } catch (Exception e) {
                    log("✗ Erreur: " + e.getMessage());
                    JOptionPane.showMessageDialog(frame,
                        "Erreur:\n" + e.getMessage(),
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void afficherAPropos() {
        String message = "Système de Réservation Multi-Agences\n\n" +
                        "Version 2.0 - Interface Graphique\n" +
                        "Architecture REST avec Spring Boot\n\n" +
                        "Fonctionnalités:\n" +
                        "• Recherche de chambres multi-agences\n" +
                        "• Comparaison de prix en temps réel\n" +
                        "• Réservation en ligne\n" +
                        "• Consultation des réservations\n\n" +
                        "© 2025 - Projet Multi-Agences";

        JOptionPane.showMessageDialog(frame, message, "À propos",
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void effacerRecherche() {
        txtAdresse.setText("");
        txtDateArrivee.setText("");
        txtDateDepart.setText("");
        txtPrixMin.setText("");
        txtPrixMax.setText("");
        txtNbrEtoiles.setText("");
        txtNbrLits.setText("");
        log("🗑 Formulaire effacé");
    }

    private void log(String message) {
        String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
        txtConsole.append("[" + timestamp + "] " + message + "\n");
        txtConsole.setCaretPosition(txtConsole.getDocument().getLength());
    }

    private void setStatus(String status) {
        lblStatus.setText(" " + status);
    }

    private Float parseFloat(String text) {
        if (text == null || text.trim().isEmpty()) return null;
        try {
            return Float.parseFloat(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseInt(String text) {
        if (text == null || text.trim().isEmpty()) return null;
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

