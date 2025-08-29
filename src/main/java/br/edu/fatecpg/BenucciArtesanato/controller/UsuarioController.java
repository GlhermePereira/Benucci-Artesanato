package br.edu.fatecpg.BenucciArtesanato.controller;


import br.edu.fatecpg.BenucciArtesanato.model.Usuario;
import br.edu.fatecpg.BenucciArtesanato.service.UsuarioService;
import br.edu.fatecpg.BenucciArtesanato.record.dto.UsuarioDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @Autowired
    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> getUsuario(@PathVariable Long id) {
        Usuario usuario = usuarioService.buscarPorId(id);
        if (usuario == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(usuario);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Usuario> atualizarUsuario(@PathVariable Long id, @RequestBody UsuarioDTO usuarioDTO) {
        Usuario atualizado = usuarioService.atualizarUsuario(id, usuarioDTO);
        if (atualizado == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(atualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletarUsuario(@PathVariable Long id) {
        boolean removido = usuarioService.removerUsuario(id);
        if (!removido) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok("Usuário removido com sucesso!");
    }
}
