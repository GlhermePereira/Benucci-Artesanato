package br.edu.fatecpg.BenucciArtesanato.controller;

import br.edu.fatecpg.BenucciArtesanato.model.User;
import br.edu.fatecpg.BenucciArtesanato.record.dto.UserDTO;
import br.edu.fatecpg.BenucciArtesanato.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // GET /users/{id} → retorna dados do usuário como DTO
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUser(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        // Converte User para UserDTO
        UserDTO dto = new UserDTO();
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPassword(user.getPassword());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setAddress(user.getAddress());
        dto.setType(user.getType());
        dto.setCpf(user.getCpf());

        return ResponseEntity.ok(dto);
    }

    // PUT /users/{id} → atualiza dados do usuário
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        User updatedUser = userService.updateUser(id, userDTO);
        if (updatedUser == null) {
            return ResponseEntity.notFound().build();
        }

        // Converte atualizado para DTO
        UserDTO dto = new UserDTO();
        dto.setName(updatedUser.getName());
        dto.setEmail(updatedUser.getEmail());
        dto.setPassword(updatedUser.getPassword());
        dto.setPhoneNumber(updatedUser.getPhoneNumber());
        dto.setAddress(updatedUser.getAddress());
        dto.setType(updatedUser.getType());
        dto.setCpf(updatedUser.getCpf());

        return ResponseEntity.ok(dto);
    }

    // DELETE /users/{id} → remove usuário
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        boolean removed = userService.deleteUser(id);
        if (!removed) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok("Usuário removido com sucesso!");
    }
}
