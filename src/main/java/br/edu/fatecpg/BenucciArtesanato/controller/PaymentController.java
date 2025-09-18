package br.edu.fatecpg.BenucciArtesanato.controller;

import br.edu.fatecpg.BenucciArtesanato.record.dto.PaymentRequestDTO;
import br.edu.fatecpg.BenucciArtesanato.record.dto.PaymentResponseDTO;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.client.payment.PaymentPayerRequest;
import com.mercadopago.client.common.IdentificationRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Value("${mercado.pago.access-token}")
    private String accessToken;

    @PostMapping("/pix")
    public ResponseEntity<?> createPixPayment(@RequestBody PaymentRequestDTO requestDTO) {
        try {
            MercadoPagoConfig.setAccessToken(accessToken);

            PaymentClient client = new PaymentClient();

            // Monta o pagamento
            var request = PaymentCreateRequest.builder()
                    .transactionAmount(BigDecimal.valueOf(requestDTO.amount()))
                    .description(requestDTO.description())
                    .paymentMethodId("pix")
                    .payer(PaymentPayerRequest.builder()
                            .firstName(requestDTO.payer().firstName())
                            .lastName(requestDTO.payer().lastName())
                            .email(requestDTO.payer().email())
                            .identification(IdentificationRequest.builder()
                                    .type("CPF")
                                    .number("12345678909") // Pode ser fixo para sandbox
                                    .build())
                            .build())
                    .build();

            // Cria pagamento
            var mpPayment = client.create(request);

            // Monta resposta com QR Code PIX
            PaymentResponseDTO response = new PaymentResponseDTO(
                    String.valueOf(mpPayment.getId()),
                    mpPayment.getTransactionAmount(),
                    mpPayment.getStatus(),
                    mpPayment.getPointOfInteraction() != null && mpPayment.getPointOfInteraction().getTransactionData() != null
                            ? mpPayment.getPointOfInteraction().getTransactionData().getQrCode() : null,
                    mpPayment.getPointOfInteraction() != null && mpPayment.getPointOfInteraction().getTransactionData() != null
                            ? mpPayment.getPointOfInteraction().getTransactionData().getQrCodeBase64() : null
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro ao criar pagamento PIX: " + e.getMessage());
        }
    }
}
