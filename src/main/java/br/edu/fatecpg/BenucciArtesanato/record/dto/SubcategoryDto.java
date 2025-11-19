package br.edu.fatecpg.BenucciArtesanato.record.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubcategoryDto {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private Long categoryId;
    private List<ThemeDto> themes;
}
