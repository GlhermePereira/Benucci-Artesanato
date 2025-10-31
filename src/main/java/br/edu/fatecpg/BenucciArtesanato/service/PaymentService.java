package br.edu.fatecpg.BenucciArtesanato.service;

import br.edu.fatecpg.BenucciArtesanato.model.Order;
import br.edu.fatecpg.BenucciArtesanato.model.OrderItem;
import br.edu.fatecpg.BenucciArtesanato.model.Payment;
import br.edu.fatecpg.BenucciArtesanato.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final WebClient mercadoPagoWebClient;
    private final OrderService orderService; // 👈 injete o OrderService aqui

    @Value("${mercadopago.back-urls.success}")
    private String successBackUrl;

    @Value("${mercadopago.back-urls.failure}")
    private String failureBackUrl;

    @Value("${mercadopago.back-urls.pending}")
    private String pendingBackUrl;

    @Value("${mercadopago.notification-url}")
    private String notificationUrl;
    public Payment createPayment(Order order) {
        try {
            // Monta lista de itens
            List<Map<String, Object>> itemsList = new ArrayList<>();
            for (OrderItem item : order.getItems()) {
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("id", item.getId() != null ? item.getId().toString() : null);
                itemMap.put("title", item.getProductName());
                itemMap.put("quantity", item.getQuantity());
                // Ensure numeric primitive is sent (double) instead of BigDecimal
                itemMap.put("unit_price", item.getUnitPrice() != null ? item.getUnitPrice().doubleValue() : 0.0);
                itemMap.put("currency_id", "BRL");
                itemsList.add(itemMap);
            }

            // Monta request
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("items", itemsList);

            if (hasValidBackUrls()) {
                Map<String, String> backUrls = new HashMap<>();
                backUrls.put("success", successBackUrl);
                backUrls.put("failure", failureBackUrl);
                backUrls.put("pending", pendingBackUrl);
                requestBody.put("back_urls", backUrls);
                requestBody.put("auto_return", "all");
            } else {
                System.out.println("PaymentService.createPayment -> skipping back_urls/auto_return (invalid or local URLs)");
            }

            requestBody.put("binary_mode", true);
            requestBody.put("external_reference", order.getId().toString());
            if (notificationUrl != null && !notificationUrl.isBlank()) {
                requestBody.put("notification_url", notificationUrl);
            }

        // Chamada à API Mercado Pago - enviar para /checkout/preferences
        System.out.println("PaymentService.createPayment -> requestBody to Mercado Pago: " + requestBody);
            Map<String, Object> response = null;
            try {
                response = mercadoPagoWebClient.post()
                        .uri("/checkout/preferences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(requestBody)
                        .retrieve()
                        .bodyToMono(Map.class)
                        .block();

                System.out.println("PaymentService.createPayment -> response from Mercado Pago: " + response);
            } catch (org.springframework.web.reactive.function.client.WebClientResponseException ex) {
                // Log response body for debugging (contains Mercado Pago error details)
                System.err.println("PaymentService.createPayment -> MercadoPago error response body: " + ex.getResponseBodyAsString());
                throw new RuntimeException("Erro ao criar preferência de pagamento: " + ex.getStatusCode() + " " + ex.getResponseBodyAsString(), ex);
            }

            // Cria Payment
            Payment payment = new Payment();
            payment.setOrder(order);
            payment.setAmount(order.getTotalAmount());
            payment.setStatus("pending");
            payment.setPaymentMethod("MERCADO_PAGO");

            if (response != null) {
                payment.setMpPreferenceId(String.valueOf(response.get("id")));
                payment.setInitPoint(String.valueOf(response.get("init_point")));
                payment.setSandboxLink(String.valueOf(response.get("sandbox_init_point")));
            }

            Payment savedPayment = paymentRepository.save(payment);

// Atualiza Order com mpPreferenceId
            order.setMpPreferenceId(savedPayment.getMpPreferenceId());
            orderService.updateOrder(order); // ✅ correto

            return savedPayment;


        } catch (Exception e) {
            throw new RuntimeException("Erro ao criar preferência de pagamento: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> fetchPaymentDetails(String paymentId) {
        if (paymentId == null || paymentId.isBlank()) {
            throw new IllegalArgumentException("paymentId inválido");
        }

        return mercadoPagoWebClient.get()
                .uri("/v1/payments/{id}", paymentId)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    private boolean hasValidBackUrls() {
        return isPublicHttps(successBackUrl)
                && isPublicHttps(failureBackUrl)
                && isPublicHttps(pendingBackUrl);
    }

    private boolean isPublicHttps(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }

        try {
            URI uri = URI.create(url);
            if (!"https".equalsIgnoreCase(uri.getScheme())) {
                return false;
            }

            String host = uri.getHost();
            if (host == null) {
                return false;
            }

            String lowerHost = host.toLowerCase(Locale.ROOT);
            if (lowerHost.equals("localhost") || lowerHost.equals("127.0.0.1")) {
                return false;
            }

            if (lowerHost.startsWith("192.168.")) {
                return false;
            }

            if (lowerHost.startsWith("10.")) {
                return false;
            }

            if (lowerHost.startsWith("172.")) {
                // disqualify private 172.x addresses
                return false;
            }

            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
