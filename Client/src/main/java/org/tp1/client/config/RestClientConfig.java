package org.tp1.client.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Configuration REST pour le client
 */
@Configuration
public class RestClientConfig {

    /**
     * Bean RestTemplate pour effectuer des appels REST vers l'agence
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(5))  // Timeout de connexion
                .setReadTimeout(Duration.ofSeconds(10))     // Timeout de lecture
                .build();
    }
}

