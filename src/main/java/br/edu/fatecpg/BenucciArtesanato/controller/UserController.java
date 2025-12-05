package br.edu.fatecpg.BenucciArtesanato.controller;

import br.edu.fatecpg.BenucciArtesanato.model.User;
import br.edu.fatecpg.BenucciArtesanato.record.dto.UserDTO;
import br.edu.fatecpg.BenucciArtesanato.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "user-controller", description = "Gerenciamento de usuários")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Listar todos os usuários", description = "Retorna uma lista de todos os usuários cadastrados")
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAll() {
        List<UserDTO> users = userService.getAll().stream()
                .map(UserDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Buscar usuário por ID", description = "Retorna os dados de um usuário pelo seu ID")
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getById(@PathVariable Long id) {
        User user = userService.getByIdOrThrow(id); // método lança ResourceNotFoundException se não achar
        return ResponseEntity.ok(UserDTO.fromEntity(user));
    }

    @Operation(summary = "Buscar usuário por email", description = "Retorna os dados de um usuário pelo email")
    @GetMapping("/email/{email}")
    public ResponseEntity<UserDTO> getByEmail(@PathVariable String email) {
        User user = userService.getByEmailOrThrow(email); // lança ResourceNotFoundException se não achar
        return ResponseEntity.ok(UserDTO.fromEntity(user));
    }

    @Operation(summary = "Cadastrar novo usuário", description = "Cadastra um novo usuário no sistema")
    @PostMapping
    public ResponseEntity<UserDTO> registerUser(@RequestBody UserDTO dto) {
        User created = userService.registerUser(dto); // lança InvalidDataException se email já existir
        return ResponseEntity.ok(UserDTO.fromEntity(created));
    }

    @Operation(summary = "Atualizar usuário", description = "Atualiza os dados de um usuário pelo ID")
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody UserDTO dto) {
        User updated = userService.updateUser(id, dto);
        return ResponseEntity.ok(UserDTO.fromEntity(updated));
    }

    @Operation(summary = "Deletar usuário", description = "Deleta um usuário pelo ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUserOrThrow(id); // lança ResourceNotFoundException se não achar
        return ResponseEntity.noContent().build();
    }
}
