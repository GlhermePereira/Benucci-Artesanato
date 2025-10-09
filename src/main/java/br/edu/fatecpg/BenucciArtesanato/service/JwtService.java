package br.edu.fatecpg.BenucciArtesanato.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}") // 24 horas padrão
    private Long jwtExpiration;

    /**
     * Gera um token JWT com o email e as roles do usuário
     *
     * @param email Email do usuário
     * @param userType Tipo do usuário (ADMIN ou USER)
     * @return Token JWT
     */
    public String generateToken(String email, String userType) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        // 🔥 CRÍTICO: As roles devem estar no formato "ROLE_ADMIN" ou "ROLE_USER"
        // O Spring Security automaticamente adiciona o prefixo "ROLE_" quando usa hasRole()
        List<String> roles = userType.equals("ADMIN")
                ? List.of("ROLE_ADMIN", "ROLE_USER")
                : List.of("ROLE_USER");

        System.out.println("🔑 JwtService: Gerando token para: " + email);
        System.out.println("🔑 JwtService: Tipo de usuário: " + userType);
        System.out.println("🔑 JwtService: Roles: " + roles);

        String token = Jwts.builder()
                .setSubject(email)
                .claim("roles", roles)
                .claim("userType", userType)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(key)
                .compact();

        System.out.println("✅ JwtService: Token gerado com sucesso!");
        return token;
    }

    /**
     * Valida se o token é válido
     */
    public boolean validateToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            System.err.println("❌ JwtService: Token inválido - " + e.getMessage());
            return false;
        }
    }

    /**
     * Extrai o email do token
     */
    public String getEmailFromToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (Exception e) {
            System.err.println("❌ JwtService: Erro ao extrair email do token - " + e.getMessage());
            return null;
        }
    }
}