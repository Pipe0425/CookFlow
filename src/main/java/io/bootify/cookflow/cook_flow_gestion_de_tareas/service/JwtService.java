package io.bootify.cookflow.cook_flow_gestion_de_tareas.service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import io.bootify.cookflow.cook_flow_gestion_de_tareas.domain.Usuario;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.domain.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);
    private static final String SECRET_KEY = "d36905aa2b801d074089a2fa241b82d548638b6075811796";

    public String generateToken(UserDetails userDetails) {
        log.info("╔════════════════════════════════════════╗");
        log.info("║   GENERANDO NUEVO TOKEN JWT            ║");
        log.info("╚════════════════════════════════════════╝");
        
        // Verificar que userDetails es instancia de Usuario
        if (!(userDetails instanceof Usuario)) {
            log.error("❌ ERROR: userDetails NO es instancia de Usuario");
            log.error("Tipo recibido: {}", userDetails.getClass().getName());
            throw new IllegalArgumentException("UserDetails debe ser instancia de Usuario");
        }
        
        Usuario usuario = (Usuario) userDetails;
        log.info("✓ Usuario: {}", usuario.getEmail());
        
        // Verificar que el usuario tenga rol
        if (usuario.getRole() == null) {
            log.error("❌ ERROR: El usuario NO tiene rol asignado");
            throw new IllegalStateException("El usuario debe tener un rol asignado");
        }
        
        String roleName = usuario.getRole().name();
        log.info("✓ Rol del usuario: {}", roleName);
        
        // Crear claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", roleName);
        log.info("✓ Claims preparados: {}", claims);
        
        // Construir token
        String token = buildToken(claims, usuario.getEmail());
        log.info("✓ Token construido (longitud: {} chars)", token.length());
        
        // VERIFICACIÓN CRÍTICA: Decodificar el token recién creado
        try {
            Claims verifiedClaims = getAllClaims(token);
            Object roleInToken = verifiedClaims.get("role");
            
            log.info("════════════════════════════════════════");
            log.info("VERIFICACIÓN DEL TOKEN GENERADO:");
            log.info("  - Subject (email): {}", verifiedClaims.getSubject());
            log.info("  - Claim 'role': {}", roleInToken);
            log.info("  - Issued at: {}", verifiedClaims.getIssuedAt());
            log.info("  - Expires at: {}", verifiedClaims.getExpiration());
            
            if (roleInToken == null) {
                log.error("❌ CRÍTICO: El token NO contiene el claim 'role'");
                log.error("Claims completos: {}", verifiedClaims);
            } else if (!roleName.equals(roleInToken.toString())) {
                log.error("❌ CRÍTICO: El rol en el token ('{}') NO coincide con el esperado ('{}')", 
                          roleInToken, roleName);
            } else {
                log.info("✓✓✓ TOKEN VÁLIDO CON ROL CORRECTO ✓✓✓");
            }
            log.info("════════════════════════════════════════");
            
        } catch (Exception e) {
            log.error("❌ ERROR verificando el token recién generado", e);
        }
        
        return token;
    }

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

    private Key getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Obtener email (subject)
    public String getEmailFromToken(String token) {
        return getClaim(token, Claims::getSubject);
    }

    // Obtener rol
    public String getRoleFromToken(String token) {
        try {
            Claims claims = getAllClaims(token);
            Object roleClaim = claims.get("role");
            
            if (roleClaim != null) {
                String roleStr = roleClaim.toString();
                log.debug("Rol extraído del token: '{}'", roleStr);
                return roleStr;
            } else {
                log.warn("No se encontró claim 'role' en el token");
                log.debug("Claims disponibles: {}", claims.keySet());
                return null;
            }
        } catch (Exception e) {
            log.error("Error extrayendo el rol del token: {}", e.getMessage(), e);
            return null;
        }
    }

    // Validar token
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String email = getEmailFromToken(token);
            boolean valid = (email.equals(userDetails.getUsername()) && !isTokenExpired(token));
            log.debug("Token válido para {}: {}", email, valid);
            return valid;
        } catch (Exception e) {
            log.error("Error validando token: {}", e.getMessage());
            return false;
        }
    }

    // === Métodos auxiliares ===

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