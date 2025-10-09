package br.edu.fatecpg.BenucciArtesanato.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            // 1. Extrair o token do header Authorization
            String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                System.out.println("🔍 JwtAuthFilter: Nenhum token Bearer encontrado");
                filterChain.doFilter(request, response);
                return;
            }

            String token = authHeader.substring(7); // Remove "Bearer "
            System.out.println("🔍 JwtAuthFilter: Token recebido: " + token.substring(0, Math.min(20, token.length())) + "...");

            // 2. Validar e extrair claims do token
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String email = claims.getSubject();

            // 3. Extrair roles do token (prefer 'roles' array, fallback to 'role' string)
            @SuppressWarnings("unchecked")
            List<String> roles = claims.get("roles", List.class);

            if (roles == null) {
                // try single string claim named 'role'
                String singleRole = claims.get("role", String.class);
                if (singleRole != null) {
                    roles = List.of(singleRole);
                }
            }

            System.out.println("✅ JwtAuthFilter: Token válido para usuário: " + email);
            System.out.println("✅ JwtAuthFilter: Roles: " + roles);

            if (roles == null) {
                roles = List.of("ROLE_USER"); // default role if missing
            }

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        // 4. Converter roles para authorities do Spring Security
        List<SimpleGrantedAuthority> authorities = roles.stream()
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());

        // 5. Criar autenticação e setar no contexto
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(email, null, authorities);

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                System.out.println("✅ JwtAuthFilter: Autenticação configurada com sucesso!");
                System.out.println("✅ JwtAuthFilter: Authorities: " + authorities);
            }

        } catch (Exception e) {
            System.err.println("❌ JwtAuthFilter: Erro ao processar token: " + e.getMessage());
            e.printStackTrace();
        }

        filterChain.doFilter(request, response);
    }
}