package br.edu.fatecpg.BenucciArtesanato.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class MercadoPagoConfig {

    @Value("${mercadopago.access-token}")
    private String accessToken;

    // WebClient para consultar pagamentos (usado no webhook)
    @Bean
    public WebClient mercadoPagoPaymentWebClient() {
        return WebClient.builder()
                .baseUrl("https://api.mercadopago.com") // base geral da API
                .defaultHeader("Authorization", "Bearer " + accessToken)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    // WebClient para criar preferências de pagamento
    @Bean
    public WebClient mercadoPagoPreferenceWebClient() {
        return WebClient.builder()
                .baseUrl("https://api.mercadopago.com/checkout/preferences") // endpoint de preferências
                .defaultHeader("Authorization", "Bearer " + accessToken)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
