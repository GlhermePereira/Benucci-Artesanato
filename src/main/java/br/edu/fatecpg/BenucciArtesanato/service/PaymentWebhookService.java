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


    public void processWebHook(Map<String, Object> body) {
        log.info("=== PROCESSANDO WEBHOOK MERCADO PAGO ===");

        try {
            String action = Objects.toString(body.get("action"), "n/a");
            String type = Objects.toString(body.get("type"), "n/a");

            Map<String, Object> data = (body.get("data") instanceof Map)
                    ? (Map<String, Object>) body.get("data")
                    : null;

            if (data == null || data.get("id") == null) {
                log.warn("Webhook recebido sem data.id");
                return;
            }

            String mpPaymentId = Objects.toString(data.get("id"), null);

            log.info("Webhook recebido: action={}, type={}, mp_payment_id={}", action, type, mpPaymentId);

            // --------------------------------------------------------------------
            // CONSULTA COMPLETA NA API DO MERCADO PAGO
            // --------------------------------------------------------------------
            Map<String, Object> mpResponse = mercadoPagoWebClient
                    .get()
                    .uri("/v1/payments/" + mpPaymentId)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (mpResponse == null) {
                log.error("API do MP retornou null ao consultar pagamento {}", mpPaymentId);
                return;
            }

            log.info("MP response: {}", mpResponse);

            // --------------------------------------------------------------------
            // LÊ CAMPOS COMO STRINGS (MP ENVIA TUDO COMO STRING)
            // --------------------------------------------------------------------
            String status = Objects.toString(mpResponse.get("status"), "unknown");
            String preferenceId = Objects.toString(mpResponse.get("preference_id"), null);
            String externalReference = Objects.toString(mpResponse.get("external_reference"), null);
            String transactionAmountStr = Objects.toString(mpResponse.get("transaction_amount"), null);

            // converter amount
            BigDecimal amount = null;
            if (transactionAmountStr != null) {
                try {
                    amount = new BigDecimal(transactionAmountStr);
                } catch (Exception e) {
                    log.warn("Falha ao converter transaction_amount={}", transactionAmountStr);
                }
            }

            // --------------------------------------------------------------------
            // BUSCA O PAGAMENTO NO BANCO
            // SEM mp_payment_id — APENAS preference_id
            // --------------------------------------------------------------------
            if (preferenceId == null) {
                log.error("MP não enviou preference_id, impossível relacionar pagamento.");
                return;
            }

            Optional<Payment> optionalPayment = paymentRepository.findByMpPreferenceId(preferenceId);

            if (optionalPayment.isEmpty()) {
                log.error("Nenhum Payment encontrado para preference_id={}", preferenceId);
                return;
            }

            Payment payment = optionalPayment.get();

            // --------------------------------------------------------------------
            // ATUALIZAÇÃO DOS DADOS EXISTENTES NO BANCO
            // --------------------------------------------------------------------
            payment.setStatus(status);

            if (amount != null) {
                payment.setAmount(amount);
            }

            paymentRepository.save(payment);

            log.info("Pagamento atualizado: id={}, status={}, amount={}",
                    payment.getId(),
                    payment.getStatus(),
                    payment.getAmount()
            );

        } catch (Exception e) {
            log.error("Erro ao processar webhook MP", e);
        }

        log.info("=== FINALIZADO ===");
    }

}
