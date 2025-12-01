package br.edu.fatecpg.BenucciArtesanato.service;

import br.edu.fatecpg.BenucciArtesanato.model.Order;
import br.edu.fatecpg.BenucciArtesanato.model.OrderItem;
import br.edu.fatecpg.BenucciArtesanato.model.Payment;
import br.edu.fatecpg.BenucciArtesanato.record.dto.PaymentResponseDTO;
import br.edu.fatecpg.BenucciArtesanato.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final WebClient mercadoPagoWebClient;
    private final OrderService orderService;

    /**
     * Cria pagamento para uma Order (entidade) e retorna PaymentResponseDTO
     */
    public PaymentResponseDTO createPayment(Order order) {
        try {
            // Monta lista de itens a partir da entidade Order
            List<Map<String, Object>> itemsList = new ArrayList<>();
            for (OrderItem item : order.getItems()) {
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("id", item.getId().toString());
                itemMap.put("title", item.getProductName());
                itemMap.put("quantity", item.getQuantity());
                itemMap.put("unit_price", item.getUnitPrice());
                itemMap.put("currency_id", "BRL");
                itemsList.add(itemMap);
            }

            // Corpo da requisição Mercado Pago
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("items", itemsList);
            // Configura URLs de retorno para Deep Linking do App
            // O esquema deve corresponder ao configurado no app.json (benucci-artesanato)
            String appScheme = "benucci-artesanato://";
            requestBody.put("back_urls", Map.of(
                    "success", appScheme + "success",
                    "failure", appScheme + "failure",
                    "pending", appScheme + "pending"
            ));
            requestBody.put("auto_return", "approved"); // Retorna automaticamente apenas se aprovado
            requestBody.put("binary_mode", true);
            requestBody.put("external_reference", order.getId().toString());

            // Chamada Mercado Pago
            Map<String, Object> response = mercadoPagoWebClient.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            // Cria Payment e associa à Order
            Payment payment = new Payment();
            payment.setOrder(order);
            payment.setAmount(order.getTotalAmount());
            payment.setStatus("pending");
            payment.setPaymentMethod("Mercado Pago"); // ou do pedido

            if (response != null) {
                payment.setMpPreferenceId(String.valueOf(response.get("id")));
                payment.setInitPoint(String.valueOf(response.get("init_point")));
                payment.setSandboxLink(String.valueOf(response.get("sandbox_init_point")));
            }

            Payment savedPayment = paymentRepository.save(payment);

            // Atualiza Order com mpPreferenceId
            order.setMpPreferenceId(savedPayment.getMpPreferenceId());
            orderService.updateOrderStatus(order.getId(), order.getStatus().name()); // mantém método existente

            // Retorna DTO
            return new PaymentResponseDTO(
                    savedPayment.getMpPreferenceId(),
                    savedPayment.getAmount(),
                    savedPayment.getStatus(),
                    savedPayment.getInitPoint(),
                    savedPayment.getSandboxLink()
            );

        } catch (Exception e) {
            throw new RuntimeException("Erro ao criar preferência de pagamento: " + e.getMessage(), e);
        }
    }
}
