package br.edu.fatecpg.BenucciArtesanato.controller;

import br.edu.fatecpg.BenucciArtesanato.model.Product;
import br.edu.fatecpg.BenucciArtesanato.model.ProductImage;
import br.edu.fatecpg.BenucciArtesanato.record.dto.ProductDTO;
import br.edu.fatecpg.BenucciArtesanato.record.dto.ProductMapper;
import br.edu.fatecpg.BenucciArtesanato.record.dto.ProductPageDTO;
import br.edu.fatecpg.BenucciArtesanato.record.dto.UpdateProductDTO;
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

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {


    private final ProductService productService;
    private final SupabaseService supabaseService;
    private final ObjectMapper objectMapper;
    private final ProductMapper productMapper; // <--- campo

    @Autowired
    public ProductController(ProductService productService,
                             SupabaseService supabaseService,
                             ObjectMapper objectMapper,
                             ProductMapper productMapper) {
        this.productService = productService;
        this.supabaseService = supabaseService;
        this.objectMapper = objectMapper;
        this.productMapper = productMapper; // <--- inicializa
    }
    @Operation(summary = "Listar produtos com paginação", description = "Retorna uma lista paginada de produtos cadastrados.")
    @ApiResponse(responseCode = "200", description = "Lista de produtos retornada com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductPageDTO.class)))
    @GetMapping
    public ResponseEntity<ProductPageDTO> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        ProductPageDTO pagedProducts = productService.getPaginatedProducts(page, size);
        return ResponseEntity.ok(pagedProducts);
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
    }@PreAuthorize("hasRole('ADMIN')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createProduct(
            @RequestPart("product") String productJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        try {
            // Desserializa manualmente o JSON para ProductDTO
            ProductDTO dto = objectMapper.readValue(productJson, ProductDTO.class);

            productService.createProduct(dto, images);

            return ResponseEntity.ok("Produto criado com sucesso!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Erro: " + e.getMessage());
        }
    }

    @Operation(summary = "Atualizar produto", description = "Atualiza um produto existente pelo ID (somente ADMIN).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produto atualizado com sucesso", content = @Content(schema = @Schema(implementation = ProductDTO.class))),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductDTO> updateProduct(
            @PathVariable Long id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) BigDecimal price,
            @RequestParam(required = false) Integer stock,
            @RequestParam(required = false) Long subcategoryId,
            @RequestParam(required = false) List<Long> themeIds,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        UpdateProductDTO dto = new UpdateProductDTO();
        dto.setName(name);
        dto.setDescription(description);
        dto.setPrice(price);
        dto.setStock(stock);
        dto.setSubcategoryId(subcategoryId);
        dto.setThemeIds(themeIds);

        Product updated = productService.updateProduct(id, dto, images);
        ProductDTO responseDTO = productMapper.toDTO(updated);

        return ResponseEntity.ok(responseDTO);
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