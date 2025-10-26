package io.bootify.cookflow.cook_flow_gestion_de_tareas.Auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    String nombre;
    String apellido;
    String tipoDocumento;
    String numeroDocumento;
    String telefono;
    String email;
    String password;
}

