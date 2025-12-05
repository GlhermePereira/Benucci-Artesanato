package br.edu.fatecpg.BenucciArtesanato.controller;

import br.edu.fatecpg.BenucciArtesanato.config.JwtUtils;
import br.edu.fatecpg.BenucciArtesanato.model.User;
import br.edu.fatecpg.BenucciArtesanato.record.AuthResponse;
import br.edu.fatecpg.BenucciArtesanato.record.LoginRequest;
import br.edu.fatecpg.BenucciArtesanato.record.RegisterRequest;
import br.edu.fatecpg.BenucciArtesanato.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "auth-controller", description = "Autenticação e registro de usuários")
public class AuthController {

    private final JwtUtils jwtUtils;
    private final AuthService authService;

    @Autowired
    public AuthController(JwtUtils jwtUtils, AuthService authService) {
        this.jwtUtils = jwtUtils;
        this.authService = authService;
    }

    // DTO de erro padrão
    @Data
    @AllArgsConstructor
    static class ErrorResponse {
        private String message;
        private int status;
    }

    @Operation(summary = "Registrar usuário", description = "Registra um novo usuário no sistema e retorna um token JWT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário registrado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class),
                            examples = @ExampleObject(value = "{\"token\":\"jwt_token_aqui\",\"user\":{\"id\":1,\"name\":\"João\",\"email\":\"joao@email.com\"}}")
                    )),
            @ApiResponse(responseCode = "400", description = "Dados inválidos para registro",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Email já cadastrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        System.out.println("📝 Tentativa de registro para: " + request.email());
        try {
            User user = authService.register(request);
            String token = jwtUtils.generateToken(user);

            System.out.println("✅ Usuário registrado com sucesso!");
            AuthResponse response = new AuthResponse(token, user);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException ex) {
            System.err.println("❌ Dados inválidos: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(ex.getMessage(), 400));

        } catch (IllegalStateException ex) {
            System.err.println("❌ Email já cadastrado: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse(ex.getMessage(), 409));

        } catch (Exception ex) {
            System.err.println("❌ Erro inesperado no registro: " + ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Erro interno. Contate o administrador.", 500));
        }
    }

    @Operation(summary = "Login de usuário", description = "Autentica um usuário e retorna um token JWT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login bem-sucedido",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class),
                            examples = @ExampleObject(value = "{\"token\":\"jwt_token_aqui\",\"user\":{\"id\":1,\"name\":\"João\",\"email\":\"joao@email.com\"}}")
                    )),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        System.out.println("🔐 Tentativa de login para: " + request.email());
        try {
            String token = authService.login(request);
            User user = authService.getUserByEmail(request.email());

            System.out.println("✅ Login bem-sucedido!");
            AuthResponse response = new AuthResponse(token, user);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException ex) {
            System.err.println("❌ Credenciais inválidas: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(ex.getMessage(), 401));

        } catch (IllegalStateException ex) {
            System.err.println("❌ Usuário não encontrado: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(ex.getMessage(), 404));

        } catch (Exception ex) {
            System.err.println("❌ Erro inesperado no login: " + ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Erro interno. Contate o administrador.", 500));
        }
    }
}
