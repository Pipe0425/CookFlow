package io.bootify.cookflow.cook_flow_gestion_de_tareas.JWT;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String username = null;
        try {
            username = jwtService.getUsernameFromToken(token);
        } catch (Exception ex) {
            // Token inválido / parse error -> no autenticamos, dejamos pasar para que endpoint lo rechace si necesario
            log.warn("Token inválido o no se pudo parsear: {}", ex.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // Validar token respecto al UserDetails
                if (jwtService.isTokenValid(token, userDetails)) {

                    // Si UserDetails es instancia de tu entidad Usuario, comprobamos si está activo
                    if (userDetails instanceof Usuario) {
                        Usuario usuario = (Usuario) userDetails;
                        // Comprobamos campo 'activo' (Boolean). Si es null o false, rechazamos.
                        if (!Boolean.TRUE.equals(usuario.getActivo())) {
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.setContentType("text/plain;charset=UTF-8");
                            response.getWriter().write("Tu cuenta está inactiva. Contacta al administrador.");
                            return;
                        }
                    }

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }

            } catch (Exception ex) {
                // Problema cargando usuario o autenticando: log y continuar (endpoint puede lanzar 401)
                log.error("Error al autenticar con JWT: {}", ex.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Intenta sacar el token primero de la cookie "token", y si no existe, busca en el header Authorization: Bearer ...
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        // Buscar cookie "token"
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("token".equals(cookie.getName())) {
                    String val = cookie.getValue();
                    if (StringUtils.hasText(val)) {
                        return val;
                    }
                }
            }
        }

        // Si no hay cookie, buscar en header Authorization
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    /**
     * Excluir endpoints de autenticación (login/register) del filtro.
     * Si necesitas excluir más rutas (swagger, actuator), añádelas aquí.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/auth") || path.startsWith("/swagger") || path.startsWith("/v3/api-docs");
    }
}
