package br.edu.fatecpg.BenucciArtesanato.service;

import br.edu.fatecpg.BenucciArtesanato.model.Payment;
import br.edu.fatecpg.BenucciArtesanato.record.dto.PaymentRequestDTO;
import br.edu.fatecpg.BenucciArtesanato.repository.PaymentRepository;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.client.payment.PaymentPayerRequest;
import com.mercadopago.client.common.IdentificationRequest;
import br.edu.fatecpg.BenucciArtesanato.model.Payment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PaymentService {

    private final PaymentRepository repository;
    @Value("${'mercadopago.access-token}")
    private String mercadoPagoAccessToken;

    public PaymentService(PaymentRepository repository) {
        this.repository = repository;
        // Configure seu Access Token do Mercado Pago
        MercadoPagoConfig.setAccessToken(mercadoPagoAccessToken);
    }

    public Payment createPayment(PaymentRequestDTO dto) throws Exception {
        PaymentClient client = new PaymentClient();

        PaymentCreateRequest request = PaymentCreateRequest.builder()
                .transactionAmount(BigDecimal.valueOf(dto.amount()))
                .description(dto.description())
                .paymentMethodId("pix")
                .payer(PaymentPayerRequest.builder()
                        .firstName(dto.payer().firstName())
                        .lastName(dto.payer().lastName())
                        .email(dto.payer().email())
                        .identification(IdentificationRequest.builder()
                                .type(dto.payer().identification().type())
                                .number(dto.payer().identification().number())
                                .build())
                        .build())
                .build();

        // Cria pagamento via SDK
        com.mercadopago.resources.payment.Payment mpPayment = client.create(request);

        // Cria pagamento local
        Payment payment = new Payment();
        payment.setMercadoPagoId(String.valueOf(mpPayment.getId())); // converte Long -> String
        payment.setAmount(mpPayment.getTransactionAmount());
        payment.setStatus(mpPayment.getStatus());

        if (mpPayment.getPointOfInteraction() != null && mpPayment.getPointOfInteraction().getTransactionData() != null) {
            payment.setQrCode(mpPayment.getPointOfInteraction().getTransactionData().getQrCode());
            payment.setQrCodeBase64(mpPayment.getPointOfInteraction().getTransactionData().getQrCodeBase64());
        }

        return repository.save(payment);
    }
}
