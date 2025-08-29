package br.edu.fatecpg.BenucciArtesanato.service;


import br.edu.fatecpg.BenucciArtesanato.model.Usuario;
import br.edu.fatecpg.BenucciArtesanato.record.LoginRequest;
import br.edu.fatecpg.BenucciArtesanato.record.RegisterRequest;
import br.edu.fatecpg.BenucciArtesanato.repository.UsuarioRepository;
import br.edu.fatecpg.BenucciArtesanato.config.JwtUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioRepository repository;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;

    public AuthService(UsuarioRepository repository, PasswordEncoder encoder, JwtUtils jwtUtils) {
        this.repository = repository;
        this.encoder = encoder;
        this.jwtUtils = jwtUtils;
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

    public String login(LoginRequest request) {
        Usuario usuario = repository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (!encoder.matches(request.senha(), usuario.getSenha())) {
            throw new RuntimeException("Senha inválida");
        }

        return jwtUtils.generateToken(usuario);
    }
}