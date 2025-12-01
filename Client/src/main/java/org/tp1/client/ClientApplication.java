package org.tp1.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.tp1.client.cli.ClientCLIRest;
import org.tp1.client.gui.ClientGUI;

import java.awt.GraphicsEnvironment;

/**
 * Application principale du client
 * Détecte automatiquement l'environnement et lance GUI ou CLI
 */
@SpringBootApplication
public class ClientApplication {

    public static void main(String[] args) {
        // Forcer le mode non-headless pour permettre l'affichage graphique
        System.setProperty("java.awt.headless", "false");

        // Démarrer Spring Boot mais sans serveur web
        ConfigurableApplicationContext context = SpringApplication.run(ClientApplication.class, args);

        // Détecter si un environnement graphique est disponible
        boolean isHeadless = GraphicsEnvironment.isHeadless();
        boolean hasDisplay = System.getenv("DISPLAY") != null && !System.getenv("DISPLAY").isEmpty();

        // Vérifier si l'utilisateur force un mode via argument
        boolean forceCLI = false;
        boolean forceGUI = false;
        for (String arg : args) {
            if ("--cli".equals(arg)) forceCLI = true;
            if ("--gui".equals(arg)) forceGUI = true;
        }

        if (forceCLI || (isHeadless && !forceGUI)) {
            // Mode CLI
            System.out.println("╔═══════════════════════════════════════════════════════════════╗");
            System.out.println("║           MODE CLI - Interface Ligne de Commande             ║");
            System.out.println("╚═══════════════════════════════════════════════════════════════╝");
            System.out.println();

            if (isHeadless) {
                System.out.println("ℹ️  Environnement sans interface graphique détecté");
                System.out.println("   → Utilisation du mode CLI");
                System.out.println();
            }

            ClientCLIRest cli = context.getBean(ClientCLIRest.class);
            cli.run();

            // Fermer le contexte Spring après la fin du CLI
            System.exit(SpringApplication.exit(context));
        } else {
            // Mode GUI
            System.out.println("╔═══════════════════════════════════════════════════════════════╗");
            System.out.println("║           MODE GUI - Interface Graphique                      ║");
            System.out.println("╚═══════════════════════════════════════════════════════════════╝");
            System.out.println();
            System.out.println("✓ Environnement graphique disponible");
            System.out.println("  → Ouverture de l'interface Swing...");
            System.out.println();

            ClientGUI gui = context.getBean(ClientGUI.class);
            gui.run();

            // Note: L'application reste active tant que la fenêtre GUI est ouverte
        }
    }
}

