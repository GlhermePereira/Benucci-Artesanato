
package br.edu.fatecpg.BenucciArtesanato.config;

import br.edu.fatecpg.BenucciArtesanato.model.Usuario;
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
        Usuario usuario = Usuario.builder()
                .email("teste@email.com")
                .nome("Teste")
                .build();

        String token = jwtUtils.generateToken(usuario);

        assertNotNull(token, "O token não deve ser nulo");
        assertTrue(token.length() > 0, "O token não deve ser vazio");
    }

    @Test
    public void testValidateToken() {
        Usuario usuario = Usuario.builder()
                .email("teste@email.com")
                .nome("Teste")
                .build();

        String token = jwtUtils.generateToken(usuario);

        assertTrue(jwtUtils.validateToken(token), "O token deve ser válido");
    }

    @Test
    public void testGetEmailFromToken() {
        Usuario usuario = Usuario.builder()
                .email("teste@email.com")
                .nome("Teste")
                .build();

        String token = jwtUtils.generateToken(usuario);

        String email = jwtUtils.getEmailFromToken(token);

        assertEquals("teste@email.com", email, "O email extraído do token deve ser o do usuário");
    }
}
