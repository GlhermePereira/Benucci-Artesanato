package br.edu.fatecpg.BenucciArtesanato.service;

import br.edu.fatecpg.BenucciArtesanato.model.Order;
import br.edu.fatecpg.BenucciArtesanato.model.OrderItem;
import br.edu.fatecpg.BenucciArtesanato.model.Payment;
import br.edu.fatecpg.BenucciArtesanato.record.dto.PaymentResponseDTO;
import br.edu.fatecpg.BenucciArtesanato.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final WebClient mercadoPagoWebClient;

    // URL pública onde o Mercado Pago irá chamar o webhook
    private static final String NOTIFICATION_URL =
            "https://benucci-artesanato.onrender.com/webhook/mercadopago";

    // Token de teste do vendedor (sandbox)
    private static final String MP_ACCESS_TOKEN =
            "APP_USR-8233251246137327-092016-40f5cda162326e2aa9c6d42366cdffdf-2700246702";

    /**
     * Cria uma preferência de pagamento no Mercado Pago (sandbox)
     */
    public PaymentResponseDTO createPayment(Order order) {
        try {
            // 1. Montagem dos itens da preferência
            List<Map<String, Object>> itemsList = new ArrayList<>();
            for (OrderItem item : order.getItems()) {
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("id", item.getProduct().getId().toString());
                itemMap.put("title", item.getProductName());
                itemMap.put("quantity", item.getQuantity());
                itemMap.put("unit_price", item.getUnitPrice());
                itemMap.put("currency_id", "BRL");
                itemsList.add(itemMap);
            }

            // 2. Corpo da requisição para criar preferência
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("items", itemsList);
            requestBody.put("external_reference", order.getId().toString());
            requestBody.put("binary_mode", true);
            requestBody.put("auto_return", "all");
            requestBody.put("notification_url", NOTIFICATION_URL);
            requestBody.put("back_urls", Map.of(
                    "success", "https://yourapp.com/success",
                    "failure", "https://yourapp.com/failure",
                    "pending", "https://yourapp.com/pending"
            ));

            // 3. Chamada ao Mercado Pago (sandbox)
            Map<String, Object> response = mercadoPagoWebClient.post()
                    .uri("/checkout/preferences")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + MP_ACCESS_TOKEN)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            log.info("Resposta Mercado Pago: {}", response);

            if (response == null) {
                throw new IllegalStateException("Resposta nula do Mercado Pago.");
            }

            // 4. Captura preference_id retornado
            String preferenceId = Objects.toString(response.get("id"), null);
            if (preferenceId == null) {
                throw new IllegalStateException("Mercado Pago não retornou preference_id.");
            }

            // 5. Cria e salva o pagamento no banco
            Payment payment = new Payment();
            payment.setOrder(order);
            payment.setAmount(order.getTotalAmount());
            payment.setStatus("pending");
            payment.setPaymentMethod("Mercado Pago");
            payment.setMpPreferenceId(preferenceId);
            payment.setMpPaymentId(null); // pagamento ainda não existe
            payment.setInitPoint(response.get("init_point") != null ? response.get("init_point").toString() : null);
            payment.setSandboxLink(response.get("sandbox_init_point") != null ? response.get("sandbox_init_point").toString() : null);

            Payment savedPayment = paymentRepository.save(payment);

            // 6. Retorno DTO
            return new PaymentResponseDTO(
                    savedPayment.getMpPreferenceId(),
                    savedPayment.getAmount(),
                    savedPayment.getStatus(),
                    savedPayment.getInitPoint(),
                    savedPayment.getSandboxLink()
            );

        } catch (Exception e) {
            log.error("Erro ao criar preferência de pagamento", e);
            throw new RuntimeException("Erro ao criar preferência de pagamento: " + e.getMessage(), e);
        }
    }
}
