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

@Component
public class JwtUtils {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${jwt.secret}")
    private String jwtSecret; // chave base64 ou longa o suficiente (>= 64 bytes)

    @Value("${jwt.expirationMs}")
    private int jwtExpirationMs;

    // Cria a chave de assinatura a partir do jwtSecret
// Cria a chave de assinatura a partir do jwtSecret (Base64)
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }


    //
    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("id", user.getId())
                .claim("name", user.getName())
                .claim("type", user.getType())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    // Obtém o email do token
    public String getEmailFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // Valida o token JWT sem verificar usuário
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

    // Valida o token e se pertence ao usuário
    public boolean IsValidateToken(String token, User user) {
        String emailToken = getEmailFromToken(token);
        // Chama o validateToken sem usuário para checar validade do token
        return emailToken.equals(user.getEmail()) && IsValidateToken(token);
    }


}
