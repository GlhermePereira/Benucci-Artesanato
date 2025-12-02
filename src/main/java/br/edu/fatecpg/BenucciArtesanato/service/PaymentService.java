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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final WebClient mercadoPagoWebClient;

    private static final String NOTIFICATION_URL =
            "https://benucci-artesanato.onrender.com/webhook/mercadopago";

    private static final String MP_ACCESS_TOKEN =
            "APP_USR-7329173875972159-120123-c8e1fc25840c193bbf8acf2550bbcdd4-3032944549";

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    public PaymentResponseDTO createPayment(Order order) {
        try {
            // 1. Montagem dos itens
            List<Map<String, Object>> itemsList = new ArrayList<>();
            for (OrderItem item : order.getItems()) {
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("id", item.getProduct().getId().toString());
                itemMap.put("title", item.getProductName());
                itemMap.put("quantity", item.getQuantity());
                itemMap.put("unit_price", item.getUnitPrice());
                itemMap.put("description", item.getProduct().getDescription());
                itemMap.put("category_id", "retail");
                itemsList.add(itemMap);
            }


            // 5. Montagem do corpo da requisição
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("items", itemsList);
            requestBody.put("external_reference", order.getId().toString());
            requestBody.put("binary_mode", false);
            requestBody.put("notification_url", NOTIFICATION_URL);


            // 6. Chamada ao Mercado Pago
            Map<String, Object> response = mercadoPagoWebClient.post()
                    .uri("/checkout/preferences")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + MP_ACCESS_TOKEN)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            log.info("Resposta Mercado Pago: {}", response);
            if (response == null || !response.containsKey("id")) {
                throw new IllegalStateException("Resposta inválida do Mercado Pago.");
            }

            // 7. Salva pagamento no banco
            Payment payment = new Payment();
            payment.setOrder(order);
            payment.setAmount(order.getTotalAmount());
            payment.setStatus("pending");
            payment.setPaymentMethod("Mercado Pago");
            payment.setMpPreferenceId(response.get("id").toString());
            payment.setMpPaymentId(null);
            payment.setInitPoint(response.getOrDefault("init_point", "").toString());
            payment.setSandboxLink(response.getOrDefault("sandbox_init_point", "").toString());

            Payment savedPayment = paymentRepository.save(payment);

            // 8. Retorno DTO
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
