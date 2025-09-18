package br.edu.fatecpg.BenucciArtesanato.record.dto;

import java.math.BigDecimal;

public record PaymentResponseDTO(
        String mercadoPagoId,
        double amount,
        String status,
        String qrCode,
        String qrCodeBase64
) {
    // Construtor secundário que recebe BigDecimal para facilitar conversão
    public PaymentResponseDTO(String mercadoPagoId, BigDecimal amount, String status, String qrCode, String qrCodeBase64) {
        this(
                mercadoPagoId,
                amount != null ? amount.doubleValue() : 0.0,
                status,
                qrCode,
                qrCodeBase64
        );
    }
}
