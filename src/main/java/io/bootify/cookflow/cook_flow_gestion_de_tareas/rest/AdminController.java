package io.bootify.cookflow.cook_flow_gestion_de_tareas.rest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @GetMapping("/panel")
    public String adminPanel(Model model) {
        // Agregar datos si los necesitas
        return "adminPanel"; // templates/adminPanel.html
    }
}
