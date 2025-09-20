package br.edu.fatecpg.BenucciArtesanato.record.dto;

import java.util.List;

public record PaymentPreferenceRequestDTO(
        List<ItemDTO> items,
        UserDTO user,
        String paymentMethodId // opcional, para futura escolha de método
) {}

