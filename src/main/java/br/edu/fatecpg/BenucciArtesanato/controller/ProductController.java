package br.edu.fatecpg.BenucciArtesanato.controller;

import br.edu.fatecpg.BenucciArtesanato.model.Product;
import br.edu.fatecpg.BenucciArtesanato.model.ProductImage;
import br.edu.fatecpg.BenucciArtesanato.record.dto.ProductDTO;
import br.edu.fatecpg.BenucciArtesanato.service.ProductService;
import br.edu.fatecpg.BenucciArtesanato.service.SupabaseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;
    @Autowired
    private SupabaseService supabaseService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Operation(summary = "Listar todos os produtos", description = "Retorna uma lista de todos os produtos cadastrados.")
    @ApiResponse(responseCode = "200", description = "Lista de produtos retornada com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductDTO.class)))
    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        List<ProductDTO> products = productService.getAllDTO();
        return ResponseEntity.ok(products);
    }

    @Operation(summary = "Ver um produto específico", description = "Retorna os detalhes de um produto pelo seu ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produto encontrado", content = @Content(schema = @Schema(implementation = ProductDTO.class))),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProduct(
            @Parameter(description = "ID do produto a ser buscado", required = true) @PathVariable Long id) {
        try {
            ProductDTO dto = productService.getProductDTOById(id);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Criar um novo produto", description = "Cria um produto com imagem (somente usuários ADMIN).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produto criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro de validação nos dados do produto"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<String> createProduct(
            @Parameter(description = "JSON do produto")
            @RequestPart("product") String productJson,

            @Parameter(description = "Lista de imagens do produto (opcional)")
            @RequestPart(value = "images", required = false)
            List<MultipartFile> images
    ) {
        try {
            // Converte JSON → DTO
            ProductDTO productDTO = objectMapper.readValue(productJson, ProductDTO.class);

            // Chama o service corretamente
            productService.createProduct(productDTO, images);

            return ResponseEntity.ok("Produto criado com sucesso!");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erro: " + e.getMessage());
        }
    }



//    @Operation(summary = "Atualizar produto", description = "Atualiza um produto existente pelo ID (somente ADMIN).")
//    @ApiResponses({
//            @ApiResponse(responseCode = "200", description = "Produto atualizado com sucesso", content = @Content(schema = @Schema(implementation = ProductDTO.class))),
//            @ApiResponse(responseCode = "404", description = "Produto não encontrado")
//    })
//    @PreAuthorize("hasRole('ADMIN')")
//    @PutMapping("/{id}")
//    public ResponseEntity<ProductDTO> updateProduct(
//            @Parameter(description = "ID do produto a ser atualizado", required = true)
//            @PathVariable Long id,
//
//            @io.swagger.v3.oas.annotations.parameters.RequestBody(
//                    description = "Dados do produto para atualização",
//                    required = true,
//                    content = @Content(schema = @Schema(implementation = ProductDTO.class))
//            )
//            @RequestBody ProductDTO productDTO
//    ) {
//        try {
//            Product updated = productService.updateProduct(id, productDTO);
//
//            // Mapear entidade para DTO antes de retornar
//            ProductDTO responseDTO = mapToDTO(updated);
//
//            return ResponseEntity.ok(responseDTO);
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.notFound().build();
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(500).build();
//        }
//    }
    private ProductDTO mapToDTO(Product product) {

        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStock(product.getStock());

        // ===== SUBCATEGORY =====
        if (product.getSubcategory() != null) {
            dto.setSubcategoryId(product.getSubcategory().getId());
            dto.setSubcategoryName(product.getSubcategory().getName());

            // ===== CATEGORY (vem da SUBCATEGORY) =====
            if (product.getSubcategory().getCategory() != null) {
                dto.setCategoryId(product.getSubcategory().getCategory().getId());
                dto.setCategoryName(product.getSubcategory().getCategory().getName());
            }
        }

        // ===== IMAGENS =====
        List<String> imageUrls = product.getImages()
                .stream()
                .map(ProductImage::getImageUrl)
                .toList();

        dto.setImageUrls(imageUrls);

        // Primeira imagem como capa (se quiser)
        if (!imageUrls.isEmpty()) {
            dto.setMainImageUrl(imageUrls.get(0));
        }

        // Datas
        dto.setCreatedAt(product.getCreatedAt().toLocalDateTime());
        dto.setUpdatedAt(product.getUpdatedAt().toLocalDateTime());

        return dto;
    }


    @Operation(summary = "Excluir produto", description = "Remove um produto pelo ID (somente ADMIN).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produto removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(
            @Parameter(description = "ID do produto a ser removido", required = true) @PathVariable Long id) {
        boolean removed = productService.deleteProduct(id);
        if (!removed) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok("Product removed successfully!");
    }
}