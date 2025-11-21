package br.edu.fatecpg.BenucciArtesanato.record.dto;


import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class UpdateProductDTO {

    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private Long subcategoryId;
    private List<Long> themeIds;
}
