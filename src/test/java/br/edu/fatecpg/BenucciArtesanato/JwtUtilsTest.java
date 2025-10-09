package br.edu.fatecpg.BenucciArtesanato;

import br.edu.fatecpg.BenucciArtesanato.config.JwtUtils;
import br.edu.fatecpg.BenucciArtesanato.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsTest {

    private JwtUtils jwtUtils;
    private User usuario;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        usuario = new User();
        usuario.setId(1L);
        usuario.setName("Teste User");
        usuario.setEmail("teste@email.com");
        usuario.setRole("ADMIN");
    }

    @Test
    void testGenerateToken() {
        String token = jwtUtils.generateToken(usuario);
        assertNotNull(token, "Token não deve ser nulo");
        System.out.println("Token gerado: " + token);
    }

    @Test
    void testGetEmailFromToken() {
        String token = jwtUtils.generateToken(usuario);
        String email = jwtUtils.getEmailFromToken(token);
        assertEquals(usuario.getEmail(), email, "O email extraído deve ser igual ao do usuário");
    }

    @Test
    void testValidateToken() {
        String token = jwtUtils.generateToken(usuario);
        assertTrue(jwtUtils.IsValidateToken(token), "Token válido deve retornar true");

        // Teste com token inválido
        String tokenInvalido = token + "123";
        assertFalse(jwtUtils.IsValidateToken(tokenInvalido), "Token alterado deve retornar false");
    }
}
