package br.edu.fatecpg.BenucciArtesanato.service;

import br.edu.fatecpg.BenucciArtesanato.model.Order;
import br.edu.fatecpg.BenucciArtesanato.model.OrderItem;
import br.edu.fatecpg.BenucciArtesanato.model.Payment;
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
    private final OrderService orderService; // 👈 injete o OrderService aqui
    public Payment createPayment(Order order) {
        try {
            // Monta lista de itens
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

            // Monta request
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("items", itemsList);
            requestBody.put("back_urls", Map.of(
                    "success", "https://yourapp.com/success",
                    "failure", "https://yourapp.com/failure",
                    "pending", "https://yourapp.com/pending"
            ));
            requestBody.put("auto_return", "all");
            requestBody.put("binary_mode", true);
            requestBody.put("external_reference", order.getId().toString());

            // Chamada à API Mercado Pago
            Map<String, Object> response = mercadoPagoWebClient.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            // Cria Payment
            Payment payment = new Payment();
            payment.setOrder(order);
            payment.setAmount(order.getTotalAmount());
            payment.setStatus("pending");

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
}
