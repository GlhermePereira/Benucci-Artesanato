package br.edu.fatecpg.BenucciArtesanato.record.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ProductPageDTO {
    private List<ProductDTO> content;
    private int page;
    private int size;
    private int totalPages;
    private long totalElements;
}
