package io.bootify.cookflow.cook_flow_gestion_de_tareas.Auth;

import io.bootify.cookflow.cook_flow_gestion_de_tareas.service.AuthService;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.service.UsuarioService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador de autenticación (login / register) adaptado a CookFlow.
 * Usa cookies para almacenar el JWT (nombre: "token") y renderiza plantillas Thymeleaf en templates/auth/.
 */
@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final UsuarioService usuarioService;

    // ----- LOGIN POST (form submit) -----
    @PostMapping("/login")
    public String login(@ModelAttribute("loginRequest") @Valid LoginRequest request,
                        BindingResult bindingResult,
                        Model model,
                        HttpServletResponse response) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("loginRequest", request);
            return "login";
        }

        try {
            // authService.login debe lanzar AuthenticationException en credenciales inválidas
            var authResponse = authService.login(request);

            Cookie cookie = new Cookie("token", authResponse.getToken());
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(24 * 60 * 60); // 1 día
            // cookie.setSecure(true); // activar en prod con HTTPS

            response.addCookie(cookie);

            return "redirect:/home";
        } catch (AuthenticationException ex) {
            model.addAttribute("error", "Correo o contraseña incorrectos.");
            model.addAttribute("loginRequest", request);
            return "auth/login";
        } catch (Exception ex) {
            log.error("Error en login", ex);
            model.addAttribute("error", "Ocurrió un error al iniciar sesión");
            model.addAttribute("loginRequest", request);
            return "/login";
        }
    }

    // ----- LOGIN PAGE -----
    @GetMapping("/login")
    public String showLoginPage(Model model) {
        if (!model.containsAttribute("loginRequest")) {
            model.addAttribute("loginRequest", new LoginRequest());
        }
        return "login";
    }

    // ----- REGISTER PAGE -----
    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        if (!model.containsAttribute("registerRequest")) {
            model.addAttribute("registerRequest", new RegisterRequest());
        }
        return "register";
    }

    // ----- REGISTER POST (form submit) -----
    @PostMapping("/register")
    public String createUser(@Valid @ModelAttribute("registerRequest") RegisterRequest registerRequest,
                             BindingResult result,
                             RedirectAttributes redirectAttributes,
                             Model model) {

        if (usuarioService.existsByEmail(registerRequest.getEmail())) {
            result.rejectValue("email", "error.user", "Este correo ya está registrado");
        }

        if (registerRequest.getNombre() != null && registerRequest.getNombre().equalsIgnoreCase(registerRequest.getApellido())) {
            result.rejectValue("apellido", "error.user", "El apellido no puede ser igual al nombre");
        }

        if (result.hasErrors()) {
            model.addAttribute("registerRequest", registerRequest);
            return "register";
        }

        usuarioService.create(registerRequest);
        redirectAttributes.addFlashAttribute("successMessage", "Usuario registrado exitosamente.");
        return "redirect:/auth/login";
    }
}
