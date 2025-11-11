package br.edu.fatecpg.BenucciArtesanato.record.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubcategoryDto {
    private Long id;
    private Long categoryId;
    private String name;
    private String slug;
    private String description;
}
