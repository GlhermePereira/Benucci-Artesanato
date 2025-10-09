
package br.edu.fatecpg.BenucciArtesanato.service;

import br.edu.fatecpg.BenucciArtesanato.config.JwtUtils;
import br.edu.fatecpg.BenucciArtesanato.exception.ResourceNotFoundException;
import br.edu.fatecpg.BenucciArtesanato.model.User;
import br.edu.fatecpg.BenucciArtesanato.record.LoginRequest;
import br.edu.fatecpg.BenucciArtesanato.record.RegisterRequest;
import br.edu.fatecpg.BenucciArtesanato.repository.UserRepository;
import br.edu.fatecpg.BenucciArtesanato.service.exception.InvalidPasswordException;
import br.edu.fatecpg.BenucciArtesanato.service.validation.UserValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private UserRepository repository;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserValidator validator;

    @InjectMocks
    private AuthService authService;

    private User usuario;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        usuario = User.builder()
                .id(1L)
                .name("Joao")
                .email("joao@email.com")
                .password("hashedSenha")
                .phoneNumber("13988240253")
                .address("Av. São Paulo, 416")
                .role("cliente")
                .build();

        RegisterRequest request = new RegisterRequest(
                "Joao",
                "joao@email.com",
                "1234",
                "13988240253",      // ← phoneNumber (4º parâmetro)
                "Av. São Paulo",    // ← address (5º parâmetro)
                "12345678900",      // ← CPF (6º parâmetro)
                "cliente"           // ← type (7º parâmetro)
        );

        loginRequest = new LoginRequest("joao@email.com", "senha123");
    }

    @Test
    void testRegisterSuccess() {
        when(encoder.encode(registerRequest.password())).thenReturn("hashedSenha");
        when(repository.save(any(User.class))).thenReturn(usuario);

        User savedUser = authService.register(registerRequest);

        assertNotNull(savedUser, "O usuário registrado não deve ser nulo");
        assertEquals(usuario.getEmail(), savedUser.getEmail(), "O email do usuário deve ser igual");
        verify(repository, times(1)).save(any(User.class));
    }

    @Test
    void testAuthenticateUserSuccess() {
        when(repository.findByEmail(loginRequest.email())).thenReturn(Optional.of(usuario));
        when(encoder.matches(loginRequest.password(), usuario.getPassword())).thenReturn(true);
        when(jwtUtils.generateToken(usuario)).thenReturn("token123");

        String token = authService.authenticateUser(loginRequest);

        assertEquals("token123", token, "O token gerado deve ser igual ao esperado");
    }

    @Test
    void testAuthenticateUserUserNotFound() {
        when(repository.findByEmail(loginRequest.email())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            authService.authenticateUser(loginRequest);
        });

        assertTrue(exception.getMessage().contains("Usuário não encontrado"), "Mensagem de erro deve indicar usuário não encontrado");
    }

    @Test
    void testAuthenticateUserInvalidPassword() {
        when(repository.findByEmail(loginRequest.email())).thenReturn(Optional.of(usuario));
        when(encoder.matches(loginRequest.password(), usuario.getPassword())).thenReturn(false);

        InvalidPasswordException exception = assertThrows(InvalidPasswordException.class, () -> {
            authService.authenticateUser(loginRequest);
        });

        assertTrue(exception.getMessage().contains("Senha inválida"), "Mensagem de erro deve indicar password inválida");
    }

    @Test
    void testLoginSuccess() {
        when(repository.findByEmail(loginRequest.email())).thenReturn(Optional.of(usuario));
        when(encoder.matches(loginRequest.password(), usuario.getPassword())).thenReturn(true);
        when(jwtUtils.generateToken(usuario)).thenReturn("tokenLogin");

        String token = authService.login(loginRequest);

        assertEquals("tokenLogin", token, "O token gerado pelo login deve ser igual ao esperado");
    }

    @Test
    void testLoginUserNotFound() {
        when(repository.findByEmail(loginRequest.email())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            authService.login(loginRequest);
        });

        assertTrue(exception.getMessage().contains("Usuário não encontrado"), "Mensagem deve indicar que usuário não foi encontrado");
    }

    @Test
    void testLoginInvalidPassword() {
        when(repository.findByEmail(loginRequest.email())).thenReturn(Optional.of(usuario));
        when(encoder.matches(loginRequest.password(), usuario.getPassword())).thenReturn(false);

        InvalidPasswordException exception = assertThrows(InvalidPasswordException.class, () -> {
            authService.login(loginRequest);
        });

        assertTrue(exception.getMessage().contains("Senha inválida"), "Mensagem deve indicar password incorreta");
    }

    @Test
    void testInitAdminCreation() throws Exception {
        when(repository.findByEmail("admin@email.com")).thenReturn(Optional.empty());
        when(encoder.encode("admin123")).thenReturn("encodedAdmin123");

        authService.init(repository, encoder).run();

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(repository, times(1)).save(captor.capture());
        User savedAdmin = captor.getValue();

        assertEquals("Admin", savedAdmin.getName(), "O admin criado deve ter name 'Admin'");
        assertEquals("admin@email.com", savedAdmin.getEmail(), "O email do admin deve ser 'admin@email.com'");
        assertEquals("encodedAdmin123", savedAdmin.getPassword(), "A password deve estar codificada corretamente");
    }
}
