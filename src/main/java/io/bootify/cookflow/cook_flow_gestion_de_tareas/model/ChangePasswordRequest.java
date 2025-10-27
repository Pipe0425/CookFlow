package io.bootify.cookflow.cook_flow_gestion_de_tareas.model;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class ChangePasswordRequest {
    @NotBlank
    private String currentPassword;
    @NotBlank
    private String newPassword;
    @NotBlank
    private String confirmPassword;
}
