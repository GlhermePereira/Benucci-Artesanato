package br.edu.fatecpg.BenucciArtesanato.controller;

import br.edu.fatecpg.BenucciArtesanato.record.dto.SubcategoryThemeAssignDTO;
import br.edu.fatecpg.BenucciArtesanato.service.SubcategoryThemeService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/subcategory-themes")
@RequiredArgsConstructor
public class SubcategoryThemeController {

    private final SubcategoryThemeService subcategoryThemeService;

    // =============================
    // POST → Associar temas (adicionar)
    // =============================
    @Operation(summary = "Associar temas a uma subcategoria")
    @PostMapping
    public ResponseEntity<Void> assignThemes(@RequestBody SubcategoryThemeAssignDTO dto) {
        subcategoryThemeService.assignThemesToSubcategory(dto);
        return ResponseEntity.ok().build();
    }

    // =============================
    // PUT → Atualizar/Substituir temas
    // =============================
    @Operation(summary = "Atualizar temas associados a uma subcategoria")
    @PutMapping("/{subcategoryId}")
    public ResponseEntity<Void> updateThemes(
            @PathVariable Long subcategoryId,
            @RequestBody List<Long> themeIds) {
        subcategoryThemeService.updateThemes(subcategoryId, themeIds);
        return ResponseEntity.ok().build();
    }

    // =============================
    // DELETE → Remover associação específica
    // =============================
    @Operation(summary = "Remover um tema específico de uma subcategoria")
    @DeleteMapping("/{subcategoryId}/{themeId}")
    public ResponseEntity<Void> removeTheme(
            @PathVariable Long subcategoryId,
            @PathVariable Long themeId) {
        subcategoryThemeService.removeThemeFromSubcategory(subcategoryId, themeId);
        return ResponseEntity.noContent().build();
    }

    // =============================
    // GET → Listar IDs de temas
    // =============================
    @Operation(summary = "Listar IDs de temas associados a uma subcategoria")
    @GetMapping("/{subcategoryId}")
    public ResponseEntity<List<Long>> getThemesBySubcategory(@PathVariable Long subcategoryId) {
        return ResponseEntity.ok(subcategoryThemeService.getThemesBySubcategory(subcategoryId));
    }
}
