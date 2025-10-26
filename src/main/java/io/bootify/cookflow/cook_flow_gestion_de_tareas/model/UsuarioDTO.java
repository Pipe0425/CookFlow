package io.bootify.cookflow.cook_flow_gestion_de_tareas.model;

import io.bootify.cookflow.cook_flow_gestion_de_tareas.domain.Role;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class UsuarioDTO {

    private Long idUsuario;

    @NotNull
    @Size(max = 100)
    private String nombre;

    @NotNull
    @Size(max = 100)
    private String apellido;

    @NotNull
    @Size(max = 120)
    private String email;

    @NotNull
    @Size(max = 255)
    private String password;

    @NotNull
    private Boolean activo;

    @NotNull
    private Role role;

}
