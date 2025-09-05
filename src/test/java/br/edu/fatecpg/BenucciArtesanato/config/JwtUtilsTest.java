
package br.edu.fatecpg.BenucciArtesanato.config;

import br.edu.fatecpg.BenucciArtesanato.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @BeforeEach
    public void setUp() {
        jwtUtils = new JwtUtils(); // inicializa o JwtUtils
    }

    @Test
    public void testGenerateToken() {
        User usuario = User.builder()
                .email("teste@email.com")
                .name("Teste")
                .build();

        String token = jwtUtils.generateToken(usuario);

        assertNotNull(token, "O token não deve ser nulo");
        assertTrue(token.length() > 0, "O token não deve ser vazio");
    }

    @Test
    public void testValidateToken() {
        User usuario = User.builder()
                .email("teste@email.com")
                .name("Teste")
                .build();

        String token = jwtUtils.generateToken(usuario);

        assertTrue(jwtUtils.IsValidateToken(token), "O token deve ser válido");
    }

    @Test
    public void testGetEmailFromToken() {
        User usuario = User.builder()
                .email("teste@email.com")
                .name("Teste")
                .build();

        String token = jwtUtils.generateToken(usuario);

        String email = jwtUtils.getEmailFromToken(token);

        assertEquals("teste@email.com", email, "O email extraído do token deve ser o do usuário");
    }
}
