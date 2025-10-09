package br.edu.fatecpg.BenucciArtesanato.config;

import br.edu.fatecpg.BenucciArtesanato.model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtils {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expirationMs}")
    private int jwtExpirationMs;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(User user) {
        // Extrai o type sem o prefixo ROLE_
        String type = user.getRole() != null ? user.getRole().replace("ROLE_", "") : "USER";

        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("id", user.getId())
                .claim("name", user.getName())
                .claim("role", user.getRole())  // mantém role para Spring Security (string)
                .claim("roles", List.of(user.getRole())) // compatibilidade com filter (lista)
                .claim("type", type)            // adiciona type para frontend
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public String getEmailFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean IsValidateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            logger.error("Invalid Token JWT: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("Token JWT expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("Token JWT not suported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT empty or null: {}", e.getMessage());
        }
        return false;
    }

    public boolean IsValidateToken(String token, User user) {
        String emailToken = getEmailFromToken(token);
        return emailToken.equals(user.getEmail()) && IsValidateToken(token);
    }
}