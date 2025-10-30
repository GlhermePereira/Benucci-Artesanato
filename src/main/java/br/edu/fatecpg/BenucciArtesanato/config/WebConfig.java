package br.edu.fatecpg.BenucciArtesanato.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                        "http://localhost:3000",     // React dev server
                        "http://127.0.0.1:3000",    // React dev server alternativo
                        "http://192.168.1.198:3000", // Se o frontend estiver neste IP
                        "http://localhost:8081",     // Expo/React Native
                        "http://192.168.1.198:8081",  // Expo/React Native no IP local
                        "http://localhost:8081/swagger-ui/index.html",
                        "http://localhost:8081/swagger-ui.html"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}