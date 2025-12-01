package org.tp1.hotellerie.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration pour servir les images statiques
 */
@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Servir les images depuis le dossier Image
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:Image/")
                .addResourceLocations("classpath:/static/images/");
    }
}

