package br.edu.fatecpg.BenucciArtesanato.record.dto;
import jakarta.validation.constraints.NotNull;

public record PayerDTO(
        @NotNull String firstName,
        @NotNull String lastName,
        @NotNull String email,
        @NotNull PayerIdentificationDTO identification
) {}