package br.edu.fatecpg.BenucciArtesanato.record.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubcategoryThemeAssignDTO {
    private Long categoryId;
    private Long subcategoryId;
    private List<Long> themeIds; // Lista de IDs de temas a associar
}
