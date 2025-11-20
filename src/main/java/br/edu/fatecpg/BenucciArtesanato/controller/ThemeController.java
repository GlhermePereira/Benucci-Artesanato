package br.edu.fatecpg.BenucciArtesanato.controller;

import br.edu.fatecpg.BenucciArtesanato.record.dto.ThemeInputDTO;
import br.edu.fatecpg.BenucciArtesanato.service.ThemeService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/themes")
@RequiredArgsConstructor
public class ThemeController {

    private final ThemeService themeService;

    // =============================
    // ROTA 1 → Criar Tema
    // =============================
    @Operation(summary = "Criar um tema")
    @PostMapping
    public ResponseEntity<ThemeInputDTO> createTheme(@RequestBody ThemeInputDTO dto) {
        return ResponseEntity.ok(themeService.createTheme(dto));
    }

    // =============================
    // ROTA 2 → Listar todos os temas
    // =============================
    @Operation(summary = "Listar todos os temas")
    @GetMapping
    public ResponseEntity<List<ThemeInputDTO>> getAllThemes() {
        return ResponseEntity.ok(themeService.getAllThemes());
    }

    // =============================
    // ROTA 3 → Atualizar tema
    // =============================
    @Operation(summary = "Atualizar um tema pelo ID")
    @PutMapping("/{id}")
    public ResponseEntity<ThemeInputDTO> updateTheme(@PathVariable Long id,
                                                     @RequestBody ThemeInputDTO dto) {
        return ResponseEntity.ok(themeService.updateTheme(id, dto));
    }

    // =============================
    // ROTA 4 → Deletar tema
    // =============================
    @Operation(summary = "Deletar um tema pelo ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTheme(@PathVariable Long id) {
        themeService.deleteTheme(id);
        return ResponseEntity.noContent().build();
    }
}
