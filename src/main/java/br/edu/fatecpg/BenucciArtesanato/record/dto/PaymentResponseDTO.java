package br.edu.fatecpg.BenucciArtesanato.record.dto;

import java.math.BigDecimal;

public record PaymentResponseDTO(
        String preferenceId,
        BigDecimal amount,
        String orderStatus,
        String sandboxLink,
        String initPoint
) {}
