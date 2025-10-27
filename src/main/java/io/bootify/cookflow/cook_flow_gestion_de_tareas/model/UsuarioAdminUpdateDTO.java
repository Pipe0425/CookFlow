package io.bootify.cookflow.cook_flow_gestion_de_tareas.model;

import io.bootify.cookflow.cook_flow_gestion_de_tareas.domain.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UsuarioAdminUpdateDTO {
    @NotBlank
    private String nombre;
    @NotBlank
    private String apellido;
    @Email
    private String email;
    private String password; 
    private Role role;
    private Boolean activo;
}
