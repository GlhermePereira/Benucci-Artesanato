package br.edu.fatecpg.BenucciArtesanato.record.dto;

import br.edu.fatecpg.BenucciArtesanato.model.Category;

public record CategorySimpleDTO(
        Long id,
        String name
) {
    public static CategorySimpleDTO fromEntity(Category category) {
        return new CategorySimpleDTO(
                category.getId(),
                category.getName()
        );
    }
}