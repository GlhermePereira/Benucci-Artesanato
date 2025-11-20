package br.edu.fatecpg.BenucciArtesanato.record.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubcategoryThemeDto {
    private Long subcategoryId;
    private List<Long> themeIds;
}
