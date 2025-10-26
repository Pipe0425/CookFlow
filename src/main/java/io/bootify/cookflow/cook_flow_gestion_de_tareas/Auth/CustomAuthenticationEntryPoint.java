package io.bootify.cookflow.cook_flow_gestion_de_tareas.Auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Entry point que responde de dos maneras:
 *  - Si la petición es "API" (xhr, accept: application/json o ruta /api/...), devuelve 401 JSON.
 *  - Si la petición es una petición de navegador normal, redirige a la página de login.
 */
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger log = LoggerFactory.getLogger(CustomAuthenticationEntryPoint.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException)
            throws IOException, ServletException {

        log.debug("Unauthorized access to: {} — reason: {}", request.getRequestURI(), authException.getMessage());

        // Detectar si la petición es API/AJAX/JSON
        boolean isApiRequest = isApiRequest(request);

        if (isApiRequest) {
            // Responder 401 con JSON (útil para Postman, fetch/AJAX, SPA)
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");

            Map<String, Object> body = new HashMap<>();
            body.put("error", "unauthorized");
            body.put("message", "No autorizado. Por favor inicie sesión.");
            body.put("path", request.getRequestURI());

            String payload = objectMapper.writeValueAsString(body);
            response.getWriter().write(payload);
            return;
        } else {
            // Petición de navegador: redirigir a la página de login
            // Evitar bucle si la ruta actual ya es /auth/login o /auth/register
            String path = request.getRequestURI();
            if (path.startsWith("/auth/login") || path.startsWith("/auth/register")) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No autorizado");
                return;
            }
            response.sendRedirect("/auth/login?error=true");
        }
    }

    private boolean isApiRequest(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        String requestedWith = request.getHeader("X-Requested-With");
        String path = request.getRequestURI();

        // Considerar API si:
        // - Ruta comienza por /api/
        // - O el Accept indica JSON
        // - O es una petición AJAX (X-Requested-With: XMLHttpRequest)
        if (path != null && path.startsWith("/api/")) {
            return true;
        }
        if (accept != null && accept.contains("application/json")) {
            return true;
        }
        if ("XMLHttpRequest".equalsIgnoreCase(requestedWith)) {
            return true;
        }
        return false;
    }
}
