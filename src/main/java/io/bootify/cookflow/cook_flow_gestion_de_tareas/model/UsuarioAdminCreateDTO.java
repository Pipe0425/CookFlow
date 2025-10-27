package io.bootify.cookflow.cook_flow_gestion_de_tareas.model;



import io.bootify.cookflow.cook_flow_gestion_de_tareas.domain.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UsuarioAdminCreateDTO {
    @NotBlank
    private String nombre;
    @NotBlank
    private String apellido;
    @Email @NotBlank
    private String email;
    @NotBlank
    private String password; // plain -> will be encoded in service
    @NotNull
    private Role role; // Role.ADMIN, Role.STAFF, etc.
    private Boolean activo;

}
