package br.edu.fatecpg.BenucciArtesanato.service;

import br.edu.fatecpg.BenucciArtesanato.model.Order;
import br.edu.fatecpg.BenucciArtesanato.record.dto.*;
import br.edu.fatecpg.BenucciArtesanato.repository.OrderRepository;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceCreateRequest;
import com.mercadopago.client.preference.PreferencePayerRequest;
import com.mercadopago.client.preference.PreferenceItemRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.stream.Collectors;
import java.math.BigDecimal;

@Service
public class PaymentService {

    @Value("${mercadopago.access-token}")
    private String accessToken;

    private final OrderRepository orderRepository;

    public PaymentService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public PaymentResponseDTO createPreference(PaymentPreferenceRequestDTO requestDTO) throws Exception {
        MercadoPagoConfig.setAccessToken(accessToken);

        PreferenceClient client = new PreferenceClient();

        // Monta os itens da preferência
        var items = requestDTO.items().stream()
                .map(i -> PreferenceItemRequest.builder()
                        .title(i.title())
                        .quantity(i.quantity())
                        .unitPrice(i.unitPrice())
                        .currencyId("BRL")
                        .build())
                .collect(Collectors.toList());

        // Monta payer (apenas email é obrigatório para sandbox)
        var payer = PreferencePayerRequest.builder()
                .email(requestDTO.user().email())
                .build();

        // Cria a preferência
        var preferenceRequest = PreferenceCreateRequest.builder()
                .items(items)
                .payer(payer)
                .build();

        var preference = client.create(preferenceRequest);

        // Salva no banco
        Order order = new Order();
        order.setUserName(requestDTO.user().name());
        order.setUserEmail(requestDTO.user().email());
        order.setUserCpf(requestDTO.user().cpf());
        order.setAddress(requestDTO.user().address());
        order.setPhoneNumber(requestDTO.user().phoneNumber());
        order.setItemsJson(items.toString());
        order.setTotalAmount(
                requestDTO.items().stream()
                        .map(i -> i.unitPrice().multiply(BigDecimal.valueOf(i.quantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
        );
        order.setStatus("pending");
        order.setPaymentId(String.valueOf(preference.getId()));
        order.setSandboxUrl(preference.getSandboxInitPoint());
        order.setProductionUrl(preference.getInitPoint());

        orderRepository.save(order);

        return new PaymentResponseDTO(
                String.valueOf(preference.getId()),
                order.getTotalAmount(),
                order.getStatus(),
                preference.getSandboxInitPoint(),
                preference.getInitPoint()
        );
    }
}
