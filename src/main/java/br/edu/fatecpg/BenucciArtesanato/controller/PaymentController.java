package br.edu.fatecpg.BenucciArtesanato.controller;

import br.edu.fatecpg.BenucciArtesanato.record.dto.*;
import br.edu.fatecpg.BenucciArtesanato.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/preference")
    public ResponseEntity<?> createPaymentPreference(@RequestBody PaymentPreferenceRequestDTO requestDTO) {
        try {
            PaymentResponseDTO response = paymentService.createPreference(requestDTO);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro ao criar preferência de pagamento: " + e.getMessage());
        }
    }
}
