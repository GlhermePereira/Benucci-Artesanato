package br.edu.fatecpg.BenucciArtesanato.controller;

import br.edu.fatecpg.BenucciArtesanato.record.LoginRequest;
import br.edu.fatecpg.BenucciArtesanato.record.RegisterRequest;
import br.edu.fatecpg.BenucciArtesanato.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class AuthControllerTest {

    @Mock
    private AuthService authService;  // mock do service

    @InjectMocks
    private AuthController authController;  // controller que vamos testar

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this); // inicializa mocks
    }

    @Test
    public void testRegister() {
        RegisterRequest request = new RegisterRequest(
                "Joao",
                "joao@email.com",
                "1234",
                "13988240253",
                "Av. São Paulo",
                "cliente",      // ← tipo de usuário (cliente ou admin)
                "12345678900"   // ← CPF
        );

        when(authService.register(request)).thenReturn(null);

        ResponseEntity<?> response = authController.register(request);

        assertEquals(201, response.getStatusCodeValue());
        verify(authService, times(1)).register(request);
    }

    @Test
    public void testLogin() {
        LoginRequest request = new LoginRequest("joao@email.com", "1234");
        String tokenMock = "token123";

        when(authService.authenticateUser(request)).thenReturn(tokenMock);

        ResponseEntity<?> response = authController.login(request);

        // Verifica se o status retornado é 200 OK
        assertEquals(200, response.getStatusCodeValue());

        // Verifica se o token retornado é o mock
        assertEquals(tokenMock, response.getBody());

        // Verifica se o método authenticateUser do service foi chamado exatamente uma vez
        verify(authService, times(1)).authenticateUser(request);
    }
}