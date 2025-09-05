package br.edu.fatecpg.BenucciArtesanato.controller;


import br.edu.fatecpg.BenucciArtesanato.config.JwtUtils;
import br.edu.fatecpg.BenucciArtesanato.model.User;
import br.edu.fatecpg.BenucciArtesanato.record.LoginRequest;
import br.edu.fatecpg.BenucciArtesanato.record.RegisterRequest;
import br.edu.fatecpg.BenucciArtesanato.service.AuthService;
import br.edu.fatecpg.BenucciArtesanato.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService usuarioService;
    private final JwtUtils jwtUtils;
    private final AuthService authService;

    @Autowired
    public AuthController(UserService usuarioService, JwtUtils jwtUtils, AuthService authService) {
        this.usuarioService = usuarioService;
        this.jwtUtils = jwtUtils;
        this.authService = authService;
    }


    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody RegisterRequest request) {
        User usuario = authService.register(request);
        return ResponseEntity.ok(usuario);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {
        String token = authService.login(request);
        return ResponseEntity.ok(token);
    }
}