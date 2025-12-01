package org.tp1.client.test;

import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.tp1.client.dto.RechercheRequest;

public class TestConnexionDirecte {

    public static void main(String[] args) {
        System.out.println("╔═══════════════════════════════════════════════════════════════╗");
        System.out.println("║     TEST DE CONNEXION DIRECTE AUX SERVICES                   ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════╝");
        System.out.println();

        RestTemplate restTemplate = new RestTemplate();

        // Test 1 : Ping Agence 1
        System.out.println("Test 1 : Ping Agence 1 (http://localhost:8081)");
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:8081/api/agence/ping",
                String.class
            );
            System.out.println("✓ Succès : " + response.getBody());
        } catch (Exception e) {
            System.out.println("✗ Erreur : " + e.getMessage());
        }
        System.out.println();

        // Test 2 : Recherche Agence 1
        System.out.println("Test 2 : Recherche chambres Lyon via Agence 1");
        try {
            String url = "http://localhost:8081/api/agence/chambres/rechercher";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String jsonBody = "{\"dateArrive\":\"2025-12-01\",\"dateDepart\":\"2025-12-05\",\"adresse\":\"Lyon\"}";

            HttpEntity<String> request = new HttpEntity<>(jsonBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            System.out.println("✓ Succès : " + response.getStatusCode());
            System.out.println("Nombre de caractères reçus : " + response.getBody().length());

            // Compter les chambres (approximatif)
            int count = response.getBody().split("\"id\"").length - 1;
            System.out.println("Nombre de chambres environ : " + count);
        } catch (Exception e) {
            System.out.println("✗ Erreur : " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();

        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.println("Si les tests ci-dessus fonctionnent, le problème est dans la GUI");
        System.out.println("Si les tests échouent, les services ne sont pas accessibles");
        System.out.println("═══════════════════════════════════════════════════════════════");
    }
}

