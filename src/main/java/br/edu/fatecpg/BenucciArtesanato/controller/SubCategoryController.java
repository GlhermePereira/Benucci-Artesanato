package br.edu.fatecpg.BenucciArtesanato.controller;

import br.edu.fatecpg.BenucciArtesanato.record.dto.SubcategoryDto;
import br.edu.fatecpg.BenucciArtesanato.service.SubCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/categories")
public class SubCategoryController {


    private final SubCategoryService subCategoryService;

    // =============================
// LISTAR SUBCATEGORIAS POR CATEGORIA
// =============================
    @Operation(summary = "Listar Subcategorias de uma Categoria",
            description = "Retorna todas as subcategorias pertencentes a uma categoria existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de subcategorias retornada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada")
    })
    @GetMapping("/{categoryId}/subcategories")
    public ResponseEntity<List<SubcategoryDto>> getSubcategoriesByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(subCategoryService.getSubcategoriesByCategory(categoryId));
    }

    // =============================
// CRIAR SUBCATEGORIA
// =============================
    @Operation(summary = "Criar Subcategoria",
            description = "Cria uma nova subcategoria dentro de uma categoria existente. O nome deve ser único dentro da categoria.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Subcategoria criada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada"),
            @ApiResponse(responseCode = "409", description = "Já existe uma subcategoria com este nome nesta categoria")
    })
    @PostMapping("/{categoryId}/subcategories")
    public ResponseEntity<SubcategoryDto> createSubcategory(
            @PathVariable Long categoryId,
            @RequestBody SubcategoryDto dto
    ) {
        return ResponseEntity.ok(subCategoryService.createSubcategory(categoryId, dto));
    }

    // =============================
// ATUALIZAR SUBCATEGORIA
// =============================
    @Operation(summary = "Atualizar Subcategoria",
            description = "Atualiza os dados de uma subcategoria existente dentro de uma categoria. " +
                    "O nome não pode duplicar outra subcategoria da mesma categoria.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Subcategoria atualizada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Subcategoria ou categoria não encontrada"),
            @ApiResponse(responseCode = "409", description = "Já existe outra subcategoria com este nome"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou inconsistentes")
    })
    @PutMapping("/{categoryId}/subcategories/{subcategoryId}")
    public ResponseEntity<SubcategoryDto> updateSubcategory(
            @PathVariable Long categoryId,
            @PathVariable Long subcategoryId,
            @RequestBody SubcategoryDto dto
    ) {
        return ResponseEntity.ok(subCategoryService.updateSubcategory(categoryId, subcategoryId, dto));
    }

    // =============================
// DELETAR SUBCATEGORIA
// =============================
    @Operation(summary = "Excluir Subcategoria",
            description = "Remove uma subcategoria existente de uma categoria. " +
                    "Não é possível deletar se a subcategoria não pertencer à categoria informada.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Subcategoria deletada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Subcategoria ou categoria não encontrada"),
            @ApiResponse(responseCode = "400", description = "Subcategoria não pertence à categoria informada")
    })
    @DeleteMapping("/{categoryId}/subcategories/{subcategoryId}")
    public ResponseEntity<Void> deleteSubcategory(
            @PathVariable Long categoryId,
            @PathVariable Long subcategoryId
    ) {
        subCategoryService.deleteSubcategory(categoryId, subcategoryId);
        return ResponseEntity.noContent().build();
    }

}
