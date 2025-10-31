package br.edu.fatecpg.BenucciArtesanato.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class MercadoPagoConfig {

    @Value("${mercadopago.base-url}")
    private String baseUrl;

    @Value("${mercadopago.access-token}")
    private String accessToken;

    @Value("${mercadopago.integrator-id:}")
    private String integratorId;

    @Bean
    public WebClient mercadoPagoWebClient() {
    WebClient.Builder builder = WebClient.builder()
        .baseUrl(baseUrl)
        .defaultHeader("Authorization", "Bearer " + accessToken);

    if (integratorId != null && !integratorId.isBlank()) {
        builder.defaultHeader("X-Integrator-Id", integratorId);
    }

    return builder.build();
    }
}
