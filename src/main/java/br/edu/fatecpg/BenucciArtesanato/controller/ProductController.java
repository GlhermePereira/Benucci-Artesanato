package br.edu.fatecpg.BenucciArtesanato.controller;

import br.edu.fatecpg.BenucciArtesanato.record.dto.ProductDTO;
import br.edu.fatecpg.BenucciArtesanato.service.ProductService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/products")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    // ✅ LISTAR todos os produtos (público)
    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        List<ProductDTO> products = productService.getAllDTO();
        return ResponseEntity.ok(products);
    }

    // ✅ VER um produto específico (público)
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProduct(@PathVariable Long id) {
        try {
            ProductDTO dto = productService.getProductDTOById(id);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 🔒 CRIAR produto (somente ADMIN)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createProduct(@RequestBody ProductDTO productDTO) {
        try {
            normalizeCategoryId(productDTO);
            ProductDTO created = productService.createProduct(productDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "invalid_product_payload",
                    "message", e.getMessage()
            ));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createProductWithUpload(@RequestPart("product") String productJson,
                                                                  @RequestPart(value = "imageFile", required = false) MultipartFile imageFile) {
        try {
            if (logger.isInfoEnabled()) {
                logger.info("createProductWithUpload -> raw product JSON: {}", productJson);
            }
            ProductDTO productDTO = parseProduct(productJson);
            if (logger.isInfoEnabled()) {
                logger.info("createProductWithUpload -> payload categoryId={}, nestedCategoryId={}",
                        productDTO.getCategoryId(),
                        productDTO.getCategory() != null ? productDTO.getCategory().getId() : null);
            }
            ProductDTO created = productService.createProduct(productDTO, imageFile);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "invalid_product_payload",
                    "message", e.getMessage()
            ));
        }
    }

    // 🔒 ATUALIZAR produto (somente ADMIN)
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody ProductDTO productDTO) {
        try {
            normalizeCategoryId(productDTO);
            ProductDTO updated = productService.updateProduct(id, productDTO);
            return ResponseEntity.ok(updated);
    } catch (IllegalArgumentException e) {
        HttpStatus status = e.getMessage() != null && e.getMessage().toLowerCase().contains("not found")
            ? HttpStatus.NOT_FOUND
            : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(Map.of(
            "error", status == HttpStatus.NOT_FOUND ? "product_not_found" : "invalid_product_payload",
            "message", e.getMessage()
        ));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProductWithUpload(@PathVariable Long id,
                                                                  @RequestPart("product") String productJson,
                                                                  @RequestPart(value = "imageFile", required = false) MultipartFile imageFile) {
        try {
            ProductDTO productDTO = parseProduct(productJson);
            ProductDTO updated = productService.updateProduct(id, productDTO, imageFile);
            return ResponseEntity.ok(updated);
    } catch (IllegalArgumentException e) {
        HttpStatus status = e.getMessage() != null && e.getMessage().toLowerCase().contains("not found")
            ? HttpStatus.NOT_FOUND
            : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(Map.of(
            "error", status == HttpStatus.NOT_FOUND ? "product_not_found" : "invalid_product_payload",
            "message", e.getMessage()
        ));
        }
    }

    // 🔒 EXCLUIR produto (somente ADMIN)
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        boolean removed = productService.deleteProduct(id);
        if (!removed) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok("Product removed successfully!");
    }

    @GetMapping(value = "/{id}/image")
    public ResponseEntity<byte[]> getProductImage(@PathVariable Long id) {
        Optional<ProductService.ProductImageData> imageData = productService.getProductImage(id);
        if (imageData.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        ProductService.ProductImageData data = imageData.get();
        MediaType mediaType = data.contentType() != null
                ? MediaType.parseMediaType(data.contentType())
        : MediaType.IMAGE_JPEG;

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + (data.fileName() != null ? data.fileName() : "image") + "\"")
                .contentType(mediaType)
                .body(data.data());
    }

    private ProductDTO parseProduct(String productJson) {
        try {
            JsonNode root = objectMapper.readTree(productJson);
            if (logger.isInfoEnabled()) {
                logger.info("parseProduct -> raw tree: {}", root);
            }

            ProductDTO dto = objectMapper.treeToValue(root, ProductDTO.class);

            if (dto.getCategory() == null && root.has("category") && !root.get("category").isNull()) {
                ProductDTO.CategoryDTO categoryNode = objectMapper.treeToValue(root.get("category"), ProductDTO.CategoryDTO.class);
                dto.setCategory(categoryNode);
            }

            if (dto.getCategoryId() == null) {
                if (root.hasNonNull("categoryId")) {
                    dto.setCategoryId(root.get("categoryId").asLong());
                } else if (root.has("category") && root.get("category").hasNonNull("id")) {
                    dto.setCategoryId(root.get("category").get("id").asLong());
                }
            }

            normalizeCategoryId(dto);
            if (logger.isInfoEnabled()) {
                logger.info("parseProduct -> after normalization DTO={}", dto);
            }
            return dto;
        } catch (JsonProcessingException e) {
            String detailed = e.getOriginalMessage() != null ? e.getOriginalMessage() : e.getMessage();
            throw new IllegalArgumentException("Invalid product payload: " + detailed, e);
        }
    }

    private void normalizeCategoryId(ProductDTO dto) {
        if (dto != null && dto.getCategoryId() == null && dto.getCategory() != null) {
            dto.setCategoryId(dto.getCategory().getId());
            if (logger.isDebugEnabled()) {
                logger.debug("normalizeCategoryId -> categoryId populated from nested category");
            }
        } else if (dto != null && logger.isDebugEnabled()) {
            logger.debug("normalizeCategoryId -> categoryId already present: {}", dto.getCategoryId());
        }
    }
}
