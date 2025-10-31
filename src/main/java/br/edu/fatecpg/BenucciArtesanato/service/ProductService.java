package br.edu.fatecpg.BenucciArtesanato.service;

import br.edu.fatecpg.BenucciArtesanato.model.Category;
import br.edu.fatecpg.BenucciArtesanato.model.Product;
import br.edu.fatecpg.BenucciArtesanato.record.dto.ProductDTO;
import br.edu.fatecpg.BenucciArtesanato.repository.CategoryRepository;
import br.edu.fatecpg.BenucciArtesanato.repository.ProductRepository;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    @Autowired
    private ProductRepository productRepository;

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

    @Transactional
    public ProductDTO createProduct(ProductDTO dto) {
        return createProduct(dto, null);
    }

    @Transactional
    public ProductDTO createProduct(ProductDTO dto, MultipartFile imageFile) {
    Long categoryId = resolveCategoryId(dto);
    if (logger.isInfoEnabled()) {
        logger.info("createProduct -> resolved categoryId={}, nestedCategoryId={} (payload)",
                categoryId,
                dto.getCategory() != null ? dto.getCategory().getId() : null);
    }
    Category category = categoryRepository.findById(categoryId)
        .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + categoryId));

        Product product = new Product();
        applyBasicFields(product, dto);
        product.setCategory(category);

        boolean hasUpload = hasUpload(imageFile);
        if (hasUpload) {
            storeImage(product, imageFile);
            product.setImageUrl(null);
        } else {
            applyImageUrl(product, dto.getImageUrl());
        }

        product = productRepository.save(product);

        if (hasUpload) {
            product.setImageUrl(buildImageUrl(product.getId()));
            product = productRepository.save(product);
        }

        return convertToDTO(product);
    }

    @Transactional
    public ProductDTO updateProduct(Long id, ProductDTO dto) {
        return updateProduct(id, dto, null);
    }

    @Transactional
    public ProductDTO updateProduct(Long id, ProductDTO dto, MultipartFile imageFile) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        applyBasicFields(product, dto);

    if (dto.getCategoryId() != null || dto.getCategory() != null) {
        Long categoryId = resolveCategoryId(dto);
        if (logger.isInfoEnabled()) {
            logger.info("updateProduct -> resolved categoryId={}, nestedCategoryId={} (payload)",
                    categoryId,
                    dto.getCategory() != null ? dto.getCategory().getId() : null);
        }
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new IllegalArgumentException("Category not found"));
            product.setCategory(category);
        }

        boolean hasUpload = hasUpload(imageFile);
        if (hasUpload) {
            storeImage(product, imageFile);
            product.setImageUrl(buildImageUrl(product.getId()));
        } else if (dto.getImageUrl() != null) {
            applyImageUrl(product, dto.getImageUrl());
        }

        Product saved = productRepository.save(product);
        return convertToDTO(saved);
    }

    @Transactional
    public boolean deleteProduct(Long id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional(readOnly = true)
    public Optional<ProductImageData> getProductImage(Long id) {
        return productRepository.findById(id)
                .filter(product -> product.getImageData() != null && product.getImageData().length > 0)
                .map(product -> new ProductImageData(
                        product.getImageData(),
                        product.getImageContentType(),
                        product.getImageFileName()
                ));
    }

    // Método auxiliar para converter Product -> ProductDTO
    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStock(product.getStock());
        String imageUrl = product.getImageUrl();
        if (imageUrl == null && product.getImageData() != null && product.getImageData().length > 0) {
            imageUrl = buildImageUrl(product.getId());
        }
        dto.setImageUrl(imageUrl);

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

    private void applyBasicFields(Product product, ProductDTO dto) {
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
    }

    private boolean hasUpload(MultipartFile imageFile) {
        return imageFile != null && !imageFile.isEmpty();
    }

    private void storeImage(Product product, MultipartFile imageFile) {
        try {
            product.setImageContentType(imageFile.getContentType());
            product.setImageFileName(imageFile.getOriginalFilename());
            product.setImageData(imageFile.getBytes());
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to process product image", e);
        }
    }

    private void applyImageUrl(Product product, String imageUrl) {
        if (imageUrl == null) {
            return;
        }

        String trimmed = imageUrl.trim();
        if (trimmed.isEmpty()) {
            product.setImageUrl(null);
            clearStoredImage(product);
        } else {
            product.setImageUrl(trimmed);
            clearStoredImage(product);
        }
    }

    private void clearStoredImage(Product product) {
        product.setImageData(null);
        product.setImageContentType(null);
        product.setImageFileName(null);
    }

    private String buildImageUrl(Long productId) {
        return "/products/" + productId + "/image";
    }

    public record ProductImageData(byte[] data, String contentType, String fileName) {}

    private Long resolveCategoryId(ProductDTO dto) {
        Long categoryId = dto.getCategoryId();
        if (categoryId == null && dto.getCategory() != null) {
            categoryId = dto.getCategory().getId();
        }
        if (logger.isDebugEnabled()) {
            logger.debug("resolveCategoryId -> categoryId={}, nestedCategory={} ",
                    categoryId,
                    dto.getCategory());
        }
        if (categoryId == null) {
            Long nestedId = dto.getCategory() != null ? dto.getCategory().getId() : null;
            throw new IllegalArgumentException("Category id is required (payload categoryId=" + dto.getCategoryId()
                    + ", nestedCategoryId=" + nestedId + ")");
        }
        return categoryId;
    }
}