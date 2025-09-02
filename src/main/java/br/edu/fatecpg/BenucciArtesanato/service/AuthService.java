package br.edu.fatecpg.BenucciArtesanato.service;


import br.edu.fatecpg.BenucciArtesanato.exception.ResourceNotFoundException;
import br.edu.fatecpg.BenucciArtesanato.model.Usuario;
import br.edu.fatecpg.BenucciArtesanato.record.LoginRequest;
import br.edu.fatecpg.BenucciArtesanato.record.RegisterRequest;
import br.edu.fatecpg.BenucciArtesanato.repository.UsuarioRepository;
import br.edu.fatecpg.BenucciArtesanato.config.JwtUtils;
import br.edu.fatecpg.BenucciArtesanato.service.exception.InvalidPasswordException;
import br.edu.fatecpg.BenucciArtesanato.service.exception.UserNotFoundException;
import br.edu.fatecpg.BenucciArtesanato.service.validation.UserValidator;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioRepository repository;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;
    private final UserValidator validator;

    public AuthService(UsuarioRepository repository,
                       PasswordEncoder encoder,
                       JwtUtils jwtUtils,
                       UserValidator validator) {
        this.repository = repository;
        this.encoder = encoder;
        this.jwtUtils = jwtUtils;
        this.validator = validator; // agora funciona
    }


    public Usuario register(RegisterRequest request) {
        Usuario usuario = Usuario.builder()
                .nome(request.nome())
                .email(request.email())
                .senha(encoder.encode(request.senha()))
                .telefone(request.telefone())
                .endereco(request.endereco())
                .tipo("cliente")
                .build();
        return repository.save(usuario);
    }

    public String authenticateUser(LoginRequest request) {
        Usuario usuario = repository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + request.email()));

        if (!encoder.matches(request.senha(), usuario.getSenha())) {
            throw new InvalidPasswordException("Senha inválida para o usuário: " + request.email());
        }

        return jwtUtils.generateToken(usuario);
    }


    @Bean
    CommandLineRunner init(UsuarioRepository repository, PasswordEncoder encoder) {
        return args -> {
            repository.findByEmail("admin@email.com")
                    .ifPresentOrElse(
                            usuario -> {
                                // garante que a senha está correta
                                usuario.setSenha(encoder.encode("admin123"));
                                repository.save(usuario);
                            },
                            () -> {
                                Usuario admin = Usuario.builder()
                                        .nome("Admin")
                                        .email("admin@email.com")
                                        .senha(encoder.encode("admin123"))
                                        .tipo("admin")
                                        .build();
                                repository.save(admin);
                            }
                    );
        };
    }




    public String login(LoginRequest request) {
        Usuario usuario = repository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + request.email()));

        if (!encoder.matches(request.senha(), usuario.getSenha())) {
            throw new InvalidPasswordException("Senha inválida para o usuário: " + request.email());
        }

        return jwtUtils.generateToken(usuario);
    }



}