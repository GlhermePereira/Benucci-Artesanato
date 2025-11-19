package br.edu.fatecpg.BenucciArtesanato.controller;

import br.edu.fatecpg.BenucciArtesanato.config.JwtUtils;
import br.edu.fatecpg.BenucciArtesanato.model.User;
import br.edu.fatecpg.BenucciArtesanato.record.AuthResponse;
import br.edu.fatecpg.BenucciArtesanato.record.LoginRequest;
import br.edu.fatecpg.BenucciArtesanato.record.RegisterRequest;
import br.edu.fatecpg.BenucciArtesanato.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "auth-controller", description = "Autenticação e registro")

public class AuthController {

    private final JwtUtils jwtUtils;
    private final AuthService authService;

    @Autowired
    public AuthController(JwtUtils jwtUtils, AuthService authService) {
        this.jwtUtils = jwtUtils;
        this.authService = authService;
    }
    @Operation(summary = "Registrar usuário", description = "Registra um novo usuário no sistema e retorna um token JWT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário registrado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos para registro"),
            @ApiResponse(responseCode = "409", description = "Email já cadastrado")
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        System.out.println("📝 AuthController: Tentativa de registro para: " + request.email());

        User user = authService.register(request);
        String token = jwtUtils.generateToken(user);

        System.out.println("✅ AuthController: Usuário registrado com sucesso!");
        System.out.println("✅ AuthController: Token gerado");

        AuthResponse response = new AuthResponse(token, user);
        return ResponseEntity.ok(response);
    }
    @Operation(summary = "Login de usuário", description = "Autentica um usuário e retorna um token JWT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login bem-sucedido",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        System.out.println("🔐 AuthController: Tentativa de login para: " + request.email());

        String token = authService.login(request);
        User user = authService.getUserByEmail(request.email());

        System.out.println("✅ AuthController: Login bem-sucedido!");
        System.out.println("✅ AuthController: Token gerado");

        AuthResponse response = new AuthResponse(token, user);
        return ResponseEntity.ok(response);
    }
}