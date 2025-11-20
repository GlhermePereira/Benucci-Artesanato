package br.edu.fatecpg.BenucciArtesanato.record.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThemeInputDTO {
    private Long id;
    private String name;
    private String description;
    private String slug;

}
