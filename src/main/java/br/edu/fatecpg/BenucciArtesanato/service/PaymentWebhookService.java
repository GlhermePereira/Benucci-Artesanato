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


import java.util.List;
import java.util.Map;
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
            String action = (body.get("action") != null) ? body.get("action").toString() : "n/a";
            String type = (body.get("type") != null) ? body.get("type").toString() : "n/a";

            Map<String, Object> data;
            if (body.get("data") instanceof Map) {
                data = (Map<String, Object>) body.get("data");
            } else data = null;

            if (data == null || data.get("id") == null ) {
                log.warn("Webhook recebido sem data.id");
                return;
            }

        String mpPaymentId = data.get("id").toString();
            log.info("Webhook: action={}, type={}, payment_id={}", action, type, mpPaymentId);

            Map<String, Object> mpResponse;
            mpResponse = mercadoPagoWebClient
                    .get()
                    .uri("/v1/payments/" + mpPaymentId)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (mpResponse == null ) {
                log.error("API do Mercado Pago retornou null ao consultar pagamento {}", mpPaymentId);
                return;
            }
            log.info("Detalhes do pagamento recebidos do MP: {}", mpResponse);

            String status = (mpResponse.get("status") != null) ? mpResponse.get("status").toString() : "unknown";
            String preferenceId = (mpResponse.get("preference_id") != null)
                    ? mpResponse.get("preference_id").toString()
                    : null;


            Optional<Payment> optionalPayment = Optional.empty();

            if (preferenceId != null) {
                optionalPayment = paymentRepository.findByMpPreferenceId(preferenceId);
                }
            if (optionalPayment.isEmpty()) {
                log.warn("Nenhum pagamento encontrado no banco para preference_id={}. Tentando por mp_payment_id...", preferenceId);
                optionalPayment = paymentRepository.findByMpPaymentId(mpPaymentId);
            }

            if (optionalPayment.isEmpty()) {
                log.error("Pagamento NÃO encontrado no banco para preference_id={} ou mp_payment_id={}", preferenceId, mpPaymentId);
                return;
            }

            Payment payment = optionalPayment.get();

            // ----------------------------------------------------
            // 4. Atualizar dados do pagamento no banco
            // ----------------------------------------------------
            payment.setMpPaymentId(mpPaymentId);
            payment.setStatus(status);

            paymentRepository.save(payment);

            log.info("Pagamento atualizado com sucesso! id={}, status={}", payment.getId(), payment.getStatus());
            log.info("=== FINALIZADO WEBHOOK ===");

        } catch (Exception e) {
            log.error("Erro ao processar webhook do Mercado Pago", e);
        }



    }
}
