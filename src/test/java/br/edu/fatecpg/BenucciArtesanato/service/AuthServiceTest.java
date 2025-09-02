
package br.edu.fatecpg.BenucciArtesanato.service;

import br.edu.fatecpg.BenucciArtesanato.config.JwtUtils;
import br.edu.fatecpg.BenucciArtesanato.exception.ResourceNotFoundException;
import br.edu.fatecpg.BenucciArtesanato.model.Usuario;
import br.edu.fatecpg.BenucciArtesanato.record.LoginRequest;
import br.edu.fatecpg.BenucciArtesanato.record.RegisterRequest;
import br.edu.fatecpg.BenucciArtesanato.repository.UsuarioRepository;
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
    private UsuarioRepository repository;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserValidator validator;

    @InjectMocks
    private AuthService authService;

    private Usuario usuario;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        usuario = Usuario.builder()
                .id(1L)
                .nome("Joao")
                .email("joao@email.com")
                .senha("hashedSenha")
                .telefone("13988240253")
                .endereco("Av. São Paulo, 416")
                .tipo("cliente")
                .build();

        registerRequest = new RegisterRequest(
                "Joao", "joao@email.com", "senha123", "13988240253", "Av. São Paulo, 416"
        );

        loginRequest = new LoginRequest("joao@email.com", "senha123");
    }

    @Test
    void testRegisterSuccess() {
        when(encoder.encode(registerRequest.senha())).thenReturn("hashedSenha");
        when(repository.save(any(Usuario.class))).thenReturn(usuario);

        Usuario savedUser = authService.register(registerRequest);

        assertNotNull(savedUser, "O usuário registrado não deve ser nulo");
        assertEquals(usuario.getEmail(), savedUser.getEmail(), "O email do usuário deve ser igual");
        verify(repository, times(1)).save(any(Usuario.class));
    }

    @Test
    void testAuthenticateUserSuccess() {
        when(repository.findByEmail(loginRequest.email())).thenReturn(Optional.of(usuario));
        when(encoder.matches(loginRequest.senha(), usuario.getSenha())).thenReturn(true);
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
        when(encoder.matches(loginRequest.senha(), usuario.getSenha())).thenReturn(false);

        InvalidPasswordException exception = assertThrows(InvalidPasswordException.class, () -> {
            authService.authenticateUser(loginRequest);
        });

        assertTrue(exception.getMessage().contains("Senha inválida"), "Mensagem de erro deve indicar senha inválida");
    }

    @Test
    void testLoginSuccess() {
        when(repository.findByEmail(loginRequest.email())).thenReturn(Optional.of(usuario));
        when(encoder.matches(loginRequest.senha(), usuario.getSenha())).thenReturn(true);
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
        when(encoder.matches(loginRequest.senha(), usuario.getSenha())).thenReturn(false);

        InvalidPasswordException exception = assertThrows(InvalidPasswordException.class, () -> {
            authService.login(loginRequest);
        });

        assertTrue(exception.getMessage().contains("Senha inválida"), "Mensagem deve indicar senha incorreta");
    }

    @Test
    void testInitAdminCreation() throws Exception {
        when(repository.findByEmail("admin@email.com")).thenReturn(Optional.empty());
        when(encoder.encode("admin123")).thenReturn("encodedAdmin123");

        authService.init(repository, encoder).run();

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(repository, times(1)).save(captor.capture());
        Usuario savedAdmin = captor.getValue();

        assertEquals("Admin", savedAdmin.getNome(), "O admin criado deve ter nome 'Admin'");
        assertEquals("admin@email.com", savedAdmin.getEmail(), "O email do admin deve ser 'admin@email.com'");
        assertEquals("encodedAdmin123", savedAdmin.getSenha(), "A senha deve estar codificada corretamente");
    }
}
