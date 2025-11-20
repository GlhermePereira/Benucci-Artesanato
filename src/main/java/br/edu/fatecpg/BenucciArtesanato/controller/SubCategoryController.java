package br.edu.fatecpg.BenucciArtesanato.controller;

import br.edu.fatecpg.BenucciArtesanato.record.dto.SubcategoryDto;
import br.edu.fatecpg.BenucciArtesanato.service.SubCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/categories")
public class SubCategoryController {

    private final SubCategoryService subCategoryService;

    @Operation(summary = "Listar Subcategorias de uma Categoria")
    @GetMapping("/{categoryId}/subcategories")
    public ResponseEntity<List<SubcategoryDto>> getSubcategoriesByCategory(
            @PathVariable Long categoryId
    ) {
        return ResponseEntity.ok(subCategoryService.getSubcategoriesByCategory(categoryId));
    }

    @Operation(summary = "Criar Subcategoria")
    @PostMapping("/{categoryId}/subcategories")
    public ResponseEntity<SubcategoryDto> createSubcategory(
            @PathVariable Long categoryId,
            @RequestBody SubcategoryDto dto
    ) {
        return ResponseEntity.ok(subCategoryService.createSubcategory(categoryId, dto));
    }

    @Operation(summary = "Atualizar Subcategoria")
    @PutMapping("/{categoryId}/subcategories/{subcategoryId}")
    public ResponseEntity<SubcategoryDto> updateSubcategory(
            @PathVariable Long categoryId,
            @PathVariable Long subcategoryId,
            @RequestBody SubcategoryDto dto
    ) {
        return ResponseEntity.ok(subCategoryService.updateSubcategory(categoryId, subcategoryId, dto));
    }

    @Operation(summary = "Excluir Subcategoria")
    @DeleteMapping("/{categoryId}/subcategories/{subcategoryId}")
    public ResponseEntity<Void> deleteSubcategory(
            @PathVariable Long categoryId,
            @PathVariable Long subcategoryId
    ) {
        subCategoryService.deleteSubcategory(categoryId, subcategoryId);
        return ResponseEntity.noContent().build();
    }
}
