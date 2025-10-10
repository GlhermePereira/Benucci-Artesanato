package br.edu.fatecpg.BenucciArtesanato.controller;

import br.edu.fatecpg.BenucciArtesanato.model.User;
import br.edu.fatecpg.BenucciArtesanato.record.dto.UserDTO;
import br.edu.fatecpg.BenucciArtesanato.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;



    @GetMapping
    public ResponseEntity<List<UserDTO>> getAll() {
        List<UserDTO> users = userService.getAll().stream()
                .map(UserDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getById(@PathVariable Long id) {
        User user = userService.getById(id);
        return user != null ? ResponseEntity.ok(UserDTO.fromEntity(user))
                : ResponseEntity.notFound().build();
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserDTO> getByEmail(@PathVariable String email) {
        User user = userService.searchByEmail(email);
        return user != null ? ResponseEntity.ok(UserDTO.fromEntity(user))
                : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<UserDTO> registerUser(@RequestBody UserDTO dto) {
        User created = userService.registerUser(dto);
        return ResponseEntity.ok(UserDTO.fromEntity(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody UserDTO dto) {
        User updated = userService.updateUser(id, dto);
        return updated != null ? ResponseEntity.ok(UserDTO.fromEntity(updated))
                : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        boolean deleted = userService.deleteUser(id);
        return deleted ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
