package io.bootify.cookflow.cook_flow_gestion_de_tareas.service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import io.bootify.cookflow.cook_flow_gestion_de_tareas.domain.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private static final String SECRET_KEY = "d36905aa2b801d074089a2fa241b82d548638b6075811796";

    // Generar token JWT
    public String generateToken(UserDetails userDetails) {
        Usuario usuario = (Usuario) userDetails;

        if (usuario.getRole() == null) {
            throw new IllegalStateException("El usuario debe tener un rol asignado");
        }

        String roleName = usuario.getRole().name();

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", roleName);

        return buildToken(claims, usuario.getEmail());
    }

    // Construir token con expiración de 24 horas
    private String buildToken(Map<String, Object> claims, String subject) {
        long now = System.currentTimeMillis();
        long expiration = now + (1000 * 60 * 60 * 24); // 24h

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(expiration))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Clave secreta (decodificada en Base64)
    private Key getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // === Métodos públicos para obtener y validar datos del token ===

    public String getEmailFromToken(String token) {
        return getClaim(token, Claims::getSubject);
    }

    public String getRoleFromToken(String token) {
        try {
            Claims claims = getAllClaims(token);
            Object roleClaim = claims.get("role");
            return roleClaim != null ? roleClaim.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String email = getEmailFromToken(token);
            return email.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    // === Métodos auxiliares privados ===

    private Claims getAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private <T> T getClaim(String token, Function<Claims, T> resolver) {
        final Claims claims = getAllClaims(token);
        return resolver.apply(claims);
    }

    private Date getExpiration(String token) {
        return getClaim(token, Claims::getExpiration);
    }

    private boolean isTokenExpired(String token) {
        return getExpiration(token).before(new Date());
    }
}
