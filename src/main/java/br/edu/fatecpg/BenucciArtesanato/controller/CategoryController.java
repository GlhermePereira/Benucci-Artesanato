package br.edu.fatecpg.BenucciArtesanato.controller;

import br.edu.fatecpg.BenucciArtesanato.record.dto.CategoryDto;
import br.edu.fatecpg.BenucciArtesanato.record.dto.CategoryInputDTO;
import br.edu.fatecpg.BenucciArtesanato.record.dto.CategorySimpleDTO;
import br.edu.fatecpg.BenucciArtesanato.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    // ================================
    // CATEGORY ENDPOINTS
    // ================================


    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Operation(summary = "Listar Categorias (Simples)",
            description = "Retorna uma lista simples (ID e Nome) de todas as categorias, ideal para dropdowns.")
    @GetMapping("/list") // Novo endpoint mais específico
    public ResponseEntity<List<CategorySimpleDTO>> getCategoryList() {
        // Chama o novo método do serviço que retorna apenas o DTO simples
        return ResponseEntity.ok(categoryService.getSimpleCategories());
    }

    @Operation(summary = "Buscar Categoria por ID")
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDto> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @Operation(summary = "Criar Categoria",
            description = "Cria uma nova categoria com base no nome e descrição, retornando o recurso completo (ID, Slug, etc.).")
    @PostMapping
    // CORREÇÃO AQUI: O retorno deve ser CategoryDto, pois é o recurso criado
    public ResponseEntity<CategoryDto> createCategory(@Valid @RequestBody CategoryInputDTO dto) {

        // 1. Chama o serviço usando o DTO de entrada (Input)
        CategoryDto createdCategory = categoryService.createCategory(dto);

        // 2. Retorna 201 Created com o DTO de saída (Output)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
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
}


    // =========