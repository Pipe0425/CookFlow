package io.bootify.cookflow.cook_flow_gestion_de_tareas.JWT;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import io.bootify.cookflow.cook_flow_gestion_de_tareas.domain.Usuario;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String token = getTokenFromRequest(request);
        
        // DEBUG: Log detallado del token
        if (token != null) {
            log.info("=== DEBUG TOKEN ===");
            log.info("Path: {}", request.getServletPath());
            log.info("Token (primeros 50 chars): {}", token.substring(0, Math.min(50, token.length())));
            log.info("==================");
        }

        if (token == null) {
            // Solo dejamos pasar si el endpoint es público
            String path = request.getServletPath();
            if (path.startsWith("/auth") || path.startsWith("/swagger") || 
                path.startsWith("/v3/api-docs") || path.startsWith("/home") ||
                path.equals("/")) {
                filterChain.doFilter(request, response);
            } else {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"error\":\"Token ausente o inválido\"}");
            }
            return;
        }

        String username = null;
        try {
            username = jwtService.getEmailFromToken(token);
            log.info("Email extraído del token: {}", username);
        } catch (Exception ex) {
            log.warn("Token inválido o no se pudo parsear: {}", ex.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                log.info("Usuario cargado: {}", userDetails.getUsername());

                // Validar token respecto al UserDetails
                if (jwtService.isTokenValid(token, userDetails)) {

                    // Si UserDetails es instancia de Usuario, comprobamos si está activo
                    if (userDetails instanceof Usuario) {
                        Usuario usuario = (Usuario) userDetails;
                        
                        // Comprobamos campo 'activo'
                        if (!Boolean.TRUE.equals(usuario.getActivo())) {
                            log.warn("Usuario inactivo: {}", usuario.getEmail());
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.setContentType("text/plain;charset=UTF-8");
                            response.getWriter().write("Tu cuenta está inactiva. Contacta al administrador.");
                            return;
                        }
                        
                        log.info("Usuario activo confirmado: {}", usuario.getEmail());
                    }

                    // Obtener el rol directamente del token
                    String role = jwtService.getRoleFromToken(token);
                    
                    // CRÍTICO: Log para debugging
                    log.info("Rol extraído del token (raw): '{}'", role);
                    
                    List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                    
                    if (role != null && !role.trim().isEmpty()) {
                        // Limpiar el rol
                        role = role.trim();
                        
                        // Asegurar prefijo ROLE_
                        String authority = role.startsWith("ROLE_") ? role : "ROLE_" + role;
                        authorities.add(new SimpleGrantedAuthority(authority));
                        
                        log.info("Authority añadida al contexto de seguridad: '{}'", authority);
                    } else {
                        log.error("Token sin rol detectado - Probablemente es un token antiguo");
                        // Token sin rol = rechazar inmediatamente para endpoints protegidos
                        response.setStatus(HttpStatus.UNAUTHORIZED.value());
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write("{\"error\":\"Token obsoleto. Por favor, inicia sesión nuevamente.\"}");
                        return;
                    }

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            authorities
                    );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    
                    log.info("Autenticación establecida para: {} con authorities: {}", 
                             username, authorities);
                }

            } catch (Exception ex) {
                log.error("Error al autenticar con JWT: {}", ex.getMessage(), ex);
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Intenta sacar el token primero de la cookie "token", 
     * y si no existe, busca en el header Authorization: Bearer ...
     * PRIORIDAD: Cookie > Header
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String tokenFromCookie = null;
        String tokenFromHeader = null;
        
        // Buscar cookie "token"
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("token".equals(cookie.getName())) {
                    String val = cookie.getValue();
                    if (StringUtils.hasText(val)) {
                        tokenFromCookie = val;
                        log.debug("Token encontrado en cookie");
                        break;
                    }
                }
            }
        }

        // Si no hay cookie, buscar en header Authorization
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            tokenFromHeader = authHeader.substring(7);
            log.debug("Token encontrado en header Authorization");
        }
        
        // SIEMPRE dar prioridad a la cookie
        if (tokenFromCookie != null) {
            if (tokenFromHeader != null && !tokenFromCookie.equals(tokenFromHeader)) {
                log.warn("Se detectaron dos tokens diferentes (cookie vs header). Usando el de la cookie.");
            }
            return tokenFromCookie;
        }
        
        // Si no hay cookie, usar header
        if (tokenFromHeader != null) {
            return tokenFromHeader;
        }
        
        log.debug("No se encontró token en la request");
        return null;
    }

    /**
     * Excluir endpoints de autenticación del filtro.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/auth") || 
               path.startsWith("/swagger") || 
               path.startsWith("/v3/api-docs");
    }
}