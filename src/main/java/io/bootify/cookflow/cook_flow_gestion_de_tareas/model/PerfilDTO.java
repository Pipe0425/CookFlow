package io.bootify.cookflow.cook_flow_gestion_de_tareas.model;

import io.bootify.cookflow.cook_flow_gestion_de_tareas.domain.Role;
import lombok.Data;

@Data
public class PerfilDTO {
    private Long idUsuario;
    private String nombre;
    private String apellido;
    private String email;
    private Role role;
    private Boolean activo;
}
