package io.bootify.cookflow.cook_flow_gestion_de_tareas.rest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.ui.Model;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.domain.Usuario;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.repos.UsuarioRepository;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.service.JwtService;
import lombok.AllArgsConstructor;

@Controller
@AllArgsConstructor
public class MiPerfilResource {

    private final UsuarioRepository userRepository;
    private final JwtService jwtService;
    
    @GetMapping("/miPerfil")
    public String myAccountPage(Model model, @CookieValue(value = "token", required = false) String token) {
        if (token == null) {
            return "redirect:/auth/login";
        }

        try {
            String email = jwtService.getEmailFromToken(token);
            Usuario usuarioEntity = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            usuarioEntity.setPassword(null);
            model.addAttribute("perfil", usuarioEntity);

            return "miPerfil";
            
        } catch (Exception e) {
            return "redirect:/auth/login";
        }
    }
}
