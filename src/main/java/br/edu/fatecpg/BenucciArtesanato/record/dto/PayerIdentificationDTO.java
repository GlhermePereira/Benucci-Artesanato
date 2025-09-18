package br.edu.fatecpg.BenucciArtesanato.record.dto;

import jakarta.validation.constraints.NotNull;

public record PayerIdentificationDTO(
        @NotNull String type,
        @NotNull String number
) {}