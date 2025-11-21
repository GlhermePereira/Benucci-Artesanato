package br.edu.fatecpg.BenucciArtesanato.record.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ProductDTO {

    private Long id;

    private String name;
    private String description;

    private BigDecimal price;
    private Integer stock;

    private List<String> imageUrls;
    private String mainImageUrl;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    private Long subcategoryId;
    private String subcategoryName;


    private Long categoryId;
    private String categoryName;

    private List<Long> themeIds;
    private List<String> themeNames;
}
