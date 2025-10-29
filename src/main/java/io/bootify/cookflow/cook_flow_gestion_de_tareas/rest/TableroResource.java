package io.bootify.cookflow.cook_flow_gestion_de_tareas.rest;

import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller // en vez de @RestController
@RequestMapping("/tareas")
public class TableroResource {
    @GetMapping
    public String TableroTareas(Model model) {
        // Agregar datos si los necesitas
        return "TableroTareas"; // templates/TableroTareas.html
    }
}
