package org.tp1.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.tp1.client.gui.ClientGUI;

import java.awt.GraphicsEnvironment;

/**
 * Application principale du client
 * Lance l'interface graphique (GUI)
 */
@SpringBootApplication
public class ClientApplication {

    public static void main(String[] args) {
        // Forcer le mode non-headless pour permettre l'affichage graphique
        System.setProperty("java.awt.headless", "false");

        // Démarrer Spring Boot mais sans serveur web
        ConfigurableApplicationContext context = SpringApplication.run(ClientApplication.class, args);

        // Mode GUI
        System.out.println("╔═══════════════════════════════════════════════════════════════╗");
        System.out.println("║           MODE GUI - Interface Graphique gRPC                 ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("✓ Lancement de l'interface graphique...");
        System.out.println("  → Ouverture de l'interface Swing...");
        System.out.println();

        ClientGUI gui = context.getBean(ClientGUI.class);
        gui.run();

        // Note: L'application reste active tant que la fenêtre GUI est ouverte
    }
}

