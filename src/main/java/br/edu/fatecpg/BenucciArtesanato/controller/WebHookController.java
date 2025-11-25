package br.edu.fatecpg.BenucciArtesanato.controller;

import br.edu.fatecpg.BenucciArtesanato.model.Order;
import br.edu.fatecpg.BenucciArtesanato.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/webhook")
public class WebHookController {

    private final OrderService orderService;

    public WebHookController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/mercadopago")
    public ResponseEntity<String> handlePayment(@RequestBody Map<String, Object> payload) {

        // Pega o ID do payment enviado pelo Mercado Pago
        Map<String, Object> data = (Map<String, Object>) payload.get("data");
        String mpPreferenceId = String.valueOf(data.get("id")); // sempre String, consistente com a entidade

        // Pega a action do evento (ex: payment.approved, payment.declined)
        String action = (String) payload.get("action");

        try {
            // Atualiza o status do pedido de acordo com a action
            switch (action) {
                case "payment.approved":
                    orderService.updateOrderStatusByMpPreferenceId(mpPreferenceId, Order.OrderStatus.preparing.name());
                    break;
                case "payment.declined":
                    orderService.updateOrderStatusByMpPreferenceId(mpPreferenceId, Order.OrderStatus.canceled.name());
                    break;
                default:
                    return ResponseEntity.badRequest().body("Action não tratada: " + action);
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        return ResponseEntity.ok("Webhook recebido com sucesso");
    }
}
