package org.tp1.agence.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Configuration REST pour l'agence
 * Définit les beans nécessaires pour les appels REST
 */
@Configuration
public class RestClientConfig {

    /**
     * Bean RestTemplate pour effectuer des appels REST synchrones
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(5))  // Timeout de connexion
                .setReadTimeout(Duration.ofSeconds(10))     // Timeout de lecture
                .build();
    }
}

