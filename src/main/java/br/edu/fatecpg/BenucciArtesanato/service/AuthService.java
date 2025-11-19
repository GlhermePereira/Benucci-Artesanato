package br.edu.fatecpg.BenucciArtesanato.service;


import br.edu.fatecpg.BenucciArtesanato.exception.ResourceNotFoundException;
import br.edu.fatecpg.BenucciArtesanato.model.User;
import br.edu.fatecpg.BenucciArtesanato.record.LoginRequest;
import br.edu.fatecpg.BenucciArtesanato.record.RegisterRequest;
import br.edu.fatecpg.BenucciArtesanato.repository.UserRepository;
import br.edu.fatecpg.BenucciArtesanato.config.JwtUtils;
import br.edu.fatecpg.BenucciArtesanato.service.exception.InvalidPasswordException;
import br.edu.fatecpg.BenucciArtesanato.service.validation.UserValidator;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository repository;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;
    private final UserValidator validator;

    public AuthService(UserRepository repository,
                       PasswordEncoder encoder,
                       JwtUtils jwtUtils,
                       UserValidator validator) {
        this.repository = repository;
        this.encoder = encoder;
        this.jwtUtils = jwtUtils;
        this.validator = validator; // agora funciona
    }


    public User register(RegisterRequest request) {

        // role padrão
        String role = "ROLE_USER";

        // se vier "admin", vira ROLE_ADMIN
        if (request.role() != null && request.role().equalsIgnoreCase("admin")) {
            role = "ROLE_ADMIN";
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(encoder.encode(request.password()))
                .phoneNumber(request.phoneNumber())
                .address(request.address())
                .cpf(request.cpf())
                .role(role)
                .build();

        return repository.save(user);
    }



    public User getUserByEmail(String email) {
        return repository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
    }

    public String authenticateUser(LoginRequest request) {
        User user = repository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("User not founded: " + request.email()));

        if (!encoder.matches(request.password(), user.getPassword())) {
            throw new InvalidPasswordException("Senha inválida para o usuário: " + request.email());
        }

        return jwtUtils.generateToken(user);
    }





    public String login(LoginRequest request) {
        User user = repository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + request.email()));

        if (!encoder.matches(request.password(), user.getPassword())) {
            throw new InvalidPasswordException("Senha inválida para o usuário: " + request.email());
        }

        return jwtUtils.generateToken(user);
    }



}
