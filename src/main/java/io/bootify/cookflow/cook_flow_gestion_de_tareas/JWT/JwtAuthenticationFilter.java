package io.bootify.cookflow.cook_flow_gestion_de_tareas.JWT;

import java.io.IOException;
import java.util.List;

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

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String token = getTokenFromRequest(request);

        if (token == null) {
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

        String username;
        try {
            username = jwtService.getEmailFromToken(token);
        } catch (Exception ex) {
            // token inválido -> no autenticamos, dejamos que el endpoint gestione (o devuelva 401)
            filterChain.doFilter(request, response);
            return;
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtService.isTokenValid(token, userDetails)) {

                    String role = jwtService.getRoleFromToken(token);
                    if (role != null && !role.trim().isEmpty()) {
                        role = role.trim();
                        String authority = role.startsWith("ROLE_") ? role : "ROLE_" + role;

                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                List.of(new SimpleGrantedAuthority(authority))
                        );
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    } else {
                        // token sin rol válido -> no autenticamos y respondemos 401 para endpoints protegidos
                        response.setStatus(HttpStatus.UNAUTHORIZED.value());
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write("{\"error\":\"Token sin rol válido\"}");
                        return;
                    }
                }
            } catch (Exception ex) {
                // Error durante carga de usuario o autenticación -> no autenticamos
                // Dejar pasar para que el endpoint devuelva 401 si es necesario
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Prioridad: cookie "token" > Header Authorization
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String tokenFromCookie = null;
        String tokenFromHeader = null;

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("token".equals(cookie.getName())) {
                    String val = cookie.getValue();
                    if (StringUtils.hasText(val)) {
                        tokenFromCookie = val;
                        break;
                    }
                }
            }
        }

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            tokenFromHeader = authHeader.substring(7);
        }

        if (tokenFromCookie != null) {
            return tokenFromCookie;
        }
        return tokenFromHeader;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/auth") ||
               path.startsWith("/swagger") ||
               path.startsWith("/v3/api-docs");
    }
}
