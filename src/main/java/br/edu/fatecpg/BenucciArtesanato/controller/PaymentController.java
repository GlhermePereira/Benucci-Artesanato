package br.edu.fatecpg.BenucciArtesanato.controller;

import br.edu.fatecpg.BenucciArtesanato.model.Order;
import br.edu.fatecpg.BenucciArtesanato.model.Payment;
import br.edu.fatecpg.BenucciArtesanato.record.dto.OrderRequestDTO;
import br.edu.fatecpg.BenucciArtesanato.record.dto.PaymentResponseDTO;
import br.edu.fatecpg.BenucciArtesanato.service.OrderService;
import br.edu.fatecpg.BenucciArtesanato.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final OrderService orderService;

    @PostMapping("/preference")
    public ResponseEntity<?> createPaymentPreference(@RequestBody OrderRequestDTO requestDTO) {
        try {
            Order order = orderService.createOrder(requestDTO);
            Payment payment = paymentService.createPayment(order);

            return ResponseEntity.ok(new PaymentResponseDTO(
                    payment.getMpPreferenceId(),
                    payment.getAmount(),
                    order.getStatus(),
                    payment.getSandboxLink(),
                    payment.getInitPoint()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("Erro ao criar preferência de pagamento: " + e.getMessage());
        }
    }
}
