package br.edu.fatecpg.BenucciArtesanato.record.dto;

import java.math.BigDecimal;

public record ItemDTO(
        String title,
        int quantity,
        BigDecimal unitPrice
) {}