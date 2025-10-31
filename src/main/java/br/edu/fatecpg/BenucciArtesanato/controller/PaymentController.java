package br.edu.fatecpg.BenucciArtesanato.controller;

import br.edu.fatecpg.BenucciArtesanato.model.Order;
import br.edu.fatecpg.BenucciArtesanato.model.Payment;
import br.edu.fatecpg.BenucciArtesanato.record.dto.OrderRequestDTO;
import br.edu.fatecpg.BenucciArtesanato.record.dto.PaymentResponseDTO;
import br.edu.fatecpg.BenucciArtesanato.service.OrderService;
import br.edu.fatecpg.BenucciArtesanato.service.PaymentService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final OrderService orderService;
    private final br.edu.fatecpg.BenucciArtesanato.repository.PaymentRepository paymentRepository;

    @PostMapping("/preference")
    public ResponseEntity<?> createPaymentPreference(@RequestBody OrderRequestDTO requestDTO) {
        try {
            Order order = orderService.createOrder(requestDTO);
            Payment payment = paymentService.createPayment(order);

            return ResponseEntity.ok(new PaymentResponseDTO(
                    payment.getMpPreferenceId(),
                    payment.getAmount(),
                    order.getStatus().name(),
                    payment.getSandboxLink(),
                    payment.getInitPoint()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("Erro ao criar preferência de pagamento: " + e.getMessage());
        }
    }

    /**
     * Webhook endpoint para receber notificações do provedor de pagamento (ex: Mercado Pago).
     * Espera um JSON com ao menos um dos campos: external_reference (order id) ou mp_preference_id.
     * Pode conter também um campo 'paymentStatus' indicando 'approved'/'pending' etc.
     */
    @PostMapping("/webhook")
    public ResponseEntity<?> handleWebhook(
            @RequestParam Map<String, String> queryParams,
            @RequestBody(required = false) Map<String, Object> payload) {
        try {
            System.out.println("PaymentController.webhook -> queryParams: " + queryParams);
            System.out.println("PaymentController.webhook -> payload: " + payload);

            String topic = Optional.ofNullable(queryParams.get("topic"))
                    .orElseGet(() -> Optional.ofNullable(queryParams.get("type")).orElse(null));
            if (topic == null && payload != null) {
                topic = Optional.ofNullable((String) payload.get("type")).orElse(null);
            }

            String paymentId = queryParams.get("id");
            if (paymentId == null && queryParams.containsKey("resource")) {
                String resource = queryParams.get("resource");
                if (resource != null && resource.contains("/payments/")) {
                    paymentId = resource.substring(resource.lastIndexOf('/') + 1);
                }
            }

            if (paymentId == null && payload != null) {
                Object data = payload.get("data");
                if (data instanceof Map<?, ?> dataMap && dataMap.get("id") != null) {
                    paymentId = dataMap.get("id").toString();
                }
            }

            if (paymentId == null && payload != null && payload.get("id") != null) {
                // algumas notificações enviam o id no campo raiz
                paymentId = payload.get("id").toString();
            }

            if (!"payment".equalsIgnoreCase(topic) || paymentId == null) {
                System.out.println("PaymentController.webhook -> notificacao ignorada (topic=" + topic + ", paymentId=" + paymentId + ")");
                return ResponseEntity.ok("ignored");
            }

            Map<String, Object> paymentInfo = paymentService.fetchPaymentDetails(paymentId);
            System.out.println("PaymentController.webhook -> paymentInfo: " + paymentInfo);

            if (paymentInfo == null || paymentInfo.isEmpty()) {
                return ResponseEntity.status(404).body("payment info not found");
            }

            String externalReference = Optional.ofNullable(paymentInfo.get("external_reference"))
                    .map(Object::toString)
                    .orElse(null);
            String status = Optional.ofNullable(paymentInfo.get("status"))
                    .map(Object::toString)
                    .orElse("pending");
            String preferenceId = Optional.ofNullable(paymentInfo.get("preference_id"))
                    .map(Object::toString)
                    .orElse(null);

            Long orderId = null;
            if (externalReference != null) {
                try {
                    orderId = Long.valueOf(externalReference);
                } catch (NumberFormatException ignored) {}
            }

            Order order = null;
            if (orderId != null) {
                order = orderService.findOrderEntityById(orderId);
            } else if (preferenceId != null) {
                order = orderService.findByMpPreferenceId(preferenceId).orElse(null);
            }

            if (order == null) {
                System.out.println("PaymentController.webhook -> pedido não encontrado para external_reference=" + externalReference + " preferenceId=" + preferenceId);
                return ResponseEntity.status(404).body("Order not found");
            }

            Payment payment = order.getPayment();
            if (payment == null) {
                payment = new Payment();
                payment.setOrder(order);
                payment.setAmount(order.getTotalAmount());
            }

            payment.setMpPaymentId(paymentId);
            payment.setMpPreferenceId(order.getMpPreferenceId());
            payment.setStatus(status);
            payment.setPaymentMethod(Optional.ofNullable(paymentInfo.get("payment_method_id")).map(Object::toString).orElse(payment.getPaymentMethod()));
            payment.setPaymentDate(LocalDateTime.now());

            Object amountObj = paymentInfo.get("transaction_amount");
            if (amountObj instanceof Number number) {
                payment.setAmount(BigDecimal.valueOf(number.doubleValue()));
            }

            paymentRepository.save(payment);

            Order.OrderStatus newStatus = switch (status.toLowerCase()) {
                case "approved" -> Order.OrderStatus.preparing;
                case "in_process", "pending", "in_mediation" -> Order.OrderStatus.pending;
                case "cancelled", "refunded", "charged_back", "rejected" -> Order.OrderStatus.canceled;
                default -> order.getStatus();
            };

            order.setStatus(newStatus);
            orderService.updateOrder(order);

            System.out.println("PaymentController.webhook -> updated order " + order.getId() + " payment status=" + payment.getStatus());

            return ResponseEntity.ok("ok");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erro ao processar webhook: " + e.getMessage());
        }
    }
}
