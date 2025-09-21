package br.edu.fatecpg.BenucciArtesanato.record.dto;

import java.util.List;

public record PaymentPreferenceRequestDTO(
        List<ItemDTO> items,
        UserDTO user,
        String paymentMethodId,

        String deliveryType,
        String deliveryAddress
) {}
