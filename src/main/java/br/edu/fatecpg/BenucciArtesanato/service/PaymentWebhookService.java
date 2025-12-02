package br.edu.fatecpg.BenucciArtesanato.service;

import br.edu.fatecpg.BenucciArtesanato.model.Order;
import br.edu.fatecpg.BenucciArtesanato.model.OrderItem;
import br.edu.fatecpg.BenucciArtesanato.model.Payment;
import br.edu.fatecpg.BenucciArtesanato.repository.OrderItemRepository;
import br.edu.fatecpg.BenucciArtesanato.repository.OrderRepository;
import br.edu.fatecpg.BenucciArtesanato.repository.PaymentNotificationRepository;
import br.edu.fatecpg.BenucciArtesanato.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import lombok.extern.slf4j.Slf4j;


import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentWebhookService {

    private final PaymentRepository paymentRepository;
    private final WebClient mercadoPagoWebClient;
    private final WebClient mercadoPagoPaymentWebClient; // Injetado do config

    private static final String MP_ACCESS_TOKEN =
            "APP_USR-7329173875972159-120123-c8e1fc25840c193bbf8acf2550bbcdd4-3032944549";

    public void processWebHook(Map<String, Object> body) {
        log.info("=== PROCESSANDO WEBHOOK MERCADO PAGO ===");

        try {
            String type = Objects.toString(body.get("type"), "n/a");
            if (!"payment".equals(type)) {
                log.info("Webhook ignorado (não é payment): {}", type);
                return;
            }

            Map<String, Object> data = (Map<String, Object>) body.get("data");
            if (data == null || data.get("id") == null) {
                log.warn("Webhook recebido sem data.id");
                return;
            }

            String mpPaymentId = Objects.toString(data.get("id"));
            log.info("Webhook recebido para payment ID={}", mpPaymentId);

            // Consulta status do pagamento no Mercado Pago
            Map<String, Object> mpResponse = mercadoPagoPaymentWebClient.get()
                    .uri("/v1/payments/{id}", mpPaymentId)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();





            if (mpResponse == null) {
                log.error("MP retornou null para payment {}", mpPaymentId);
                return;
            }

            String status = Objects.toString(mpResponse.get("status"), "unknown");
            String statusDetail = Objects.toString(mpResponse.get("status_detail"), "");
            String preferenceId = Objects.toString(mpResponse.get("preference_id"));

            // Busca Payment no banco
            Optional<Payment> optionalPayment = paymentRepository.findByMpPaymentId(mpPaymentId);
            if (optionalPayment.isEmpty() && preferenceId != null) {
                optionalPayment = paymentRepository.findByMpPreferenceId(preferenceId);
            }

            if (optionalPayment.isEmpty()) {
                log.error("Nenhum Payment encontrado para mpPaymentId={} ou preferenceId={}", mpPaymentId, preferenceId);
                return;
            }

            Payment payment = optionalPayment.get();
            payment.setStatus(status);
            payment.setMpPaymentId(mpPaymentId); // salva MP ID para futuros webhooks

            paymentRepository.save(payment);

            log.info("Pagamento atualizado: id={}, status={}, statusDetail={}",
                    payment.getId(), payment.getStatus());

        } catch (Exception e) {
            log.error("Erro ao processar webhook Mercado Pago", e);
        }

        log.info("=== FINALIZADO ===");
    }
}




