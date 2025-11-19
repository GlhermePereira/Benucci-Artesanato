package br.edu.fatecpg.BenucciArtesanato.service;

import br.edu.fatecpg.BenucciArtesanato.model.Category;
import br.edu.fatecpg.BenucciArtesanato.model.Product;
import br.edu.fatecpg.BenucciArtesanato.record.dto.ProductDTO;
import br.edu.fatecpg.BenucciArtesanato.repository.CategoryRepository;
import br.edu.fatecpg.BenucciArtesanato.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import net.coobird.thumbnailator.Thumbnails;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private SupabaseService supabaseService;
    @Autowired
    private CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<ProductDTO> getAllDTO() {
        List<Product> products = productRepository.findAll();
        return products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public ProductDTO getProductDTOById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));
        return convertToDTO(product);
    }
    public Product createProduct(ProductDTO productDTO, MultipartFile image) {
        try {
            String finalImageUrl;

            // ✅ Caso 1: imagem enviada via upload local
            if (image != null && !image.isEmpty()) {

                validateFile(image);

                byte[] processedImage = resizeImage(image);
                finalImageUrl = supabaseService.uploadImage(
                        image.getOriginalFilename(),
                        processedImage
                );

            } else {
                // ✅ Caso 2: sem upload → usar imageUrl que veio no JSON
                finalImageUrl = productDTO.getImageUrl();
            }


            // ✅ Criar produto
            Product product = new Product();
            product.setName(productDTO.getName());
            product.setDescription(productDTO.getDescription());
            product.setPrice(productDTO.getPrice());
            product.setStock(productDTO.getStock());
            product.setImageUrl(finalImageUrl);


            return productRepository.save(product);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (IOException e) {
            throw new RuntimeException("Erro ao processar a imagem", e);
        } catch (Exception e) {
            throw new RuntimeException("Erro interno ao criar produto", e);
        }
    }

    private void validateFile(MultipartFile file) {
        List<String> allowedTypes = List.of(
                "image/png",
                "image/jpeg",
                "image/jpg",
                "image/webp",
                "application/pdf"
        );

        String contentType = file.getContentType();
        if (contentType == null || !allowedTypes.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Formato de arquivo não permitido: " + contentType);
        }

        long maxSize = 5 * 1024 * 1024; // 5 MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("Arquivo muito grande. Máximo permitido: 5MB");
        }
    }


    private byte[] resizeImage(MultipartFile file) throws IOException {
        String contentType = file.getContentType();

        // Apenas imagens suportadas pelo Thumbnailator
        if (contentType != null &&
                (contentType.equals("image/jpeg") ||
                        contentType.equals("image/png") ||
                        contentType.equals("image/bmp") ||
                        contentType.equals("image/gif"))) {

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Thumbnails.of(file.getInputStream())
                    .size(800, 800)
                    .outputQuality(0.8)
                    .toOutputStream(outputStream);
            return outputStream.toByteArray();
        }
        // WebP, PDF ou outros formatos -> não redimensiona
        else if (contentType != null && contentType.startsWith("image/")) {
            // Você pode aceitar WebP sem redimensionar
            return file.getBytes();
        }

        throw new IllegalArgumentException("Formato de arquivo não suportado. Apenas imagens válidas são permitidas.");
    }



    @Transactional
    public Product updateProduct(Long id, ProductDTO dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        // Atualizar campos
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setImageUrl(dto.getImageUrl());

        // Atualizar categoria se fornecida
        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found"));
            product.setCategory(category);
        }

        return productRepository.save(product);
    }

    @Transactional
    public boolean deleteProduct(Long id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Método auxiliar para converter Product -> ProductDTO
    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStock(product.getStock());
        dto.setImageUrl(product.getImageUrl());

        // Preencher categoryId e objeto category
        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());

            ProductDTO.CategoryDTO categoryDTO = new ProductDTO.CategoryDTO();
            categoryDTO.setId(product.getCategory().getId());
            categoryDTO.setName(product.getCategory().getName());
            dto.setCategory(categoryDTO);
        }

        return dto;
    }
}