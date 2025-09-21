package br.edu.fatecpg.BenucciArtesanato.service;


import br.edu.fatecpg.BenucciArtesanato.model.Product;
import br.edu.fatecpg.BenucciArtesanato.model.Category;
import br.edu.fatecpg.BenucciArtesanato.record.dto.ProductDTO;
import br.edu.fatecpg.BenucciArtesanato.repository.CategoryRepository;
import br.edu.fatecpg.BenucciArtesanato.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Autowired
    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    // Buscar produto por ID

    public ProductDTO getProductDTOById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setCategoryId(product.getCategory().getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setImageUrl(product.getImageUrl());
        dto.setPrice(product.getPrice());
        dto.setStock(product.getStock());

        return dto;
    }

    public List<ProductDTO> getAllDTO() {
        return productRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setCategoryId(product.getCategory().getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setImageUrl(product.getImageUrl());
        dto.setPrice(product.getPrice());
        dto.setStock(product.getStock());
        return dto;
    }


    // Criar novo produto
    public Product createProduct(ProductDTO dto) {
        Optional<Category> categoryOpt = categoryRepository.findById(dto.getCategoryId());
        if (categoryOpt.isEmpty()) {
            throw new IllegalArgumentException("Category not found with id: " + dto.getCategoryId());
        }

        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setImageUrl(dto.getImageUrl());
        product.setCreatedAt(LocalDateTime.now());
        product.setCategory(categoryOpt.get());

        return productRepository.save(product);
    }

    // Atualizar produto
    public Product updateProduct(Long id, ProductDTO dto) {
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isEmpty()) return null;

        Product product = productOpt.get();

        if (dto.getCategoryId() != null) {
            Optional<Category> categoryOpt = categoryRepository.findById(dto.getCategoryId());
            if (categoryOpt.isEmpty()) {
                throw new IllegalArgumentException("Category not found with id: " + dto.getCategoryId());
            }
            product.setCategory(categoryOpt.get());
        }

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setImageUrl(dto.getImageUrl());

        return productRepository.save(product);
    }

    // Remover produto
    public boolean deleteProduct(Long id) {
        if (!productRepository.existsById(id)) return false;
        productRepository.deleteById(id);
        return true;
    }
}