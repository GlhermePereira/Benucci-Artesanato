package br.edu.fatecpg.BenucciArtesanato.record.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de Entrada (Input DTO) para a criação de novas Categorias (POST /categories).
 * Este Record imutável contém apenas os dados essenciais para o registro no sistema,
 * garantindo a validação mínima (NotBlank e Size) antes de chegar ao Service.
 */
public record CategoryInputDTO(

        // Validação: Garante que o nome não é nulo e contém algum caractere não-branco.
        @NotBlank(message = "O nome da categoria é obrigatório.")
        // Validação: Limita o tamanho do nome para 100 caracteres.
        @Size(max = 100, message = "O nome não pode exceder 100 caracteres.")
        String name,

        // Validação: A descrição é opcional (não possui @NotBlank), mas tem limite de tamanho.
        @Size(max = 500, message = "A descrição não pode exceder 500 caracteres.")
        String description
) {
}