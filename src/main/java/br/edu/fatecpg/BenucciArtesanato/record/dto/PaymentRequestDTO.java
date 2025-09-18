// src/main/java/br/edu/fatecpg/BenucciArtesanato/dto/PaymentRequestDTO.java
package br.edu.fatecpg.BenucciArtesanato.record.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record PaymentRequestDTO(
        double amount,
        String description,
        @NotNull PayerDTO payer
) {}
