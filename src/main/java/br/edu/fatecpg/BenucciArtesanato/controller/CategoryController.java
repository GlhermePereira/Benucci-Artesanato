package br.edu.fatecpg.BenucciArtesanato.controller;

import br.edu.fatecpg.BenucciArtesanato.record.dto.CategoryDto;
import br.edu.fatecpg.BenucciArtesanato.record.dto.SubcategoryDto;
import br.edu.fatecpg.BenucciArtesanato.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    // ================================
    // CATEGORY ENDPOINTS
    // ================================

    @Operation(summary = "Listar Categorias",
            description = "Retorna todas as categorias com suas subcategorias")
    @GetMapping
    public ResponseEntity<List<CategoryDto>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @Operation(summary = "Buscar Categoria por ID")
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDto> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @Operation(summary = "Criar Categoria")
    @PostMapping
    public ResponseEntity<CategoryDto> createCategory(@RequestBody CategoryDto dto) {
        return ResponseEntity.ok(categoryService.createCategory(dto));
    }

    @Operation(summary = "Atualizar Categoria")
    @PutMapping("/{id}")
    public ResponseEntity<CategoryDto> updateCategory(@PathVariable Long id,
                                                      @RequestBody CategoryDto dto) {
        return ResponseEntity.ok(categoryService.updateCategory(id, dto));
    }

    @Operation(summary = "Excluir Categoria")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }


    // ================================
    // SUBCATEGORY ENDPOINTS
    // ================================

    @Operation(summary = "Listar Subcategorias da Categoria")
    @GetMapping("/{categoryId}/subcategories")
    public ResponseEntity<List<SubcategoryDto>> getSubcategoriesByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(categoryService.getSubcategoriesByCategory(categoryId));
    }

    @Operation(summary = "Criar Subcategoria")
    @PostMapping("/{categoryId}/subcategories")
    public ResponseEntity<SubcategoryDto> createSubcategory(@PathVariable Long categoryId,
                                                            @RequestBody SubcategoryDto dto) {
        return ResponseEntity.ok(categoryService.createSubcategory(categoryId, dto));
    }

    @Operation(summary = "Atualizar Subcategoria")
    @PutMapping("/{categoryId}/subcategories/{subcategoryId}")
    public ResponseEntity<SubcategoryDto> updateSubcategory(
            @PathVariable Long categoryId,
            @PathVariable Long subcategoryId,
            @RequestBody SubcategoryDto dto
    ) {
        return ResponseEntity.ok(categoryService.updateSubcategory(categoryId, subcategoryId, dto));
    }

    @Operation(summary = "Excluir Subcategoria")
    @DeleteMapping("/{categoryId}/subcategories/{subcategoryId}")
    public ResponseEntity<Void> deleteSubcategory(
            @PathVariable Long categoryId,
            @PathVariable Long subcategoryId
    ) {
        categoryService.deleteSubcategory(categoryId, subcategoryId);
        return ResponseEntity.noContent().build();
    }
}
