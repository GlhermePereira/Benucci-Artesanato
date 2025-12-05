package br.edu.fatecpg.BenucciArtesanato.controller;

import br.edu.fatecpg.BenucciArtesanato.record.dto.ThemeInputDTO;
import br.edu.fatecpg.BenucciArtesanato.service.ThemeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/themes")
@RequiredArgsConstructor
@Tag(name = "theme-controller", description = "Gerenciamento de temas: criar, atualizar, listar e deletar")
public class ThemeController {


    private final ThemeService themeService;

    // =============================
// Criar Tema
// =============================
    @Operation(summary = "Criar um novo tema", description = "Cria um tema com nome único e descrição. Retorna o tema criado com seu ID e slug gerado automaticamente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tema criado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ThemeInputDTO.class),
                            examples = @ExampleObject(value = "{\"id\":1,\"name\":\"Artesanato\",\"slug\":\"artesanato\",\"description\":\"Tema de artesanato\"}")
                    )),
            @ApiResponse(responseCode = "409", description = "Já existe um tema com esse nome",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"status\":409,\"message\":\"Já existe um tema com esse nome.\"}")
                    ))
    })
    @PostMapping
    public ResponseEntity<ThemeInputDTO> createTheme(@RequestBody
                                                     @Parameter(description = "Dados do tema a ser criado", required = true)
                                                     ThemeInputDTO dto) {
        ThemeInputDTO created = themeService.createTheme(dto);
        return ResponseEntity.ok(created);
    }

    // =============================
// Listar Todos os Temas
// =============================
    @Operation(summary = "Listar todos os temas", description = "Retorna uma lista de todos os temas cadastrados, ordenados pelo nome.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de temas retornada com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ThemeInputDTO.class),
                            examples = @ExampleObject(value = "[{\"id\":1,\"name\":\"Artesanato\",\"slug\":\"artesanato\",\"description\":\"Tema de artesanato\"}]")
                    ))
    })
    @GetMapping
    public ResponseEntity<List<ThemeInputDTO>> getAllThemes() {
        List<ThemeInputDTO> themes = themeService.getAllThemes();
        return ResponseEntity.ok(themes);
    }

    // =============================
// Atualizar Tema
// =============================
    @Operation(summary = "Atualizar um tema pelo ID", description = "Atualiza os dados de um tema existente. Não é permitido ter nomes duplicados.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tema atualizado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ThemeInputDTO.class),
                            examples = @ExampleObject(value = "{\"id\":1,\"name\":\"Novo Nome\",\"slug\":\"novo-nome\",\"description\":\"Nova descrição\"}")
                    )),
            @ApiResponse(responseCode = "404", description = "Tema não encontrado",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"status\":404,\"message\":\"Tema não encontrado\"}")
                    )),
            @ApiResponse(responseCode = "409", description = "Já existe outro tema com esse nome",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"status\":409,\"message\":\"Já existe outro tema com esse nome.\"}")
                    ))
    })
    @PutMapping("/{id}")
    public ResponseEntity<ThemeInputDTO> updateTheme(
            @Parameter(description = "ID do tema a ser atualizado", required = true) @PathVariable Long id,
            @Parameter(description = "Dados do tema atualizados", required = true) @RequestBody ThemeInputDTO dto) {
        ThemeInputDTO updated = themeService.updateTheme(id, dto);
        return ResponseEntity.ok(updated);
    }

    // =============================
// Deletar Tema
// =============================
    @Operation(summary = "Deletar um tema pelo ID", description = "Remove um tema existente. Retorna HTTP 204 se a exclusão for bem-sucedida.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Tema deletado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Tema não encontrado",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"status\":404,\"message\":\"Tema não encontrado\"}")
                    ))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTheme(
            @Parameter(description = "ID do tema a ser deletado", required = true) @PathVariable Long id) {
        themeService.deleteTheme(id);
        return ResponseEntity.noContent().build();
    }


}
