package br.edu.fatecpg.BenucciArtesanato.service;

import br.edu.fatecpg.BenucciArtesanato.model.Order;
import br.edu.fatecpg.BenucciArtesanato.model.Payment;
import br.edu.fatecpg.BenucciArtesanato.repository.OrderRepository;
import br.edu.fatecpg.BenucciArtesanato.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentWebhookService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final WebClient mercadoPagoWebClient;

    @Value("${MERCADOPAGO_ACCESS_TOKEN:}")
    private String mpAccessToken;

    private static final Duration MP_TIMEOUT = Duration.ofSeconds(5);

    @Transactional
    public void processWebHook(Map<String, Object> body) {
        log.info("=== PROCESSANDO WEBHOOK MERCADO PAGO ===");

        if (body == null) {
            log.warn("Webhook vazio recebido");
            return;
        }

        String type = Objects.toString(body.get("type"), "");
        if (!"payment".equalsIgnoreCase(type)) {
            log.debug("Webhook ignorado: type='{}'", type);
            return;
        }

        Object dataObj = body.get("data");
        if (!(dataObj instanceof Map<?, ?> data)) {
            log.warn("Formato inesperado de data: {}", dataObj);
            return;
        }

        String mpPaymentId = Objects.toString(data.get("id"), "").trim();
        if (mpPaymentId.isEmpty()) {
            log.warn("Webhook recebido sem data.id");
            return;
        }

        log.info("Webhook recebido: mpPaymentId={}", mpPaymentId);

        if (mpAccessToken == null || mpAccessToken.isBlank()) {
            log.error("mp.access.token não configurado. Abortando.");
            return;
        }

        Map<String, Object> mpResponse;
        try {
            mpResponse = mercadoPagoWebClient.get()
                    .uri("/v1/payments/{id}", mpPaymentId)
                    .header("Authorization", "Bearer " + mpAccessToken)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError,
                            resp -> handle4xx(resp, mpPaymentId))
                    .onStatus(HttpStatusCode::is5xxServerError,
                            resp -> handle5xx(resp, mpPaymentId))
                    .bodyToMono(Map.class)
                    .block(MP_TIMEOUT);


        } catch (WebClientResponseException e) {
            log.error("Erro HTTP ao consultar Mercado Pago (status={}): {}",
                    e.getStatusCode().value(), e.getResponseBodyAsString());
            return;

        } catch (Exception e) {
            log.error("Erro inesperado ao consultar Mercado Pago: {}", e.getMessage(), e);
            return;
        }

        if (mpResponse == null || mpResponse.isEmpty()) {
            log.error("MP retornou resposta nula/vazia para {}", mpPaymentId);
            return;
        }

        String mpStatus = Objects.toString(mpResponse.get("status"), "").toLowerCase();
        String preferenceId = Objects.toString(mpResponse.get("preference_id"), null);

        Optional<Payment> optionalPayment = paymentRepository.findByMpPaymentId(mpPaymentId);

        if (optionalPayment.isEmpty() && preferenceId != null) {
            optionalPayment = paymentRepository.findByMpPreferenceId(preferenceId);
        }

        if (optionalPayment.isEmpty()) {
            log.error("Nenhum Payment encontrado para mpPaymentId={} preferenceId={}", mpPaymentId, preferenceId);
            return;
        }

        Payment payment = optionalPayment.get();

        String mappedPaymentStatus = mapMpToPaymentStatus(mpStatus);
        Order.OrderStatus mappedOrderStatus = mapMpToOrderStatus(mpStatus);

        if (mappedPaymentStatus == null || mappedOrderStatus == null) {
            log.warn("Status MP não mapeado: {} — Ignorando.", mpStatus);
            return;
        }

        try {
            payment.setMpPaymentId(mpPaymentId);
            payment.setStatus(mappedPaymentStatus);
            paymentRepository.save(payment);
        } catch (DataAccessException ex) {
            log.error("Erro ao persistir Payment: {}", payment.getId(), ex);
            return;
        }

        Order order = payment.getOrder();
        if (order == null) {
            log.debug("Payment sem Order associada. Finalizando.");
            return;
        }

        if (order.getStatus() != mappedOrderStatus) {
            try {
                order.setStatus(mappedOrderStatus);
                orderRepository.save(order);
            } catch (DataAccessException ex) {
                log.error("Erro ao salvar Order {}", order.getId(), ex);
            }
        }

        log.info("=== FINALIZADO | paymentId={} | orderId={} ===", payment.getId(), order.getId());
    }

    private String mapMpToPaymentStatus(String mpStatus) {
        return switch (mpStatus) {
            case "approved" -> "approved";
            case "in_process", "pending" -> "pending";
            case "rejected", "cancelled", "refunded", "charged_back" -> "declined";
            default -> null;
        };
    }

    private Order.OrderStatus mapMpToOrderStatus(String mpStatus) {
        return switch (mpStatus) {
            case "approved" -> Order.OrderStatus.preparing;
            case "in_process", "pending" -> Order.OrderStatus.pending;
            case "rejected", "cancelled", "refunded", "charged_back" -> Order.OrderStatus.canceled;
            default -> Order.OrderStatus.pending;
        };
    }

    private Mono<? extends Throwable> handle4xx(ClientResponse response, String mpPaymentId) {
        return response.bodyToMono(String.class)
                .flatMap(body -> {
                    log.warn("MP 4xx | id={} | status={} | body={}",
                            mpPaymentId, response.statusCode(), body);

                    return Mono.error(new WebClientResponseException(
                            "Erro 4xx do Mercado Pago",
                            response.statusCode().value(),
                            response.statusCode().toString(),
                            null,
                            body.getBytes(),
                            null
                    ));
                });
    }

    private Mono<? extends Throwable> handle5xx(ClientResponse response, String mpPaymentId) {
        return response.bodyToMono(String.class)
                .flatMap(body -> {
                    log.error("MP 5xx | id={} | status={} | body={}",
                            mpPaymentId, response.statusCode(), body);

                    return Mono.error(new WebClientResponseException(
                            "Erro 5xx do Mercado Pago",
                            response.statusCode().value(),
                            response.statusCode().toString(),
                            null,
                            body.getBytes(),
                            null
                    ));
                });
    }

}
