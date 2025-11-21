package br.edu.fatecpg.BenucciArtesanato.record.dto;

import br.edu.fatecpg.BenucciArtesanato.model.Product;
import br.edu.fatecpg.BenucciArtesanato.model.ProductImage;
import org.springframework.stereotype.Component;@Component
public class ProductMapper {

    // Mapeia produto para DTO de resposta (com tudo que precisa exibir)
    public ProductDTO toDTO(Product product) {
        ProductDTO dto = new ProductDTO();

        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStock(product.getStock());

        dto.setSubcategoryId(product.getSubcategory().getId());
        dto.setSubcategoryName(product.getSubcategory().getName());

        dto.setCategoryId(product.getSubcategory().getCategory().getId());
        dto.setCategoryName(product.getSubcategory().getCategory().getName());

        dto.setImageUrls(
                product.getImages().stream()
                        .map(ProductImage::getImageUrl)
                        .toList()
        );

        if (!product.getImages().isEmpty()) {
            ProductImage firstImage = product.getImages().iterator().next();
            dto.setMainImageUrl(firstImage.getImageUrl());
        }

        dto.setThemeIds(
                product.getProductThemes().stream()
                        .map(pt -> pt.getTheme().getId())
                        .toList()
        );

        dto.setThemeNames(
                product.getProductThemes().stream()
                        .map(pt -> pt.getTheme().getName())
                        .toList()
        );

        dto.setCreatedAt(product.getCreatedAt().toLocalDateTime());
        dto.setUpdatedAt(product.getUpdatedAt().toLocalDateTime());

        return dto;
    }

    // NÃO mapeia para UpdateProductDTO — esse DTO é apenas para receber dados de atualização
    // Se quiser popular o form de edição, você só precisa dos campos editáveis:
    public UpdateProductDTO toUpdateDTO(Product product) {
        UpdateProductDTO dto = new UpdateProductDTO();

        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStock(product.getStock());
        dto.setSubcategoryId(product.getSubcategory().getId());
        dto.setThemeIds(
                product.getProductThemes().stream()
                        .map(pt -> pt.getTheme().getId())
                        .toList()
        );

        return dto;
    }
}
